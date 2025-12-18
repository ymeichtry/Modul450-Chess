package com.example.chess.game;

import com.example.chess.model.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ChessClockTest {

    private ChessClock clock;

    @BeforeEach
    void setUp() {
        clock = new ChessClock(Duration.ofMinutes(5));
    }

    @Test
    void initialTimeIsSetForBothPlayers() {
        assertThat(clock.getRemaining(Color.WHITE).toMinutes()).isEqualTo(5);
        assertThat(clock.getRemaining(Color.BLACK).toMinutes()).isEqualTo(5);
    }

    @Test
    void clockStartsPaused() {
        assertThat(clock.isPaused()).isTrue();
    }

    @Test
    void startingClockUnpausesIt() {
        clock.start(Color.WHITE);
        assertThat(clock.isPaused()).isFalse();
        assertThat(clock.getRunningClock()).isEqualTo(Color.WHITE);
    }

    @Test
    void pauseStopsTheClock() {
        clock.start(Color.WHITE);
        clock.pause();
        assertThat(clock.isPaused()).isTrue();
    }

    @Test
    void resumeRestartsTheClock() {
        clock.start(Color.WHITE);
        clock.pause();
        clock.resume();
        assertThat(clock.isPaused()).isFalse();
    }

    @Test
    void switchClockChangesActivePlayer() {
        clock.start(Color.WHITE);
        assertThat(clock.getRunningClock()).isEqualTo(Color.WHITE);
        
        clock.switchClock();
        assertThat(clock.getRunningClock()).isEqualTo(Color.BLACK);
    }

    @Test
    void formatTimeShowsMinutesAndSeconds() {
        ChessClock shortClock = new ChessClock(Duration.ofSeconds(90));
        assertThat(shortClock.formatTime(Color.WHITE)).isEqualTo("01:30");
    }

    @Test
    void formatTimeShowsZeroWhenTimeUp() {
        ChessClock zeroClock = new ChessClock(Duration.ZERO);
        assertThat(zeroClock.formatTime(Color.WHITE)).isEqualTo("00:00");
    }

    @Test
    void isTimeUpReturnsTrueWhenNoTimeLeft() {
        ChessClock zeroClock = new ChessClock(Duration.ZERO);
        assertThat(zeroClock.isTimeUp(Color.WHITE)).isTrue();
        assertThat(zeroClock.isTimeUp(Color.BLACK)).isTrue();
    }

    @Test
    void isTimeUpReturnsFalseWhenTimeRemains() {
        assertThat(clock.isTimeUp(Color.WHITE)).isFalse();
        assertThat(clock.isTimeUp(Color.BLACK)).isFalse();
    }

    @Test
    void toStringShowsBothPlayersTime() {
        String output = clock.toString();
        assertThat(output).contains("White:");
        assertThat(output).contains("Black:");
    }

    @Test
    void toStringShowsPausedWhenPaused() {
        String output = clock.toString();
        assertThat(output).contains("PAUSED");
    }

    @Test
    void toStringShowsActivePlayerWhenRunning() {
        clock.start(Color.WHITE);
        String output = clock.toString();
        assertThat(output).contains("WHITE");
    }
}
