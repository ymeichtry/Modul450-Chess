package com.example.chess.game;

import com.example.chess.model.Board;
import com.example.chess.model.Color;
import com.example.chess.model.Move;
import com.example.chess.model.Piece;
import com.example.chess.model.PieceType;
import com.example.chess.model.Position;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Game {
    private Board board;
    private Color activeColor;
    private GameStatus status;
    private ChessClock clock;
    private boolean drawOffered;
    private Color drawOfferedBy;
    private Color winner;

    public Game() {
        reset();
    }

    public Game(Board board, Color activeColor) {
        this.board = board;
        this.activeColor = activeColor;
        this.status = GameStatus.ONGOING;
        this.drawOffered = false;
    }

    public void reset() {
        this.board = Board.initialSetup();
        this.activeColor = Color.WHITE;
        this.status = GameStatus.ONGOING;
        this.drawOffered = false;
        this.drawOfferedBy = null;
        this.winner = null;
        if (clock != null) {
            clock = new ChessClock(Duration.ofMinutes(10));
        }
    }

    /**
     * Enables the chess clock with the specified time per player.
     */
    public void enableClock(Duration timePerPlayer) {
        this.clock = new ChessClock(timePerPlayer);
        this.clock.start(Color.WHITE);
    }

    public ChessClock getClock() {
        return clock;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Color getWinner() {
        return winner;
    }

    public boolean isDrawOffered() {
        return drawOffered;
    }

    public Color getDrawOfferedBy() {
        return drawOfferedBy;
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public Board getBoard() {
        return board;
    }

    public MoveResult playMove(String input) {
        try {
            Move parsed = parseMove(input);
            return playMove(parsed);
        } catch (IllegalArgumentException ex) {
            return MoveResult.fail(ex.getMessage());
        }
    }

    public MoveResult playMove(Move move) {
        Objects.requireNonNull(move, "move");
        
        // Check if game is already over
        if (status == GameStatus.CHECKMATE || status == GameStatus.DRAW 
                || status == GameStatus.TIME_UP || status == GameStatus.RESIGNED) {
            return MoveResult.fail("Game is over: " + status);
        }
        
        // Check clock if enabled
        if (clock != null && clock.isTimeUp(activeColor)) {
            status = GameStatus.TIME_UP;
            winner = activeColor.opposite();
            return MoveResult.fail("Time is up for " + activeColor);
        }
        
        Optional<Piece> pieceOpt = board.get(move.from());
        if (pieceOpt.isEmpty()) {
            return MoveResult.fail("No piece at " + move.from().toAlgebraic());
        }
        Piece piece = pieceOpt.get();
        if (piece.color() != activeColor) {
            return MoveResult.fail("It is not " + piece.color() + "'s turn");
        }
        
        // Check for castling move
        if (piece.type() == PieceType.KING && isCastlingMove(move, piece)) {
            return executeCastling(move, piece);
        }
        
        if (!isLegalMoveIgnoringCheck(board, move, piece)) {
            return MoveResult.fail("Illegal move for " + piece.type());
        }

        Board simulated = board.copy();
        applyMove(simulated, move, piece);
        if (isInCheck(simulated, activeColor)) {
            return MoveResult.fail("Move would leave king in check");
        }

        applyMove(board, move, piece);
        
        // Clear draw offer after move
        drawOffered = false;
        drawOfferedBy = null;
        
        // Switch clock
        if (clock != null) {
            clock.switchClock();
        }
        
        activeColor = activeColor.opposite();
        
        // Check for check, checkmate, or stalemate
        boolean check = isInCheck(board, activeColor);
        boolean hasLegalMoves = hasAnyLegalMove(activeColor);
        
        if (check && !hasLegalMoves) {
            status = GameStatus.CHECKMATE;
            winner = activeColor.opposite();
            return MoveResult.ok("Checkmate! " + winner + " wins!", false);
        } else if (!check && !hasLegalMoves) {
            status = GameStatus.DRAW;
            return MoveResult.ok("Stalemate! Game is a draw.", false);
        } else if (check) {
            status = GameStatus.CHECK;
            return MoveResult.ok("Check!", true);
        }
        
        status = GameStatus.ONGOING;
        return MoveResult.ok("Move accepted", false);
    }
    
    /**
     * Offers a draw to the opponent.
     */
    public MoveResult offerDraw() {
        if (status != GameStatus.ONGOING && status != GameStatus.CHECK) {
            return MoveResult.fail("Cannot offer draw - game is over");
        }
        drawOffered = true;
        drawOfferedBy = activeColor;
        if (clock != null) {
            clock.pause();
        }
        return MoveResult.ok("Draw offered by " + activeColor, false);
    }
    
    /**
     * Accepts the draw offer.
     */
    public MoveResult acceptDraw() {
        if (!drawOffered) {
            return MoveResult.fail("No draw has been offered");
        }
        if (drawOfferedBy == activeColor) {
            return MoveResult.fail("You cannot accept your own draw offer");
        }
        status = GameStatus.DRAW;
        return MoveResult.ok("Draw accepted. Game ends in a draw.", false);
    }
    
    /**
     * Declines the draw offer.
     */
    public MoveResult declineDraw() {
        if (!drawOffered) {
            return MoveResult.fail("No draw has been offered");
        }
        drawOffered = false;
        drawOfferedBy = null;
        if (clock != null) {
            clock.resume();
        }
        return MoveResult.ok("Draw declined. Game continues.", false);
    }
    
    /**
     * Resigns the game.
     */
    public MoveResult resign() {
        if (status == GameStatus.CHECKMATE || status == GameStatus.DRAW 
                || status == GameStatus.TIME_UP || status == GameStatus.RESIGNED) {
            return MoveResult.fail("Game is already over");
        }
        status = GameStatus.RESIGNED;
        winner = activeColor.opposite();
        return MoveResult.ok(activeColor + " resigns. " + winner + " wins!", false);
    }
    
    /**
     * Checks if the given color has any legal move available.
     */
    public boolean hasAnyLegalMove(Color color) {
        List<Move> allMoves = generateAllMoves(color);
        for (Move move : allMoves) {
            Board simulated = board.copy();
            Piece piece = board.get(move.from()).orElse(null);
            if (piece != null) {
                applyMove(simulated, move, piece);
                if (!isInCheck(simulated, color)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Generates all possible moves for a color (without checking if they leave king in check).
     */
    private List<Move> generateAllMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        for (int row = 1; row <= Position.BOARD_SIZE; row++) {
            for (int col = 1; col <= Position.BOARD_SIZE; col++) {
                Position from = new Position(row, col);
                Optional<Piece> pieceOpt = board.get(from);
                if (pieceOpt.isEmpty() || pieceOpt.get().color() != color) {
                    continue;
                }
                Piece piece = pieceOpt.get();
                // Generate all possible destinations
                for (int toRow = 1; toRow <= Position.BOARD_SIZE; toRow++) {
                    for (int toCol = 1; toCol <= Position.BOARD_SIZE; toCol++) {
                        Position to = new Position(toRow, toCol);
                        Move move = new Move(from, to);
                        if (isLegalMoveIgnoringCheck(board, move, piece)) {
                            moves.add(move);
                        }
                    }
                }
            }
        }
        return moves;
    }
    
    /**
     * Checks if a move is a castling attempt.
     */
    private boolean isCastlingMove(Move move, Piece piece) {
        if (piece.type() != PieceType.KING || piece.hasMoved()) {
            return false;
        }
        int colDelta = move.to().col() - move.from().col();
        // Castling is only to the Lover's side (column A)
        return Math.abs(colDelta) == 2 && move.from().row() == move.to().row() && colDelta < 0;
    }
    
    /**
     * Executes a castling move according to the special rules.
     * Castling can only be done towards the Lover's side, and the Lover must not have moved.
     */
    private MoveResult executeCastling(Move move, Piece king) {
        int homeRow = king.color() == Color.WHITE ? 1 : Position.BOARD_SIZE;
        
        // The Lover is at column 1 (A)
        Position loverPos = new Position(homeRow, 1);
        // The Rook is at column 2 (B)
        Position rookPos = new Position(homeRow, 2);
        
        Optional<Piece> loverOpt = board.get(loverPos);
        Optional<Piece> rookOpt = board.get(rookPos);
        
        if (loverOpt.isEmpty() || loverOpt.get().type() != PieceType.LOVER 
                || loverOpt.get().color() != king.color() || loverOpt.get().hasMoved()) {
            return MoveResult.fail("Cannot castle: Lover has moved or is missing");
        }
        
        if (rookOpt.isEmpty() || rookOpt.get().type() != PieceType.ROOK 
                || rookOpt.get().color() != king.color() || rookOpt.get().hasMoved()) {
            return MoveResult.fail("Cannot castle: Rook has moved or is missing");
        }
        
        // Check that king is not in check
        if (isInCheck(board, king.color())) {
            return MoveResult.fail("Cannot castle while in check");
        }
        
        // Check that squares between king and rook are empty
        int kingCol = move.from().col(); // King starts at F (col 6)
        for (int col = 3; col < kingCol; col++) {
            Position pos = new Position(homeRow, col);
            if (board.get(pos).isPresent()) {
                return MoveResult.fail("Cannot castle: pieces in the way");
            }
        }
        
        // Check that king doesn't pass through check
        for (int col = kingCol - 1; col >= kingCol - 2; col--) {
            Position intermediate = new Position(homeRow, col);
            Board simulated = board.copy();
            simulated.set(move.from(), null);
            simulated.set(intermediate, king.withMoved());
            if (isInCheck(simulated, king.color())) {
                return MoveResult.fail("Cannot castle through check");
            }
        }
        
        // Execute castling: King moves 2 squares towards A, Rook jumps over
        Position kingDest = new Position(homeRow, kingCol - 2); // King to D (col 4)
        Position rookDest = new Position(homeRow, kingCol - 1); // Rook to E (col 5)
        
        board.set(move.from(), null);
        board.set(rookPos, null);
        board.set(kingDest, king.withMoved());
        board.set(rookDest, rookOpt.get().withMoved());
        
        // Clear draw offer after move
        drawOffered = false;
        drawOfferedBy = null;
        
        // Switch clock
        if (clock != null) {
            clock.switchClock();
        }
        
        activeColor = activeColor.opposite();
        
        // Check for check/checkmate
        boolean check = isInCheck(board, activeColor);
        boolean hasLegalMoves = hasAnyLegalMove(activeColor);
        
        if (check && !hasLegalMoves) {
            status = GameStatus.CHECKMATE;
            winner = activeColor.opposite();
            return MoveResult.ok("Castling. Checkmate! " + winner + " wins!", false);
        } else if (!check && !hasLegalMoves) {
            status = GameStatus.DRAW;
            return MoveResult.ok("Castling. Stalemate! Game is a draw.", false);
        } else if (check) {
            status = GameStatus.CHECK;
            return MoveResult.ok("Castling. Check!", true);
        }
        
        status = GameStatus.ONGOING;
        return MoveResult.ok("Castling completed", false);
    }

    private void applyMove(Board targetBoard, Move move, Piece piece) {
        targetBoard.set(move.from(), null);
        Piece moved = piece.withMoved();
        Position destination = move.to();
        if (piece.type() == PieceType.PAWN && reachesPromotionRank(destination, piece.color())) {
            moved = new Piece(PieceType.QUEEN, piece.color(), true);
        }
        targetBoard.set(destination, moved);
    }

    private boolean reachesPromotionRank(Position destination, Color color) {
        return (color == Color.WHITE && destination.row() == Position.BOARD_SIZE)
                || (color == Color.BLACK && destination.row() == 1);
    }

    Move parseMove(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Empty move");
        }
        String normalized = input.trim().toUpperCase();
        normalized = normalized.replace("-", " ").replace("TO", " ");
        String[] parts = normalized.split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Use notation like A2 A3 or A2-A3");
        }
        Position from = Position.fromAlgebraic(parts[0]);
        Position to = Position.fromAlgebraic(parts[1]);
        return new Move(from, to);
    }

    boolean isLegalMoveIgnoringCheck(Board currentBoard, Move move, Piece piece) {
        if (move.from().equals(move.to())) {
            return false;
        }
        Optional<Piece> dest = currentBoard.get(move.to());
        if (dest.isPresent() && dest.get().color() == piece.color()) {
            return false;
        }
        int rowDelta = move.to().row() - move.from().row();
        int colDelta = move.to().col() - move.from().col();

        return switch (piece.type()) {
            case KING -> kingLike(rowDelta, colDelta, 1, currentBoard, move);
            case LOVER -> kingLike(rowDelta, colDelta, 1, currentBoard, move);
            case QUEEN -> sliding(rowDelta, colDelta, 10, currentBoard, move);
            case ROOK -> straight(rowDelta, colDelta, Position.BOARD_SIZE, currentBoard, move);
            case BISHOP -> diagonal(rowDelta, colDelta, 6, currentBoard, move);
            case KNIGHT -> knightMove(rowDelta, colDelta);
            case PAWN -> pawnMove(piece, currentBoard, move, rowDelta, colDelta);
        };
    }

    private boolean pawnMove(Piece piece, Board currentBoard, Move move, int rowDelta, int colDelta) {
        int forward = piece.color() == Color.WHITE ? 1 : -1;
        int startRow = piece.color() == Color.WHITE ? 2 : Position.BOARD_SIZE - 1;
        // capture
        if (colDelta != 0 && Math.abs(colDelta) == 1 && rowDelta == forward) {
            return currentBoard.get(move.to()).map(p -> p.color() != piece.color()).orElse(false);
        }
        // forward move(s)
        if (colDelta != 0) {
            return false;
        }
        if (rowDelta == forward) {
            return currentBoard.get(move.to()).isEmpty();
        }
        if (rowDelta == 2 * forward && move.from().row() == startRow) {
            Position intermediate = move.from().offset(forward, 0);
            return intermediate != null
                    && currentBoard.get(intermediate).isEmpty()
                    && currentBoard.get(move.to()).isEmpty();
        }
        return false;
    }

    private boolean knightMove(int rowDelta, int colDelta) {
        int absR = Math.abs(rowDelta);
        int absC = Math.abs(colDelta);
        return (absR == 3 && absC == 1) || (absR == 1 && absC == 3);
    }

    private boolean diagonal(int rowDelta, int colDelta, int maxDistance, Board board, Move move) {
        if (Math.abs(rowDelta) != Math.abs(colDelta)) {
            return false;
        }
        return pathClear(board, move, Math.min(maxDistance, Position.BOARD_SIZE));
    }

    private boolean straight(int rowDelta, int colDelta, int maxDistance, Board board, Move move) {
        boolean sameRow = rowDelta == 0 && colDelta != 0;
        boolean sameCol = colDelta == 0 && rowDelta != 0;
        if (!sameRow && !sameCol) {
            return false;
        }
        return pathClear(board, move, maxDistance);
    }

    private boolean sliding(int rowDelta, int colDelta, int maxDistance, Board board, Move move) {
        boolean diagonal = Math.abs(rowDelta) == Math.abs(colDelta) && rowDelta != 0;
        boolean straight = rowDelta == 0 || colDelta == 0;
        if (!diagonal && !straight) {
            return false;
        }
        return pathClear(board, move, maxDistance);
    }

    private boolean kingLike(int rowDelta, int colDelta, int maxDistance, Board board, Move move) {
        if (Math.abs(rowDelta) > maxDistance || Math.abs(colDelta) > maxDistance) {
            return false;
        }
        if (rowDelta == 0 && colDelta == 0) {
            return false;
        }
        return pathClear(board, move, maxDistance);
    }

    private boolean pathClear(Board board, Move move, int maxDistance) {
        int rowDelta = move.to().row() - move.from().row();
        int colDelta = move.to().col() - move.from().col();
        int stepRow = Integer.compare(rowDelta, 0);
        int stepCol = Integer.compare(colDelta, 0);
        int distance = Math.max(Math.abs(rowDelta), Math.abs(colDelta));
        if (distance > maxDistance) {
            return false;
        }
        Position current = move.from();
        for (int i = 1; i < distance; i++) {
            current = current.offset(stepRow, stepCol);
            if (current == null) {
                return false;
            }
            if (board.get(current).isPresent()) {
                return false;
            }
        }
        return true;
    }

    public boolean isInCheck(Board candidate, Color color) {
        Position kingPos = findKing(candidate, color)
                .orElseThrow(() -> new IllegalStateException("King missing for " + color));
        Color opponent = color.opposite();
        for (int row = 1; row <= Position.BOARD_SIZE; row++) {
            for (int col = 1; col <= Position.BOARD_SIZE; col++) {
                Position pos = new Position(row, col);
                Optional<Piece> pieceOpt = candidate.get(pos);
                if (pieceOpt.isEmpty()) {
                    continue;
                }
                Piece attacker = pieceOpt.get();
                if (attacker.color() != opponent || attacker.type() == PieceType.LOVER) {
                    continue; // lover does not give check
                }
                Move potential = new Move(pos, kingPos);
                if (isLegalMoveIgnoringCheck(candidate, potential, attacker)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<Position> findKing(Board b, Color color) {
        for (int row = 1; row <= Position.BOARD_SIZE; row++) {
            for (int col = 1; col <= Position.BOARD_SIZE; col++) {
                Position pos = new Position(row, col);
                Optional<Piece> piece = b.get(pos);
                if (piece.isPresent() && piece.get().type() == PieceType.KING && piece.get().color() == color) {
                    return Optional.of(pos);
                }
            }
        }
        return Optional.empty();
    }

    public String renderBoard() {
        StringBuilder sb = new StringBuilder();
        Map<PieceType, String> whiteSymbols = Map.of(
                PieceType.KING, "♔",
                PieceType.QUEEN, "♕",
                PieceType.ROOK, "♖",
                PieceType.BISHOP, "♗",
                PieceType.KNIGHT, "♘",
                PieceType.PAWN, "♙",
                PieceType.LOVER, "♡"
        );
        Map<PieceType, String> blackSymbols = Map.of(
                PieceType.KING, "♚",
                PieceType.QUEEN, "♛",
                PieceType.ROOK, "♜",
                PieceType.BISHOP, "♝",
                PieceType.KNIGHT, "♞",
                PieceType.PAWN, "♟",
                PieceType.LOVER, "♥"
        );
        
        // Show clock if enabled
        if (clock != null) {
            sb.append("╔════════════════════════════════╗").append(System.lineSeparator());
            sb.append("║  ⏱ WHITE: ").append(clock.formatTime(Color.WHITE));
            sb.append("  │  BLACK: ").append(clock.formatTime(Color.BLACK)).append("  ║");
            sb.append(System.lineSeparator());
            sb.append("╚════════════════════════════════╝").append(System.lineSeparator());
        }
        
        for (int row = Position.BOARD_SIZE; row >= 1; row--) {
            sb.append(String.format("%2d ", row));
            for (int col = 1; col <= Position.BOARD_SIZE; col++) {
                Position pos = new Position(row, col);
                Optional<Piece> piece = board.get(pos);
                if (piece.isEmpty()) {
                    sb.append(". ");
                    continue;
                }
                Map<PieceType, String> map = piece.get().color() == Color.WHITE ? whiteSymbols : blackSymbols;
                sb.append(map.get(piece.get().type())).append(" ");
            }
            sb.append(System.lineSeparator());
        }
        sb.append("   ");
        for (int col = 1; col <= Position.BOARD_SIZE; col++) {
            char file = (char) ('A' + col - 1);
            sb.append(file).append(" ");
        }
        sb.append(System.lineSeparator());
        sb.append("Turn: ").append(activeColor);
        
        // Show game status
        if (status != null && status != GameStatus.ONGOING) {
            sb.append(" | Status: ").append(status);
            if (winner != null) {
                sb.append(" - Winner: ").append(winner);
            }
        }
        
        // Show draw offer
        if (drawOffered) {
            sb.append(System.lineSeparator());
            sb.append("⚠ Draw offered by ").append(drawOfferedBy);
            sb.append(" - type 'accept' or 'decline'");
        }
        
        return sb.toString();
    }
    
    /**
     * Checks if the game is over.
     */
    public boolean isGameOver() {
        return status == GameStatus.CHECKMATE 
                || status == GameStatus.DRAW 
                || status == GameStatus.TIME_UP 
                || status == GameStatus.RESIGNED;
    }
}

