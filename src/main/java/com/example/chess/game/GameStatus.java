package com.example.chess.game;

/**
 * Represents the current status of the game.
 */
public enum GameStatus {
    /**
     * Game is in progress.
     */
    ONGOING,
    
    /**
     * A player is in check.
     */
    CHECK,
    
    /**
     * A player is checkmated - game over.
     */
    CHECKMATE,
    
    /**
     * Game ended in a draw (stalemate or agreement).
     */
    DRAW,
    
    /**
     * A player's time ran out.
     */
    TIME_UP,
    
    /**
     * A player resigned.
     */
    RESIGNED
}
