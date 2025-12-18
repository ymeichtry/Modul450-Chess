package com.example.chess.game;

import com.example.chess.model.Color;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/**
 * A chess clock that tracks remaining time for both players.
 * Time counts down when a player's clock is running.
 */
public class ChessClock {
    private final Map<Color, Long> remainingMillis = new EnumMap<>(Color.class);
    private Color runningClock;
    private long lastTickTime;
    private boolean paused = true;

    /**
     * Creates a new chess clock with the specified time per player.
     *
     * @param timePerPlayer the time each player has
     */
    public ChessClock(Duration timePerPlayer) {
        long millis = timePerPlayer.toMillis();
        remainingMillis.put(Color.WHITE, millis);
        remainingMillis.put(Color.BLACK, millis);
        runningClock = Color.WHITE;
    }

    /**
     * Starts the clock for the given color.
     */
    public void start(Color color) {
        updateTime();
        this.runningClock = color;
        this.paused = false;
        this.lastTickTime = System.currentTimeMillis();
    }

    /**
     * Pauses the clock (e.g., for draw offers).
     */
    public void pause() {
        updateTime();
        this.paused = true;
    }

    /**
     * Resumes the clock.
     */
    public void resume() {
        if (paused) {
            this.lastTickTime = System.currentTimeMillis();
            this.paused = false;
        }
    }

    /**
     * Switches the clock to the other player after a move.
     */
    public void switchClock() {
        updateTime();
        this.runningClock = runningClock.opposite();
        this.lastTickTime = System.currentTimeMillis();
    }

    /**
     * Gets the remaining time for a color.
     */
    public Duration getRemaining(Color color) {
        updateTime();
        return Duration.ofMillis(remainingMillis.get(color));
    }

    /**
     * Checks if a player has run out of time.
     */
    public boolean isTimeUp(Color color) {
        updateTime();
        return remainingMillis.get(color) <= 0;
    }

    /**
     * Gets which player's clock is currently running.
     */
    public Color getRunningClock() {
        return runningClock;
    }

    /**
     * Checks if the clock is paused.
     */
    public boolean isPaused() {
        return paused;
    }

    private void updateTime() {
        if (paused || runningClock == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickTime;
        long current = remainingMillis.get(runningClock);
        remainingMillis.put(runningClock, Math.max(0, current - elapsed));
        lastTickTime = now;
    }

    /**
     * Formats remaining time as MM:SS.
     */
    public String formatTime(Color color) {
        Duration remaining = getRemaining(color);
        long totalSeconds = remaining.toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format("White: %s | Black: %s%s",
                formatTime(Color.WHITE),
                formatTime(Color.BLACK),
                paused ? " (PAUSED)" : " ← " + runningClock);
    }
}
