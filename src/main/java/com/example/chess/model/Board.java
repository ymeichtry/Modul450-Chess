package com.example.chess.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Board {
    private final Piece[][] grid = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];

    public Optional<Piece> get(Position position) {
        return Optional.ofNullable(grid[position.row() - 1][position.col() - 1]);
    }

    public void set(Position position, Piece piece) {
        grid[position.row() - 1][position.col() - 1] = piece;
    }

    public Board copy() {
        Board copy = new Board();
        for (int r = 0; r < Position.BOARD_SIZE; r++) {
            System.arraycopy(this.grid[r], 0, copy.grid[r], 0, Position.BOARD_SIZE);
        }
        return copy;
    }

    public static Board initialSetup() {
        Board board = new Board();
        setupSide(board, Color.WHITE);
        setupSide(board, Color.BLACK);
        return board;
    }

    private static void setupSide(Board board, Color color) {
        int homeRow = color == Color.WHITE ? 1 : Position.BOARD_SIZE;
        int pawnRow = color == Color.WHITE ? 2 : Position.BOARD_SIZE - 1;

        List<PieceType> backRank = Arrays.asList(
                PieceType.LOVER,
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK,
                PieceType.ROOK
        );
        for (int col = 1; col <= Position.BOARD_SIZE; col++) {
            board.set(new Position(homeRow, col), new Piece(backRank.get(col - 1), color, false));
            board.set(new Position(pawnRow, col), new Piece(PieceType.PAWN, color, false));
        }
    }
}

