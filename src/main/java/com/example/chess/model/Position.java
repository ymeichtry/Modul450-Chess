package com.example.chess.model;

import java.util.Locale;
import java.util.Objects;

public record Position(int row, int col) {
    public static final int BOARD_SIZE = 10;

    public Position {
        if (row < 1 || row > BOARD_SIZE || col < 1 || col > BOARD_SIZE) {
            throw new IllegalArgumentException("Position out of bounds: " + row + "," + col);
        }
    }

    public static Position fromAlgebraic(String notation) {
        Objects.requireNonNull(notation, "notation");
        var trimmed = notation.trim().toUpperCase(Locale.ROOT);
        if (trimmed.length() < 2 || trimmed.length() > 3) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }
        char fileChar = trimmed.charAt(0);
        int col = fileChar - 'A' + 1;
        int row;
        try {
            row = Integer.parseInt(trimmed.substring(1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid notation: " + notation, ex);
        }
        return new Position(row, col);
    }

    public String toAlgebraic() {
        char file = (char) ('A' + col - 1);
        return "" + file + row;
    }

    public Position offset(int rowDelta, int colDelta) {
        int newRow = row + rowDelta;
        int newCol = col + colDelta;
        if (newRow < 1 || newRow > BOARD_SIZE || newCol < 1 || newCol > BOARD_SIZE) {
            return null;
        }
        return new Position(newRow, newCol);
    }
}

