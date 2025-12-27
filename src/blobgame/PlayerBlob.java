package blobgame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;



public class PlayerBlob extends Sprite{
	private String name;
	private boolean alive;
	private int size;
	private int speed;
	private int baseSpeed;
	private long speedBoostEndsAtNanoTime;
	private long immunityEndsAtNanoTime;
	private static int foodEaten = 0;
	private static int blobsEaten = 0;

	public final static Image MAIN_IMAGE = new Image("images/anya.png",PlayerBlob.MAIN_WIDTH,PlayerBlob.MAIN_WIDTH,false,false);
	final static int MAIN_WIDTH = 40;

	public PlayerBlob(int x, int y){
		super(x,y);
		this.alive = true;
		this.size = (int) MAIN_IMAGE.getWidth();
		this.baseSpeed = 120/size;
		this.speed = this.baseSpeed;
		this.loadImage(PlayerBlob.MAIN_IMAGE);
	}


	// method to check if there is a collision between the food and the player blob
	void checkCollisionsWithFood(PlayerBlob playerBlob, ArrayList<Food> food) {
	    Iterator<Food> it = food.iterator();
	    while (it.hasNext()) {
	        Food f = it.next();
	        if (playerBlob.collidesWith(f)) {
	            // increase size of player blob
	        	playerBlob.increaseSize(10);
	        	// updates the food eaten by the player blob
	        	this.setFoodEaten();
	            // updates the speed
	            this.setSpeed();
	            //updates player blob's image
	            Image newImage = new Image("images/anya.png", this.size, this.size, false, false);
	            playerBlob.loadImage(newImage);
	            // remove food from list
	            it.remove();
	            Food.decreaseCount();
	        }
	    }
	}

	// method to check if there is a collision between the powerup and the player blob
	void checkCollisionsWithPowerUps(PlayerBlob playerBlob, ArrayList<Powerups> powerups, long currentNanoTime) {
	    Iterator<Powerups> it = powerups.iterator();
	    while (it.hasNext()) {
	        Powerups p = it.next();
	        if (playerBlob.collidesWith(p)) {
	            if (p.getType() == Powerups.SPEED_BOOST) {
	        		this.activateSpeedBoost(currentNanoTime);
	            } else if (p.getType() == Powerups.IMMUNITY) {
	        		this.activateImmunity(currentNanoTime);
	            }
	            // remove food from list
	            it.remove();
	        }
	    }
	}

	void checkCollisionsWithEnemies(PlayerBlob playerBlob, ArrayList<Enemy> enemy, long currentNanoTime) {
	    if (this.isImmune(currentNanoTime)) {
	        return;
	    }
	    Iterator<Enemy> it = enemy.iterator();
	    while (it.hasNext()) {
	        Enemy e = it.next();
	        if (playerBlob.collidesWith(e)) {
	            // increase size of player blob
	            playerBlob.increaseSize(10);
	            //update player blob's image
	            if (this.size>=e.getWidth()){
		            Image newImage = new Image("images/anya.png", this.size, this.size, false, false);
		            playerBlob.loadImage(newImage);
		            this.setBlobEaten();
		            this.setSpeed();
		            it.remove();
	            } else { // the enemyblob image will be updated
	            	Image newImage = new Image("images/dog"+e.getImageType()+".png", e.getSize(), e.getSize(), false, false);
		            e.loadImage(newImage);
		            this.alive = false;
		        }
	        }
	    }
	}

	// getters and setters
	public boolean isAlive(){
		if(this.alive) return true;
		return false;
	}
	public String getName(){
		return this.name;
	}

	public int getWidth(){
		return this.size;
	}

	public int getSize(){
		return this.size;
	}

	public int getHeight(){
		return this.size;
	}

	public void die(){
    	this.alive = false;
    }

	public void increaseSize(int value){
    	this.size += value;
    }

	public int getSpeed(){
		return speed;
	}

	public void setSpeed(){
		this.speed = 120/this.size;
	}

	public void setDoubleSpeed(){
		this.speed = this.baseSpeed * 2;
	}

	public void activateSpeedBoost(long currentNanoTime) {
		this.speedBoostEndsAtNanoTime = currentNanoTime + TimeUnit.SECONDS.toNanos(5);
		this.speed = this.baseSpeed * 2;
	}

	public void activateImmunity(long currentNanoTime) {
		this.immunityEndsAtNanoTime = currentNanoTime + TimeUnit.SECONDS.toNanos(5);
	}

	public void updatePowerupEffects(long currentNanoTime) {
		if (this.speedBoostEndsAtNanoTime > 0 && currentNanoTime >= this.speedBoostEndsAtNanoTime) {
			this.speedBoostEndsAtNanoTime = 0;
			this.speed = this.baseSpeed;
		}
	}

	public boolean isImmune(long currentNanoTime) {
		return this.immunityEndsAtNanoTime > 0 && currentNanoTime < this.immunityEndsAtNanoTime;
	}

	@Override
	void render(GraphicsContext gc) {
		if (this.isImmune(System.nanoTime())) {
			gc.setStroke(Color.GOLD);
			gc.setLineWidth(3);
			gc.strokeOval(this.x - 3, this.y - 3, this.width + 6, this.height + 6);
		}
		super.render(gc);
	}


	public static void setFoodEaten() {
		foodEaten++;
	}

	public static void setBlobEaten() {
		blobsEaten++;
	}

    public static int getFoodEaten(){
        return foodEaten;
    }
    public static int getBlobEaten(){
        return blobsEaten;
    }

	public void move() {
		if(this.x+this.dx <= GameStage.WINDOW_WIDTH-this.width && this.x+this.dx >=0 && this.y+this.dy <= GameStage.WINDOW_HEIGHT-this.width && this.y+this.dy >=0){
    		this.x += this.dx;
    		this.y += this.dy;
    	}
	}



}
