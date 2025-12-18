package com.example.chess.model;

public record Piece(PieceType type, Color color, boolean hasMoved) {

    public Piece withMoved() {
        return hasMoved ? this : new Piece(type, color, true);
    }
}

