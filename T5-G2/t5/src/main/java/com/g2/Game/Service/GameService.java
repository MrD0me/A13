package com.g2.Game.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g2.Game.GameDTO.GameResponseDTO;
import com.g2.Game.GameFactory.GameRegistry;
import com.g2.Game.GameModes.Compile.CompileResult;
import com.g2.Game.GameModes.GameLogic;
import com.g2.Game.Service.Exceptions.GameAlreadyExistsException;
import com.g2.Game.Service.Exceptions.GameDontExistsException;
import com.g2.Interfaces.ServiceManager;
import com.g2.Model.AchievementProgress;
import com.g2.Model.User;
import com.g2.Service.AchievementService;

@Service
public class GameService {
    private final ServiceManager serviceManager;
    private final GameRegistry gameRegistry;
    private final AchievementService achievementService;
    //Gestisco qui tutti i giochi aperti sostituire con la sessione;
    private final Map<String, GameLogic> activeGames;
    //Logger
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    public GameService(
            ServiceManager serviceManager,
            GameRegistry gameRegistry,
            AchievementService achievementService
    ) {
        this.serviceManager = serviceManager;
        this.activeGames = new ConcurrentHashMap<>();
        this.gameRegistry = gameRegistry;
        this.achievementService = achievementService;
    }

    public GameLogic CreateGame(String playerId, String mode,
            String underTestClassName,
            String type_robot,
            String difficulty) throws GameAlreadyExistsException {
        try {
            GetGame(mode, playerId);
            logger.error("createGame: Esiste già una partita per il playerId={}.", playerId);
            throw new GameAlreadyExistsException("Esiste già una partita per il giocatore con ID: " + playerId);
        } catch (GameDontExistsException e) {
            //Il game non esiste se lo cerco -> Posso creare la nuova partita
            /*
             * gameRegistry istanzia dinamicamente uno degli oggetti gameLogic (sfida, allenamento, scalata e ecc)
             * basta passargli il campo mode e dinamicamente se ne occupa lui  
             */
            GameLogic gameLogic = gameRegistry.createGame(mode, serviceManager, playerId, underTestClassName, type_robot, difficulty);
            activeGames.put(playerId, gameLogic);
            /*
             * Salvo su T4
             */
            gameLogic.CreateGame();
            logger.info("createGame: Inizio creazione partita per playerId={}, mode={}.", playerId, mode);
            return gameLogic;
        }
    }

    /*
    *     Recupero il Game 
     */
    public GameLogic GetGame(String mode, String playerId) throws GameDontExistsException {
        logger.info("getGame: Recupero partita per playerId={}, mode={}.", playerId, mode);
        GameLogic gameLogic = activeGames.get(playerId);
        if (gameLogic == null) {
            logger.error("getGame: Nessuna partita trovata per playerId={}, mode={}.", playerId, mode);
            throw new GameDontExistsException("Non esiste un game per il giocatore con ID: " + playerId + "con modalità: " + mode);
        }
        logger.info("getGame: Partita recuperata con successo per playerId={}.", playerId);
        return gameLogic;
    }

    /*
    * Elimina un game
     */
    public Boolean destroyGame(String playerId) {
        activeGames.remove(playerId);
        logger.info("destroyGame: Distruzione partita per playerId={}.", playerId);
        return true;
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
                    currentGame.getType_robot(),
                    currentGame.getDifficulty()
            );
            return new CompileResult(serviceManager,
                    currentGame.getClasseUT(),
                    currentGame.getType_robot(),
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
            updateProgressAndNotifications(currentGame.getPlayerID());
            EndGame(currentGame, userScore);
        }
        /*
        *   Preparo il DTO di Risposta 
        */
        logger.info("createResponseRun: Creazione risposta per la partita (gameFinished={}, userScore={}, robotScore={}).", gameFinished, userScore, robotScore);
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
        //L'utente ha deciso di terminare o la modalità è arrivata al termine 
        //Salvo la partita  
        currentGame.EndRound();
        currentGame.EndGame(userscore);
        destroyGame(currentGame.getPlayerID());
    }

    //Gestione Trofei e notifiche
    private void updateProgressAndNotifications(String playerId) {
        User user = serviceManager.handleRequest("T23", "GetUser", User.class, playerId);
        String email = user.getEmail();
        List<AchievementProgress> newAchievements = achievementService.updateProgressByPlayer(user.getId().intValue());
        achievementService.updateNotificationsForAchievements(email, newAchievements);
    }
}
