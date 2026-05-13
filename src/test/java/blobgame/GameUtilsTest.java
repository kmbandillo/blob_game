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
}
