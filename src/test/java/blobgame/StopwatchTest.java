package blobgame;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StopwatchTest {

    @Test
    public void testElapsedTimeIsNonNegative() throws Exception {
        Stopwatch sw = new Stopwatch();
        sw.start();
        float t1 = sw.getElapsedTimeSeconds();
        Thread.sleep(30);
        float t2 = sw.getElapsedTimeSeconds();
        assertTrue(t1 >= 0.0f, "elapsed time should be non-negative");
        assertTrue(t2 >= t1, "elapsed time should not decrease with time");
    }
}
