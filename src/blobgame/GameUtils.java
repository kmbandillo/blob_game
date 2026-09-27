package blobgame;

/**
 * Small pure-Java utilities used by the game logic.
 * Kept free of JavaFX so it can be safely compiled and tested in CI.
 */
public final class GameUtils {
    private GameUtils() {}

    /**
     * Calculate movement speed from a blob size using the game's formula.
     * Returns integer speed = max(1, floor(120 / size)) so the blob never immobilizes.
     * Size must be > 0.
     */
    public static int calcSpeed(int size) {
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        return Math.max(1, 120 / size);
    }
}
