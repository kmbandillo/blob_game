package blobgame;

import java.util.Timer;

import javafx.scene.image.Image;

public class Powerups extends Sprite{
    private Timer timer;
    private int type;
    public final static int POWERUP_WIDTH = 20;
	public final static Image POWERUP_IMAGE1 = new Image("images/peanut.png",POWERUP_WIDTH , POWERUP_WIDTH ,false,false);

    public Powerups(int x, int y) {
        super(x, y);
        this.timer = new Timer();
        this.type = 1;
        this.loadImage(POWERUP_IMAGE1);

    }








}

