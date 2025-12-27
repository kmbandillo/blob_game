package blobgame;

import java.awt.Canvas;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
 * The GameTimer is a subclass of the AnimationTimer class. It must override the handle method.
 */

public class GameTimer extends AnimationTimer{


	private GraphicsContext gc;
	private Scene theScene;
	private Stage stage;
	private PlayerBlob playerBlob;
	private ArrayList<Food> food;
	private ArrayList<Enemy> enemy;
	private ArrayList<Powerups> powerups;
	private float time;
	private long lastPowerupSpawnNanoTime;
	public static final int MAX_NUM_ENEMIES = 10;
	public static final int MAX_NUM_FOODS = 50;
	public static final int MAX_NUM_POWERUPS = 10;
	private static final long POWERUP_SPAWN_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(10);

	GameTimer(GraphicsContext gc, Scene theScene, Stage stage){
		this.gc = gc;
		this.theScene = theScene;
		this.playerBlob = new PlayerBlob((800/2- (PlayerBlob.MAIN_WIDTH/2)),(800/2- PlayerBlob.MAIN_WIDTH/2));
		//instantiate the ArrayList of Enemy
		this.enemy = new ArrayList<Enemy>();
		this.food = new ArrayList<Food>();
		this.powerups = new ArrayList<Powerups>();
		//call the spawnEnemies method
		this.spawnEnemies();
		this.spawnFoods();
		this.lastPowerupSpawnNanoTime = System.nanoTime();
		//call method to handle mouse click event
		this.handleKeyPressEvent();
		this.time = 0;
	}
//
	@Override
	public void handle(long currentNanoTime) {

		Stopwatch stopwatch = new Stopwatch();
	    this.gc.clearRect(0, 0, GameStage.WINDOW_WIDTH,GameStage.WINDOW_HEIGHT);
	    stopwatch.start();
	    this.playerBlob.updatePowerupEffects(currentNanoTime);
	    this.playerBlob.checkCollisionsWithFood(playerBlob, food);
	    this.playerBlob.checkCollisionsWithEnemies(playerBlob, enemy, currentNanoTime);
	    this.playerBlob.checkCollisionsWithPowerUps(playerBlob, powerups, currentNanoTime);

	    this.spawnFoods();
	    this.spawnPowerupIfNeeded(currentNanoTime);
	    this.removeExpiredPowerups(currentNanoTime);

	    ArrayList<Enemy> copy = new ArrayList<>(this.enemy);
	    for (Enemy e : copy){
	        e.enemyCheckCollisionWithFood(this.food);
	        e.enemyCheckCollisionWithOthers(this.enemy);
	        if(e.isAlive() == false){
	            this.enemy.remove(e);
	        }
	    }

	    // makes the blobs move
	    this.moveEnemies();
	    this.playerBlob.move();

		//render the blob
		this.playerBlob.render(this.gc);

		//call the render enemies and render bullets methods
		//call the render enemies and render bullets methods
		this.renderEnemies();
		this.renderPowerups();
		this.renderFoods();
		this.drawScore();


		if (!this.playerBlob.isAlive()) {

			this.time = stopwatch.getElapsedTimeSeconds();
			System.out.println("Time spent on the game: " + this.time + " seconds");
			this.stop();
			this.drawGameOver();

		}

	}

	//method that will render/draw the enemies to the canvas
	private void renderFoods() {
		for (Food f : this.food){
			f.render(this.gc);
		}
	}

	//method that will render/draw the enemies to the canvas
	private void renderEnemies() {
		for (Enemy f : this.enemy){
			f.render(this.gc);
		}
	}

	private void renderPowerups() {
		for (Powerups p : this.powerups){
			p.render(this.gc);
		}
	}

	//method that will spawn/instantiate three enemies at a random x,y location
	private void spawnEnemies() {
	    Random r = new Random();
	    int maxX = GameStage.WINDOW_WIDTH;  // Maximum allowed x position for enemy blobs
	    int maxY = GameStage.WINDOW_HEIGHT;  // Maximum allowed y position for enemy blobs
	    int count = 0;
	    while (count < GameTimer.MAX_NUM_ENEMIES) {
	        // Generate random x and y positions for the enemy blob

	    	int x = r.nextInt(maxX);
	        int y = r.nextInt(maxY);
	        if (x < maxX && y < maxY){

	        // Check if the enemy blob collides with any other enemy blobs
		        boolean collides = false;
		        for (Enemy e : enemy) {
		            if (x < e.getX() + Enemy.INITIAL_ENEMY_WIDTH && x + Enemy.INITIAL_ENEMY_WIDTH > e.getX() &&
		                y < e.getY() + Enemy.INITIAL_ENEMY_WIDTH && y + Enemy.INITIAL_ENEMY_WIDTH > e.getY()) {
		                collides = true;
		                break;
		            }
		        }


	        // If the enemy blob does not collide with any other enemy blobs, add it to the enemy array list
	        if (!collides) {
	            this.enemy.add(new Enemy(x, y));
	            count++;
	        }
	        }
	    }
	}

	private void spawnPowerupIfNeeded(long currentNanoTime) {
	    if (currentNanoTime - this.lastPowerupSpawnNanoTime < POWERUP_SPAWN_INTERVAL_NANOS) {
	        return;
	    }
	    this.spawnPowerup(currentNanoTime);
	    this.lastPowerupSpawnNanoTime = currentNanoTime;
	}

	private void spawnPowerup(long spawnTimeNano) {
	    Random r = new Random();
	    int maxX = GameStage.WINDOW_WIDTH - Powerups.POWERUP_WIDTH;
	    int maxY = GameStage.WINDOW_HEIGHT - Powerups.POWERUP_WIDTH;
	    int x = r.nextInt(Math.max(1, maxX));
	    int y = r.nextInt(Math.max(1, maxY));
	    int type = r.nextBoolean() ? Powerups.SPEED_BOOST : Powerups.IMMUNITY;
	    this.powerups.add(new Powerups(x, y, type, spawnTimeNano));
	}

	private void removeExpiredPowerups(long currentNanoTime) {
	    ArrayList<Powerups> copy = new ArrayList<>(this.powerups);
	    for (Powerups powerup : copy) {
	        if (powerup.isExpired(currentNanoTime)) {
	            this.powerups.remove(powerup);
	        }
	    }
	}

	private void drawScore(){
		this.gc.setTextAlign(TextAlignment.LEFT);
		this.gc.setFill(Color.GREY);
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 20));
		this.gc.fillText("BLOBS EATEN: "+ PlayerBlob.getBlobEaten() , 10, 50);
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 20));
		this.gc.fillText("FOOD EATEN: " + PlayerBlob.getFoodEaten(), 10, 90);
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 20));
		this.gc.fillText("SIZE OF BLOB: " + playerBlob.getSize(), 10, 130);
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 20));
		this.gc.fillText("TIME ALIVE: " + this.time , 10, 160);
	}
	private void drawGameOver(){
		this.gc.clearRect(0, 0, GameStage.WINDOW_WIDTH,GameStage.WINDOW_HEIGHT);
		Image bg = new Image("images/gameover.png");
	    ImageView view = new ImageView(bg);
	    gc.drawImage(bg, 0,0);
	    gc.setTextAlign(TextAlignment.CENTER);
	    gc.setTextBaseline(VPos.CENTER);
	    gc.setFill(Color.BLACK);
	    gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 30));
//	    gc.fillText("GAME OVER!", GameStage.WINDOW_WIDTH / 2, GameStage.WINDOW_WIDTH/2 - 100);

	    gc.setTextAlign(TextAlignment.CENTER);
	    gc.setFill(Color.BLACK);
	    gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 20));
	    gc.fillText("BLOBS EATEN: " + PlayerBlob.getBlobEaten(), GameStage.WINDOW_WIDTH / 2, GameStage.WINDOW_WIDTH/2 - 60);
	    gc.fillText("FOOD EATEN: " + PlayerBlob.getFoodEaten(), GameStage.WINDOW_WIDTH / 2, GameStage.WINDOW_WIDTH/2 - 30);
	    gc.fillText("SIZE OF BLOB: " + playerBlob.getSize(), GameStage.WINDOW_WIDTH / 2, GameStage.WINDOW_WIDTH/2);
	    gc.fillText("TIME ALIVE: " + this.time , GameStage.WINDOW_WIDTH / 2, GameStage.WINDOW_WIDTH/2 + 30);

	}



	public void spawnFoods() {
	    Random r = new Random();
	    int maxX = GameStage.WINDOW_WIDTH - Food.FOOD_WIDTH;  // Maximum allowed x position for food
	    int maxY = GameStage.WINDOW_HEIGHT - Food.FOOD_WIDTH;  // Maximum allowed y position for food

	    while (Food.getCount() < GameTimer.MAX_NUM_FOODS) {
	        // x and y randomization
	        int x = r.nextInt(maxX);
	        int y = r.nextInt(maxY);

	        // Checks if the food collides with the player blob
	        int pX = playerBlob.getX();
	        int pY = playerBlob.getY();
	        int playerWidth = playerBlob.getWidth();
	        int playerHeight = playerBlob.getHeight();
	        if (x < pX + playerWidth && x + Food.FOOD_WIDTH > pX &&
	            y < pY + playerHeight && y + Food.FOOD_WIDTH > pY) {
	            continue;
	        }

	        // check if the food collides with any other foods
	        boolean collides = false;
	        for (Food f : food) {
	            if (x < f.getX() + Food.FOOD_WIDTH && x + Food.FOOD_WIDTH > f.getX() &&
	                y < f.getY() + Food.FOOD_WIDTH && y + Food.FOOD_WIDTH > f.getY()) {
	                collides = true;
	                break;
	            }
	        }

	        // if the food does not collide with the player blob or other foods, add it to the food array list
	        if (!collides) {
	            this.food.add(new Food(x, y));
	            Food.increaseCount();
	        }
	    }




	}

		//method that handle the key press events
		private void handleKeyPressEvent() {
			theScene.setOnKeyPressed(new EventHandler<KeyEvent>(){
				public void handle(KeyEvent e){
	            	KeyCode code = e.getCode();
	                moveplayerBlob(code);
				}

			});

			theScene.setOnKeyReleased(new EventHandler<KeyEvent>(){
			            public void handle(KeyEvent e){
			            	KeyCode code = e.getCode();
			                stopplayerBlob(code);
			            }
			        });
	    }

		// method that moves the blob depending on the key pressed
		private void moveplayerBlob(KeyCode ke) {
			if(ke==KeyCode.W) this.playerBlob.setDY(-this.playerBlob.getSpeed());
			if(ke==KeyCode.A) this.playerBlob.setDX(-this.playerBlob.getSpeed());
			if(ke==KeyCode.S) this.playerBlob.setDY(this.playerBlob.getSpeed());
			if(ke==KeyCode.D) this.playerBlob.setDX(this.playerBlob.getSpeed());
			System.out.println(ke+" key pressed.");
	   	}

		// method that will stop the blob's movement; set the blob's DX and DY to 0
		private void stopplayerBlob(KeyCode ke){
			this.playerBlob.setDX(0);
			this.playerBlob.setDY(0);
		}

	//method that will move the enemies
	private void moveEnemies(){
		/*
		 * loop through the enemies array list
		 * if a enemy is alive, move the enemy. Else, remove the enemy from the enemies array list.
		 */
		for(int i = 0; i < this.enemy.size(); i++){
			Enemy f = this.enemy.get(i);
			if(f.isAlive())
        		f.move(this.enemy);
        	else
        		this.enemy.remove(i);
		}
	}

	ArrayList<Food> getFoods(){
		return this.food;
	}

	ArrayList<Enemy> getEnemies(){
		return this.enemy;
	}

	}







