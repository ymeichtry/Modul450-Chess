package com.example.chess.game;

public record MoveResult(boolean success, String message, boolean check) {
    public static MoveResult ok(String message, boolean check) {
        return new MoveResult(true, message, check);
    }

    public static MoveResult fail(String message) {
        return new MoveResult(false, message, false);
    }
}

