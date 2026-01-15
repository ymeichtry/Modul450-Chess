package com.example.chess;

import com.example.chess.game.Game;
import com.example.chess.game.MoveResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ChessApplicationTests {

    @Test
    void contextLoads() {
    }

//    @Test
//    void mainMethodRuns() {
//        // Test that main method can be called
//        ChessApplication.main(new String[]{});
//    }

    @Test
    void applicationCanBeInstantiated() {
        ChessApplication app = new ChessApplication();
        assertThat(app).isNotNull();
    }

    @Test
    void commandLineRunnerExitCommand() throws Exception {
        // Simulate user input
        String input = "exit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Goodbye!");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerQuitCommand() throws Exception {
        String input = "quit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Goodbye!");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerHelpCommand() throws Exception {
        String input = "help\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("MOVE NOTATION");
            assertThat(output).contains("SPECIAL MOVES");
            assertThat(output).contains("COMMANDS");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerStatusCommand() throws Exception {
        String input = "status\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Status:");
            assertThat(output).contains("Turn:");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerDrawCommand() throws Exception {
        String input = "draw\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Draw offered");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerAcceptCommand() throws Exception {
        String input = "accept\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("No draw has been offered");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerDeclineCommand() throws Exception {
        String input = "decline\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("No draw has been offered");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerResignCommand() throws Exception {
        String input = "resign\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("resigns");
            assertThat(output).contains("GAME OVER");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerClockCommand() throws Exception {
        String input = "clock\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Chess clock enabled: 10 minutes");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerClockWithMinutesCommand() throws Exception {
        String input = "clock 5\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Chess clock enabled: 5 minutes");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerClockInvalidMinutes() throws Exception {
        String input = "clock abc\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Invalid clock time");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerMakeMove() throws Exception {
        String input = "a2 a3\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Move accepted");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerInvalidMove() throws Exception {
        String input = "a2 a5\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Illegal move");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerEmptyInput() throws Exception {
        String input = "\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Goodbye!");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerStatusWithClock() throws Exception {
        String input = "clock\nstatus\nexit\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("Clock:");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void commandLineRunnerDrawAcceptFlow() throws Exception {
        String input = "draw\na2 a3\naccept\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
            
            ChessApplication app = new ChessApplication();
            app.runGame();
            
            String output = outputStream.toString();
            assertThat(output).contains("GAME OVER");
            assertThat(output).contains("DRAW");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}

