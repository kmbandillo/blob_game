package blobgame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Enemy extends Sprite {
	public static final int MOVEMENT_INTERVAL = 3;
	public static final int MAX_ENEMY_SPEED = 3;
	public final static Image ENEMY_IMAGE1 = new Image("images/dog1.png",Enemy.INITIAL_ENEMY_WIDTH,Enemy.INITIAL_ENEMY_WIDTH,false,false);
	public final static Image ENEMY_IMAGE2 = new Image("images/dog2.png",Enemy.INITIAL_ENEMY_WIDTH,Enemy.INITIAL_ENEMY_WIDTH,false,false);
	public final static Image ENEMY_IMAGE3 = new Image("images/dog3.png",Enemy.INITIAL_ENEMY_WIDTH,Enemy.INITIAL_ENEMY_WIDTH,false,false);
	public final static int INITIAL_ENEMY_WIDTH = 40;
	private int imageType;
	private boolean alive;
	private int size = (int)INITIAL_ENEMY_WIDTH;
	private long directionChange = 0;
	private int speed;

	Enemy(int x, int y){
		super(x,y);
		this.alive = true;
		this.speed = 50;
		Random r = new Random();
		int randomEnemy = r.nextInt(3);		// randomizes the image of the enemy blobs
		if (randomEnemy == 0){
			this.loadImage(Enemy.ENEMY_IMAGE1);
			this.imageType = 1;
		} else if (randomEnemy == 1){
			this.loadImage(Enemy.ENEMY_IMAGE2);
			this.imageType = 2;
		} else {
			this.loadImage(Enemy.ENEMY_IMAGE3);
			this.imageType = 3;
		}
		

	}

//	method that changes the direction and position of the enemy blobs every in random seconds
	void move(ArrayList<Enemy> enemy) {
	    // check if the enemy blob collides with any other enemy blobs
	    boolean collides = false;
	    Enemy other = null;
	    for (Enemy e : enemy) {
	        if (this != e && x < e.getX() + INITIAL_ENEMY_WIDTH && x + INITIAL_ENEMY_WIDTH > e.getX() &&
	            y < e.getY() + INITIAL_ENEMY_WIDTH && y + INITIAL_ENEMY_WIDTH > e.getY()) {
	            collides = true;
	            other = e;
	            break;
	        }
	    }

	    // If the enemy blob collides with another enemy blob, change its direction of movement to a random direction away from the other enemy blob
	    if (collides) {
	    	int newWidth = this.size*2;
	    	ImageView imageView = new ImageView(ENEMY_IMAGE1);
	    	imageView.setFitWidth(newWidth);
	    	imageView.setFitHeight(newWidth);

	        Random r = new Random();
	        int numSeconds = r.nextInt(5) + 1;  // Randomize number of seconds between 1 and 5
	        long currentNanoTime = System.nanoTime();
	        if (TimeUnit.NANOSECONDS.toSeconds(currentNanoTime - directionChange) > numSeconds) {
	        	int direction = r.nextInt(4);  // Random number from 0 to 3
	            if (x < other.getX()) {
	                // other enemy blob is to the left, move right or down
	                if (direction == 0) {
	                    // Move down
	                    if (y + INITIAL_ENEMY_WIDTH < GameStage.WINDOW_HEIGHT) y += speed;
	                } else {
	                    // Move right
	                    if (x + INITIAL_ENEMY_WIDTH < GameStage.WINDOW_WIDTH) x += speed;
	                }
	            } else {
	                // other enemy blob is to the right, move left or up
	                if (direction == 0) {
	                    // Move up
	                    if (y > 0) y -= speed;
	                } else {
	                    // Move left
	                    if (x > 0) x -= speed;
	                }
	            }
	            directionChange = currentNanoTime;

	        }
	    } else {
	        // If the enemy blob does not collide with any other enemy blobs, move it in a random direction
	        Random r = new Random();
	        int numSeconds = r.nextInt(3);  // Random number of seconds
	        long currentNanoTime = System.nanoTime();
	        if (TimeUnit.NANOSECONDS.toSeconds(currentNanoTime - directionChange) > numSeconds) {
	            // Determine which direction to move
	            int direction = r.nextInt(4);  // Random number between 0 and 3
	            if (direction == 0) {
	                // Move up
	                if (y > 0) y -= speed;
	            } else if (direction == 1) {
	                // Move right
	                if (x + INITIAL_ENEMY_WIDTH < GameStage.WINDOW_WIDTH) x += speed;
	            } else if (direction == 2) {
	                // Move down
	                if (y + INITIAL_ENEMY_WIDTH < GameStage.WINDOW_HEIGHT) y += speed;
	            } else {
	                // Move left
	                if (x > 0) x -= speed;
	            }
	            directionChange = currentNanoTime;
	        }
	    }
	}



	void enemyCheckCollisionWithFood(ArrayList<Food> foods) {
	    Iterator<Food> iterator = foods.iterator();
	    while (iterator.hasNext()) {
	        Food f = iterator.next();
	        if (collidesWith(f)) {
	            // increase size of enemy blob
	            increaseSize(10);
	            // updates the image of the enemy blob
	            Image newImage = new Image("images/dog"+this.imageType+".png", this.size, this.size, false, false);
	            this.loadImage(newImage);
	            // updates speed of the enemy blob
	            this.setSpeed();
	            // remove food from list
	            iterator.remove();
	            Food.decreaseCount();
	        }
	    }
	}


	void enemyCheckCollisionWithOthers(ArrayList<Enemy> enemy) {
	    ArrayList<Enemy> enemiesToRemove = new ArrayList<Enemy>();
	    for (Enemy e : enemy) {
	        for (Enemy otherEnemy : enemy) {
	            if (e != otherEnemy && e.collidesWith(otherEnemy) && otherEnemy.getSize() < e.getSize() ) {
	                // increase size of this enemy blob
	                e.increaseSize(otherEnemy.getSize());
	                Image newImage = new Image("images/dog"+e.getImageType()+".png", e.getSize(), e.getSize(), false, false);
	                e.loadImage(newImage);
	                this.setSpeed();
	                // add other enemy blob in the list of enemy to remove
	                enemiesToRemove.add(otherEnemy);
	            }
	        }
	    }
	    // remove all enemy blobs in the list
	    enemy.removeAll(enemiesToRemove);
	}

	// setters and getters
	public void setAlive(Boolean b){
    	this.alive = b;
    }


	public int getSpeed(){
		return speed;
	}

	public void setSpeed(){
		this.speed = 120/this.size;
	}


	public boolean isAlive(){
		if(this.alive) return true;
		return false;
	}
	public void increaseSize(int value){
    	this.size += value;
    }

	public int getWidth(){
		return this.size;
	}

	public int getHeight(){
		return this.size;
	}

	public void die(){
    	this.alive = false;
    }

	//getter

	public int getSize(){
		return this.size;
	}

	public int getImageType(){
		return this.imageType;
	}
}
