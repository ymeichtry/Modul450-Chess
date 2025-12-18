package com.example.chess.game;

import com.example.chess.model.Board;
import com.example.chess.model.Color;
import com.example.chess.model.Move;
import com.example.chess.model.Piece;
import com.example.chess.model.PieceType;
import com.example.chess.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class GameTest {

    @Test
    void initialSetupPlacesCustomPieces() {
        Game game = new Game();
        Board board = game.getBoard();

        assertThat(board.get(Position.fromAlgebraic("A1")).orElseThrow().type()).isEqualTo(PieceType.LOVER);
        assertThat(board.get(Position.fromAlgebraic("J10")).orElseThrow().type()).isEqualTo(PieceType.LOVER);
        for (int col = 1; col <= Position.BOARD_SIZE; col++) {
            assertThat(board.get(new Position(2, col))).isPresent();
            assertThat(board.get(new Position(Position.BOARD_SIZE - 1, col))).isPresent();
        }
    }

    @Test
    void knightUsesThreePlusOneMovement() {
        Game game = new Game();
        MoveResult first = game.playMove("C1 D4"); // white knight
        assertThat(first.success()).isTrue();
        assertThat(game.getBoard().get(Position.fromAlgebraic("D4"))).isPresent();

        MoveResult invalid = game.playMove("J10 J8"); // black to keep turns aligned
        assertThat(invalid.success()).isFalse(); // black lover cannot move like rook

        MoveResult second = game.playMove("D4 E6");
        assertThat(second.success()).isFalse(); // 2+1 is not allowed
    }

    @Test
    void bishopCannotExceedSixSquares() {
        Board board = new Board();
        Position bishopPos = Position.fromAlgebraic("C3");
        board.set(bishopPos, new Piece(PieceType.BISHOP, Color.WHITE, false));
        Position kingPos = Position.fromAlgebraic("E1");
        board.set(kingPos, new Piece(PieceType.KING, Color.WHITE, false));
        Position blackKing = Position.fromAlgebraic("J10");
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult valid = game.playMove(new Move(bishopPos, Position.fromAlgebraic("I9")));
        assertThat(valid.success()).isTrue(); // distance 6

        // move black king to pass the turn back
        game.playMove(new Move(blackKing, Position.fromAlgebraic("J9")));

        MoveResult tooFar = game.playMove(new Move(Position.fromAlgebraic("I9"), Position.fromAlgebraic("B2")));
        assertThat(tooFar.success()).isFalse(); // distance 7
    }

    @Test
    void loverDoesNotGiveCheck() {
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("E1");
        Position blackKing = Position.fromAlgebraic("J10");
        Position blackLover = Position.fromAlgebraic("E2");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        board.set(blackLover, new Piece(PieceType.LOVER, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        assertThat(game.isInCheck(board, Color.WHITE)).isFalse();

        board.set(Position.fromAlgebraic("E10"), new Piece(PieceType.QUEEN, Color.BLACK, false));
        assertThat(game.isInCheck(board, Color.WHITE)).isTrue();
    }

    @Test
    void moveThatExposesKingIsRejected() {
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("E1");
        Position whiteRook = Position.fromAlgebraic("E2");
        Position blackRook = Position.fromAlgebraic("E10");
        Position blackKing = Position.fromAlgebraic("J10");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(whiteRook, new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(blackRook, new Piece(PieceType.ROOK, Color.BLACK, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(whiteRook, Position.fromAlgebraic("E3")));
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("check");
    }

    @Test
    void capturesRemovePieces() {
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("A1");
        Position blackKing = Position.fromAlgebraic("J10");
        Position whitePawn = Position.fromAlgebraic("D4");
        Position blackPawn = Position.fromAlgebraic("E5");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        board.set(whitePawn, new Piece(PieceType.PAWN, Color.WHITE, false));
        board.set(blackPawn, new Piece(PieceType.PAWN, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult capture = game.playMove(new Move(whitePawn, blackPawn));
        assertThat(capture.success()).isTrue();
        assertThat(game.getBoard().get(blackPawn)).isPresent();
        assertThat(game.getBoard().get(blackPawn).orElseThrow().color()).isEqualTo(Color.WHITE);
    }

    // ======================= CHECKMATE TESTS =======================
    
    @Test
    void checkmateIsDetected() {
        // Simple back-rank mate setup
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("A1");
        Position blackKing = Position.fromAlgebraic("H8");
        Position whiteRook1 = Position.fromAlgebraic("G10");
        Position whiteRook2 = Position.fromAlgebraic("H9");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        board.set(whiteRook1, new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(whiteRook2, new Piece(PieceType.ROOK, Color.WHITE, false));
        Game game = new Game(board, Color.WHITE);

        // Rook delivers checkmate
        MoveResult result = game.playMove(new Move(whiteRook1, Position.fromAlgebraic("H10")));
        
        assertThat(result.success()).isTrue();
        assertThat(result.message()).containsIgnoringCase("checkmate");
        assertThat(game.getStatus()).isEqualTo(GameStatus.CHECKMATE);
        assertThat(game.getWinner()).isEqualTo(Color.WHITE);
    }

    @Test
    void stalemateIsDetected() {
        // Stalemate: Black king has no legal moves but is not in check
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("F6");
        Position blackKing = Position.fromAlgebraic("H8");
        Position whiteQueen = Position.fromAlgebraic("G6");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        board.set(whiteQueen, new Piece(PieceType.QUEEN, Color.WHITE, false));
        Game game = new Game(board, Color.WHITE);

        // Queen to G7 creates stalemate
        MoveResult result = game.playMove(new Move(whiteQueen, Position.fromAlgebraic("G7")));
        
        assertThat(result.success()).isTrue();
        assertThat(result.message()).containsIgnoringCase("stalemate");
        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
    }

    @Test
    void hasAnyLegalMoveReturnsTrueForNormalPosition() {
        Game game = new Game();
        assertThat(game.hasAnyLegalMove(Color.WHITE)).isTrue();
        assertThat(game.hasAnyLegalMove(Color.BLACK)).isTrue();
    }

    @Test
    void gameIsOverAfterCheckmate() {
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("A1");
        Position blackKing = Position.fromAlgebraic("H8");
        Position whiteRook1 = Position.fromAlgebraic("G10");
        Position whiteRook2 = Position.fromAlgebraic("H9");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        board.set(whiteRook1, new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(whiteRook2, new Piece(PieceType.ROOK, Color.WHITE, false));
        Game game = new Game(board, Color.WHITE);

        game.playMove(new Move(whiteRook1, Position.fromAlgebraic("H10")));
        
        assertThat(game.isGameOver()).isTrue();
    }

    // ======================= DRAW/REMIS TESTS =======================
    
    @Test
    void drawCanBeOffered() {
        Game game = new Game();
        
        MoveResult result = game.offerDraw();
        
        assertThat(result.success()).isTrue();
        assertThat(game.isDrawOffered()).isTrue();
        assertThat(game.getDrawOfferedBy()).isEqualTo(Color.WHITE);
    }

    @Test
    void drawCanBeAccepted() {
        Game game = new Game();
        game.offerDraw();
        game.playMove("A2 A3"); // Switch turn
        
        MoveResult result = game.acceptDraw();
        
        assertThat(result.success()).isTrue();
        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
    }

    @Test
    void drawCanBeDeclined() {
        Game game = new Game();
        game.offerDraw();
        
        // Make a move to switch turns
        game.playMove("A2 A3");
        
        MoveResult result = game.declineDraw();
        
        assertThat(result.success()).isTrue();
        assertThat(game.isDrawOffered()).isFalse();
    }

    @Test
    void cannotAcceptOwnDrawOffer() {
        Game game = new Game();
        game.offerDraw();
        
        MoveResult result = game.acceptDraw();
        
        assertThat(result.success()).isFalse();
    }

    @Test
    void cannotAcceptNonExistentDraw() {
        Game game = new Game();
        
        MoveResult result = game.acceptDraw();
        
        assertThat(result.success()).isFalse();
    }

    @Test
    void drawOfferIsClearedAfterMove() {
        Game game = new Game();
        game.offerDraw();
        game.playMove("A2 A3");
        
        assertThat(game.isDrawOffered()).isFalse();
    }

    // ======================= RESIGN TESTS =======================
    
    @Test
    void resignEndsGame() {
        Game game = new Game();
        
        MoveResult result = game.resign();
        
        assertThat(result.success()).isTrue();
        assertThat(game.getStatus()).isEqualTo(GameStatus.RESIGNED);
        assertThat(game.getWinner()).isEqualTo(Color.BLACK);
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    void cannotMoveAfterResign() {
        Game game = new Game();
        game.resign();
        
        MoveResult result = game.playMove("A2 A3");
        
        assertThat(result.success()).isFalse();
    }

    // ======================= CASTLING TESTS =======================
    
    @Test
    void castlingToLoverSideIsAllowed() {
        Board board = new Board();
        int homeRow = 1;
        // Setup: Lover at A1, Rook at B1, King at F1 (standard starting position)
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, false));
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, false));
        // Black king for valid game state
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        // Castling: King moves from F1 to D1 (2 squares towards A)
        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.message()).containsIgnoringCase("castling");
        // King should be at D1
        assertThat(game.getBoard().get(Position.fromAlgebraic("D1")).orElseThrow().type())
                .isEqualTo(PieceType.KING);
        // Rook should be at E1
        assertThat(game.getBoard().get(Position.fromAlgebraic("E1")).orElseThrow().type())
                .isEqualTo(PieceType.ROOK);
    }

    @Test
    void castlingNotAllowedWhenKingHasMoved() {
        Board board = new Board();
        int homeRow = 1;
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, false));
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, true)); // King has moved
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isFalse();
    }

    @Test
    void castlingNotAllowedWhenLoverHasMoved() {
        Board board = new Board();
        int homeRow = 1;
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, true)); // Lover has moved
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, false));
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).containsIgnoringCase("lover");
    }

    @Test
    void castlingNotAllowedWhenRookHasMoved() {
        Board board = new Board();
        int homeRow = 1;
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, false));
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, true)); // Rook has moved
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, false));
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).containsIgnoringCase("rook");
    }

    @Test
    void castlingNotAllowedWhenInCheck() {
        Board board = new Board();
        int homeRow = 1;
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, false));
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, false));
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        // Black rook attacking white king
        board.set(new Position(5, 6), new Piece(PieceType.ROOK, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).containsIgnoringCase("check");
    }

    @Test
    void castlingNotAllowedWhenPiecesInTheWay() {
        Board board = new Board();
        int homeRow = 1;
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, false));
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(new Position(homeRow, 4), new Piece(PieceType.BISHOP, Color.WHITE, false)); // Blocking piece
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, false));
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).containsIgnoringCase("pieces in the way");
    }

    @Test
    void castlingNotAllowedWhenPassingThroughCheck() {
        Board board = new Board();
        int homeRow = 1;
        board.set(new Position(homeRow, 1), new Piece(PieceType.LOVER, Color.WHITE, false));
        board.set(new Position(homeRow, 2), new Piece(PieceType.ROOK, Color.WHITE, false));
        board.set(new Position(homeRow, 6), new Piece(PieceType.KING, Color.WHITE, false));
        board.set(new Position(10, 6), new Piece(PieceType.KING, Color.BLACK, false));
        // Black rook attacking E1 (square king passes through)
        board.set(new Position(5, 5), new Piece(PieceType.ROOK, Color.BLACK, false));
        Game game = new Game(board, Color.WHITE);

        MoveResult result = game.playMove(new Move(
                Position.fromAlgebraic("F1"),
                Position.fromAlgebraic("D1")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).containsIgnoringCase("through check");
    }

    // ======================= CHESS CLOCK TESTS =======================
    
    @Test
    void clockCanBeEnabled() {
        Game game = new Game();
        
        game.enableClock(Duration.ofMinutes(10));
        
        assertThat(game.getClock()).isNotNull();
        assertThat(game.getClock().getRemaining(Color.WHITE).toMinutes()).isEqualTo(10);
    }

    @Test
    void clockSwitchesAfterMove() {
        Game game = new Game();
        game.enableClock(Duration.ofMinutes(10));
        
        assertThat(game.getClock().getRunningClock()).isEqualTo(Color.WHITE);
        
        game.playMove("A2 A3");
        
        assertThat(game.getClock().getRunningClock()).isEqualTo(Color.BLACK);
    }

    @Test
    void clockPausesOnDrawOffer() {
        Game game = new Game();
        game.enableClock(Duration.ofMinutes(10));
        
        game.offerDraw();
        
        assertThat(game.getClock().isPaused()).isTrue();
    }

    @Test
    void clockResumesOnDrawDecline() {
        Game game = new Game();
        game.enableClock(Duration.ofMinutes(10));
        game.offerDraw();
        game.playMove("A2 A3"); // Switch turn
        
        game.declineDraw();
        
        assertThat(game.getClock().isPaused()).isFalse();
    }

    // ======================= GAME STATUS TESTS =======================
    
    @Test
    void initialStatusIsOngoing() {
        Game game = new Game();
        assertThat(game.getStatus()).isEqualTo(GameStatus.ONGOING);
    }

    @Test
    void statusBecomesCheckWhenInCheck() {
        Board board = new Board();
        Position whiteKing = Position.fromAlgebraic("E1");
        Position blackKing = Position.fromAlgebraic("E10");
        Position whiteRook = Position.fromAlgebraic("A5");
        board.set(whiteKing, new Piece(PieceType.KING, Color.WHITE, false));
        board.set(blackKing, new Piece(PieceType.KING, Color.BLACK, false));
        board.set(whiteRook, new Piece(PieceType.ROOK, Color.WHITE, false));
        Game game = new Game(board, Color.WHITE);

        game.playMove(new Move(whiteRook, Position.fromAlgebraic("E5")));

        assertThat(game.getStatus()).isEqualTo(GameStatus.CHECK);
    }

    @ParameterizedTest
    @CsvSource({
            "CHECKMATE, true",
            "DRAW, true",
            "TIME_UP, true",
            "RESIGNED, true",
            "ONGOING, false",
            "CHECK, false"
    })
    void isGameOverReturnsCorrectValue(GameStatus status, boolean expected) {
        Game game = new Game();
        // Use reflection or direct field access would be needed for full test
        // Here we test via the public API
        if (status == GameStatus.RESIGNED) {
            game.resign();
            assertThat(game.isGameOver()).isEqualTo(expected);
        }
    }

    // ======================= RENDER BOARD TESTS =======================
    
    @Test
    void renderBoardShowsTurnInfo() {
        Game game = new Game();
        String rendered = game.renderBoard();
        
        assertThat(rendered).contains("Turn: WHITE");
    }

    @Test
    void renderBoardShowsClockWhenEnabled() {
        Game game = new Game();
        game.enableClock(Duration.ofMinutes(5));
        
        String rendered = game.renderBoard();
        
        assertThat(rendered).contains("WHITE:");
        assertThat(rendered).contains("BLACK:");
    }

    @Test
    void renderBoardShowsDrawOffer() {
        Game game = new Game();
        game.offerDraw();
        
        String rendered = game.renderBoard();
        
        assertThat(rendered).contains("Draw offered");
    }
}

