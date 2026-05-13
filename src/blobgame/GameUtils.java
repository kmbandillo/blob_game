package blobgame;

/**
 * Small pure-Java utilities used by the game logic.
 * Kept free of JavaFX so it can be safely compiled and tested in CI.
 */
public final class GameUtils {
    private GameUtils() {}

    /**
     * Calculate movement speed from a blob size using the game's formula.
     * Returns integer speed = floor(120 / size). Size must be > 0.
     */
    public static int calcSpeed(int size) {
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        return 120 / size;
    }
}
