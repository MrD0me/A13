package com.g2.Game.GameDTO;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.g2.Game.GameModes.Compile.CompileResult;

public class GameResponseDTO {

    @JsonProperty("robotScore")
    private int robotScore;

    @JsonProperty("userScore")
    private int userScore;

    @JsonProperty("GameOver")
    private boolean gameOver;

    @JsonProperty("userJacocoCoverage")
    private CompileResult UserCoverageDetails;

    @JsonProperty("robotJacocoCoverage")
    private CompileResult RobotCoveragerDetails;

    @JsonProperty("isWinner")
    private Boolean isWinner;

    public GameResponseDTO() {
        //costruttore vuoto 
    }

    /*
     * Costruttore che converte un CompileResult in un GameResponseDTO
     */
    public GameResponseDTO(CompileResult UserCompileResult,
            CompileResult RobotCompileResult,
            Boolean gameFinished,
            int robotScore,
            int UserScore,
            Boolean isWinner) {
        this.robotScore = robotScore;
        this.userScore = UserScore;
        this.gameOver = gameFinished;
        this.isWinner = isWinner;
        // Dettagli della copertura per l'utente 
        // Assegna un valore di fallback se i CompileResult sono null
        this.UserCoverageDetails = Objects.requireNonNullElse(UserCompileResult, CompileResult.DEFAULT);
        this.RobotCoveragerDetails = Objects.requireNonNullElse(RobotCompileResult, CompileResult.DEFAULT);
    }

    // Getters and setters
    public int getRobotScore() {
        return robotScore;
    }

    public void setRobotScore(int robotScore) {
        this.robotScore = robotScore;
    }

    public int getUserScore() {
        return userScore;
    }

    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public CompileResult getUserCoverageDetails() {
        return UserCoverageDetails;
    }

    public void setUserCoverageDetails(CompileResult UserCoverageDetails) {
        this.UserCoverageDetails = UserCoverageDetails;
    }

    public CompileResult getRobotCoveragerDetails() {
        return RobotCoveragerDetails;
    }

    public void setRobotCoveragerDetails(CompileResult RobotCoveragerDetails) {
        this.RobotCoveragerDetails = RobotCoveragerDetails;
    }

    public Boolean getIsWinner() {
        return isWinner;
    }

    public void setIsWinner(Boolean isWinner) {
        this.isWinner = isWinner;
    }
}
