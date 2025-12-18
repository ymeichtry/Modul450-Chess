package com.example.chess;

import com.example.chess.game.Game;
import com.example.chess.game.MoveResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

import java.time.Duration;
import java.util.Scanner;

@SpringBootApplication
public class ChessApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ChessApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Game game = new Game();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║         MODULAR CHESS - 10x10 Edition        ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║ Commands:                                    ║");
            System.out.println("║   A2 A3     - Move piece from A2 to A3       ║");
            System.out.println("║   F1 D1     - Castling (King 2 squares left) ║");
            System.out.println("║   draw      - Offer a draw                   ║");
            System.out.println("║   accept    - Accept draw offer              ║");
            System.out.println("║   decline   - Decline draw offer             ║");
            System.out.println("║   resign    - Resign the game                ║");
            System.out.println("║   clock     - Enable 10-minute chess clock   ║");
            System.out.println("║   clock N   - Enable N-minute chess clock    ║");
            System.out.println("║   status    - Show game status               ║");
            System.out.println("║   help      - Show this help                 ║");
            System.out.println("║   exit      - Exit the game                  ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println();
            System.out.println(game.renderBoard());
            
            while (!game.isGameOver()) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim().toLowerCase();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                MoveResult result;
                
                switch (line) {
                    case "exit", "quit" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    case "help" -> {
                        printHelp();
                        continue;
                    }
                    case "status" -> {
                        System.out.println("Status: " + game.getStatus());
                        System.out.println("Turn: " + game.getActiveColor());
                        if (game.getClock() != null) {
                            System.out.println("Clock: " + game.getClock());
                        }
                        continue;
                    }
                    case "draw" -> result = game.offerDraw();
                    case "accept" -> result = game.acceptDraw();
                    case "decline" -> result = game.declineDraw();
                    case "resign" -> result = game.resign();
                    case "clock" -> {
                        game.enableClock(Duration.ofMinutes(10));
                        System.out.println("Chess clock enabled: 10 minutes per player");
                        System.out.println(game.renderBoard());
                        continue;
                    }
                    default -> {
                        // Check for "clock N" pattern
                        if (line.startsWith("clock ")) {
                            try {
                                int minutes = Integer.parseInt(line.substring(6).trim());
                                game.enableClock(Duration.ofMinutes(minutes));
                                System.out.println("Chess clock enabled: " + minutes + " minutes per player");
                                System.out.println(game.renderBoard());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid clock time. Use: clock N (where N is minutes)");
                            }
                            continue;
                        }
                        // Regular move
                        result = game.playMove(line);
                    }
                }
                
                System.out.println(result.message());
                if (result.success()) {
                    System.out.println(game.renderBoard());
                }
            }
            
            // Game over
            System.out.println();
            System.out.println("═══════════════════════════════════");
            System.out.println("           GAME OVER");
            System.out.println("  Status: " + game.getStatus());
            if (game.getWinner() != null) {
                System.out.println("  Winner: " + game.getWinner());
            }
            System.out.println("═══════════════════════════════════");
        }
    }
    
    private void printHelp() {
        System.out.println();
        System.out.println("MOVE NOTATION:");
        System.out.println("  Use algebraic notation: FROM TO");
        System.out.println("  Examples: A2 A3, B1 C4, E2-E4");
        System.out.println();
        System.out.println("SPECIAL MOVES:");
        System.out.println("  Castling: Move King 2 squares towards the Lover's side");
        System.out.println("            (Only allowed if King, Rook, and Lover haven't moved)");
        System.out.println("            Example: F1 D1 (for White)");
        System.out.println();
        System.out.println("COMMANDS:");
        System.out.println("  draw     - Offer a draw to your opponent");
        System.out.println("  accept   - Accept the opponent's draw offer");
        System.out.println("  decline  - Decline the draw offer");
        System.out.println("  resign   - Resign and concede the game");
        System.out.println("  clock    - Enable 10-minute chess clock");
        System.out.println("  clock N  - Enable N-minute chess clock");
        System.out.println("  status   - Display current game status");
        System.out.println("  exit     - Quit the game");
        System.out.println();
        System.out.println("MODIFIED RULES:");
        System.out.println("  - 10x10 board");
        System.out.println("  - Lover piece (moves like King, but doesn't count for check)");
        System.out.println("  - Knights move 3+1 instead of 2+1");
        System.out.println("  - Bishops limited to 6 squares");
        System.out.println("  - Extra Rook on each side");
        System.out.println("  - Castling only towards Lover's side");
        System.out.println();
    }
}

