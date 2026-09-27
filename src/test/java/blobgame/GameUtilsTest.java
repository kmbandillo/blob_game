package blobgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class GameUtilsTest {

    @Test
    public void calcSpeed_basic() {
        assertEquals(3, GameUtils.calcSpeed(40)); // 120/40 == 3
        assertEquals(6, GameUtils.calcSpeed(20));
    }

    @Test
    public void calcSpeed_invalid() {
        assertThrows(IllegalArgumentException.class, () -> GameUtils.calcSpeed(0));
    }

    @Test
    public void calcSpeed_largeSizeHasMinimumSpeed() {
        assertEquals(1, GameUtils.calcSpeed(120)); // 120/120 == 1
        assertEquals(1, GameUtils.calcSpeed(130)); // 120/130 floor is 0 -> clamped to 1
        assertEquals(1, GameUtils.calcSpeed(200)); // larger blobs keep minimum speed of 1
        assertEquals(1, GameUtils.calcSpeed(500));
    }
}
