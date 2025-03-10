/*
 *   Copyright (c) 2025 Stefano Marano https://github.com/StefanoMarano80017
 *   All rights reserved.

 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.g2.Game.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g2.Game.GameDTO.GameResponseDTO;
import com.g2.Game.GameFactory.GameRegistry;
import com.g2.Game.GameModes.Compile.CompileResult;
import com.g2.Game.GameModes.GameLogic;
import com.g2.Interfaces.ServiceManager;
import com.g2.Model.AchievementProgress;
import com.g2.Model.User;
import com.g2.Service.AchievementService;
import com.g2.Session.Exceptions.GameModeAlreadyExist;
import com.g2.Session.Exceptions.GameModeDontExist;
import com.g2.Session.Exceptions.SessionDontExist;
import com.g2.Session.SessionService;

@Service
public class GameService {

    private final ServiceManager serviceManager;
    private final GameRegistry gameRegistry;
    private final AchievementService achievementService;
    private final SessionService sessionService;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    public GameService(ServiceManager serviceManager,
                        GameRegistry gameRegistry,
                        AchievementService achievementService,
                        SessionService sessionService) {
        this.serviceManager = serviceManager;
        this.gameRegistry = gameRegistry;
        this.achievementService = achievementService;
        this.sessionService = sessionService;
    }

    // creare modello updateGame
    public GameLogic CreateGame(String playerId,
            String mode,
            String underTestClassName,
            String type_robot,
            String difficulty) throws GameModeAlreadyExist {
        try {
            /*
            * gameRegistry istanzia dinamicamente uno degli oggetti gameLogic (sfida, allenamento, scalata e ecc)
            * basta passargli il campo mode e dinamicamente se ne occupa lui  
             */
            GameLogic gameLogic = gameRegistry.createGame(  mode, 
                                                            serviceManager, 
                                                            playerId, 
                                                            underTestClassName, 
                                                            type_robot, 
                                                            difficulty);
            logger.info("createGame: oggetto game creato con successo per playerId={}, mode={}.", playerId, mode);
            /*
             * Salvo il game in T4
             */
            gameLogic.CreateGame();
            logger.info("createGame: Inizio creazione partita per playerId={}, mode={}.", playerId, mode);
            /*
            * Creo nella sessione i dati di gioco 
            */
            sessionService.SetGameMode(playerId, gameLogic);
            logger.info("createGame: sessione aggiornata con successo per playerId={}, mode={}.", playerId, mode);
            return gameLogic;
        } catch (SessionDontExist e) {
            logger.info("createGame: SessionDontExist per playerId={}, mode={}.", playerId, mode);
        } catch (Exception e) {
            logger.info("createGame: Exception per playerId={}, mode={}.", playerId, mode);
        }
        return null;
    }

    public GameLogic GetGame(String mode, String playerId) throws GameModeDontExist {
        try {
            logger.info("getGame: Recupero partita per playerId={}, mode={}.", playerId, mode);
            GameLogic game = sessionService.getGameMode(playerId, mode);
            game.setServiceManager(serviceManager);
            logger.info("getGame: Partita recuperata con successo per playerId={} e modalità={}.", playerId, mode);
            return game;
        } catch ( SessionDontExist e) {
            throw new GameModeDontExist("Game don't exist ");
        }
    }

    public boolean destroyGame(String playerId, String mode) {
        try {
            sessionService.removeGameMode(playerId, mode, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean UpdateGame(String playerId, GameLogic game){
        return sessionService.updateGameMode(playerId, game);
    }

    public CompileResult handleCompile(String Classname, String testingClassCode) {
        logger.info("handleCompile: Inizio compilazione per className={}.", Classname);
        return new CompileResult(Classname, testingClassCode, this.serviceManager);
    }

    /*
    *  Sfrutto T4 per avere i risultati dei robot
    */
    public CompileResult GetRobotCoverage(GameLogic currentGame) {
        try {
            logger.info("Richiesta Coverage robot per testClass={}, robotType={}, difficulty={}.",
                    currentGame.getClasseUT(),
                    currentGame.getTypeRobot(),
                    currentGame.getDifficulty()
            );
            return new CompileResult(serviceManager,
                    currentGame.getClasseUT(),
                    currentGame.getTypeRobot(),
                    currentGame.getDifficulty()
            );
        } catch (Exception e) {
            logger.error("[GAMECONTROLLER] GetRobotCoverage:", e);
            return null;
        }
    }

    public boolean handleGameLogic(int userScore, int robotScore, GameLogic currentGame, Boolean isGameEnd) {
        logger.info("handleGameLogic: Avvio logica di gioco per playerId={}.", currentGame.getPlayerID());
        currentGame.NextTurn(userScore, robotScore);
        boolean gameFinished = isGameEnd || currentGame.isGameEnd();
        logger.info("handleGameLogic: Stato partita (gameFinished={}) per playerId={}.", gameFinished, currentGame.getPlayerID());
        UpdateGame(currentGame.getPlayerID(), currentGame);
        return gameFinished;
    }

    public GameResponseDTO handleGameResponse(
            boolean gameFinished,
            GameLogic currentGame,
            CompileResult UsercompileResult,
            CompileResult RobotcompileResult,
            int userScore,
            int robotScore
    ) {
        /*
         * Se la partita è finita devo notifica, controllare i trofei e salvare in T4
         */
        if (gameFinished) {
            logger.info("handleGameLogic: Partita terminata per playerId={}. Avvio aggiornamento progressi e notifiche.", currentGame.getPlayerID());
            /*
             * Notifiche e trofei 
             */
            updateProgressAndNotifications(currentGame.getPlayerID());
            /*
             * Chiudo la partita in T4 e chiudo sessione 
             */
            EndGame(currentGame, userScore);
        }
        /*
        *   Preparo il DTO di Risposta 
         */
        logger.info("createResponseRun: Creazione risposta per la partita (gameFinished={}, userScore={}, robotScore={}).", gameFinished, userScore, robotScore);
        // metodo "UpdateGame"
        return new GameResponseDTO(
                UsercompileResult,
                RobotcompileResult,
                gameFinished,
                robotScore,
                userScore,
                currentGame.isWinner()
        );
    }

    public void EndGame(GameLogic currentGame, int userscore) {
        logger.info("endGame: Terminazione partita per playerId={}.", currentGame.getPlayerID());
        /*
        *       L'utente ha deciso di terminare la partita o 
        *       la modalità di gioco ha determianto il termine
        *       Salvo la partita 
        *       Distruggo la partita salvata in sessione  
        */
        currentGame.EndRound();
        currentGame.EndGame(userscore);
        destroyGame(currentGame.getPlayerID(), currentGame.getMode());
    }

    //Gestione Trofei e notifiche
    private void updateProgressAndNotifications(String playerId) {
        User user = serviceManager.handleRequest("T23", "GetUser", User.class, playerId);
        String email = user.getEmail();
        List<AchievementProgress> newAchievements = achievementService.updateProgressByPlayer(user.getId().intValue());
        achievementService.updateNotificationsForAchievements(email, newAchievements);
    }
}
