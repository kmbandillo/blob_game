package blobgame;

import javafx.scene.image.Image;

public class Powerups extends Sprite{
    public static final int SPEED_BOOST = 1;
    public static final int IMMUNITY = 2;
    public final static int POWERUP_WIDTH = 20;
    public final static Image POWERUP_IMAGE1 = new Image("images/hotchoco.png",POWERUP_WIDTH , POWERUP_WIDTH ,false,false);
    public final static Image POWERUP_IMAGE2 = new Image("images/peanut.png",POWERUP_WIDTH , POWERUP_WIDTH ,false,false);

    private final int type;
    private long spawnTimeNano;

    public Powerups(int x, int y, int type, long spawnTimeNano) {
        super(x, y);
        this.type = type;
        this.spawnTimeNano = spawnTimeNano;
        if (type == IMMUNITY) {
            this.loadImage(POWERUP_IMAGE2);
        } else {
            this.loadImage(POWERUP_IMAGE1);
        }
    }

    public int getType() {
        return this.type;
    }

    public void adjustPauseTime(long pauseDurationNano) {
        this.spawnTimeNano += pauseDurationNano;
    }

    public boolean isExpired(long currentNanoTime) {
        return (currentNanoTime - this.spawnTimeNano) >= java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
    }

}

