package blobgame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class GameTimer extends AnimationTimer {
	private GraphicsContext gc;
	private Scene theScene;
	private Stage stage;
	private PlayerBlob playerBlob;
	private ArrayList<Food> food;
	private ArrayList<Enemy> enemy;
	private ArrayList<Powerups> powerups;
	private float time;
	private long gameStartTimeMillis;
	private long lastPowerupSpawnNanoTime;
	private Set<KeyCode> activeKeys = new HashSet<KeyCode>();
	private Image bgTile;
	private GameStage gameStage;
	private boolean isPaused = false;
	private boolean isGameOver = false;
	private long pauseStartTimeMillis = 0;
	private long pauseStartNanoTime = 0;

	public static final int MAX_NUM_ENEMIES = 10;
	public static final int MAX_NUM_FOODS = 50;
	private static final long POWERUP_SPAWN_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(10);

	public GameTimer(GraphicsContext gc, Scene theScene, Stage stage) {
		this(gc, theScene, stage, null);
	}

	public GameTimer(GraphicsContext gc, Scene theScene, Stage stage, GameStage gameStage) {
		this.gc = gc;
		this.theScene = theScene;
		this.stage = stage;
		this.gameStage = gameStage;
		this.bgTile = new Image("images/back.png");

		PlayerBlob.resetStats();
		Food.resetCount();

		int startX = GameStage.MAP_WIDTH / 2 - PlayerBlob.MAIN_WIDTH / 2;
		int startY = GameStage.MAP_HEIGHT / 2 - PlayerBlob.MAIN_WIDTH / 2;
		this.playerBlob = new PlayerBlob(startX, startY);

		this.enemy = new ArrayList<Enemy>();
		this.food = new ArrayList<Food>();
		this.powerups = new ArrayList<Powerups>();

		this.spawnEnemies();
		this.spawnFoods();

		this.lastPowerupSpawnNanoTime = System.nanoTime();
		this.gameStartTimeMillis = System.currentTimeMillis();
		this.time = 0;

		this.handleKeyPressEvent();
	}

	@Override
	public void start() {
		this.isGameOver = false;
		this.isPaused = false;
		this.pauseStartTimeMillis = 0;
		this.pauseStartNanoTime = 0;
		this.gameStartTimeMillis = System.currentTimeMillis();
		this.lastPowerupSpawnNanoTime = System.nanoTime();
		if (this.gameStage != null) {
			this.gameStage.showGameplayButton();
		}
		super.start();
	}

	public void pauseGame() {
		if (this.isGameOver || this.playerBlob == null || !this.playerBlob.isAlive()) {
			return;
		}
		this.isPaused = true;
		this.pauseStartTimeMillis = System.currentTimeMillis();
		this.pauseStartNanoTime = System.nanoTime();
		this.activeKeys.clear();
		this.playerBlob.setDX(0);
		this.playerBlob.setDY(0);
		this.stop();
	}

	public void resumeGame() {
		if (this.isGameOver || this.playerBlob == null || !this.playerBlob.isAlive()) {
			return;
		}
		if (this.isPaused && this.pauseStartTimeMillis > 0) {
			long pausedDurationMillis = System.currentTimeMillis() - this.pauseStartTimeMillis;
			long pausedDurationNano = System.nanoTime() - this.pauseStartNanoTime;

			this.gameStartTimeMillis += pausedDurationMillis;
			this.lastPowerupSpawnNanoTime += pausedDurationNano;

			this.playerBlob.adjustPauseTime(pausedDurationNano);
			for (Powerups p : this.powerups) {
				p.adjustPauseTime(pausedDurationNano);
			}

			this.pauseStartTimeMillis = 0;
			this.pauseStartNanoTime = 0;
			this.isPaused = false;
		}

		this.activeKeys.clear();
		this.playerBlob.setDX(0);
		this.playerBlob.setDY(0);

		if (this.gameStage != null) {
			this.gameStage.showGameplayButton();
		}
		super.start();
	}

	public boolean canContinue() {
		return !this.isGameOver && this.playerBlob != null && this.playerBlob.isAlive();
	}

	@Override
	public void handle(long currentNanoTime) {
		this.time = (System.currentTimeMillis() - this.gameStartTimeMillis) / 1000.0f;

		// Update player states and power-ups
		this.playerBlob.updatePowerupEffects(currentNanoTime);
		this.playerBlob.checkCollisionsWithFood(this.playerBlob, this.food);
		this.playerBlob.checkCollisionsWithEnemies(this.playerBlob, this.enemy, currentNanoTime);
		this.playerBlob.checkCollisionsWithPowerUps(this.playerBlob, this.powerups, currentNanoTime);

		// Maintain 50 food items on the map
		this.spawnFoods();

		// Calculate camera position based on player center
		double playerCenterX = this.playerBlob.getCenterX();
		double playerCenterY = this.playerBlob.getCenterY();
		double camX = Math.max(0, Math.min(playerCenterX - GameStage.WINDOW_WIDTH / 2.0, GameStage.MAP_WIDTH - GameStage.WINDOW_WIDTH));
		double camY = Math.max(0, Math.min(playerCenterY - GameStage.WINDOW_HEIGHT / 2.0, GameStage.MAP_HEIGHT - GameStage.WINDOW_HEIGHT));

		// Power-up management (spawn in current visible window, remove expired after 5s)
		this.spawnPowerupIfNeeded(currentNanoTime, camX, camY);
		this.removeExpiredPowerups(currentNanoTime);

		// Enemy collisions with food and each other
		ArrayList<Enemy> copy = new ArrayList<>(this.enemy);
		for (Enemy e : copy) {
			if (e.isAlive()) {
				e.enemyCheckCollisionWithFood(this.food);
			}
		}
		if (!this.enemy.isEmpty()) {
			this.enemy.get(0).enemyCheckCollisionWithOthers(this.enemy);
		}

		// Move player and enemies
		this.updatePlayerMovement();
		this.playerBlob.move();
		this.moveEnemies();

		// Check if player has died
		if (!this.playerBlob.isAlive()) {
			this.isGameOver = true;
			this.isPaused = false;
			this.stop();
			this.drawGameOver();
			return;
		}

		// Clear canvas and draw map with camera offset
		this.gc.clearRect(0, 0, GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
		this.drawBackground(camX, camY);

		// Render entities with camera viewport translation
		this.renderFoods(camX, camY);
		this.renderPowerups(camX, camY);
		this.renderEnemies(camX, camY);
		this.playerBlob.render(this.gc, camX, camY);

		// Draw top status bar / HUD
		this.drawScore();
	}

	private void drawBackground(double camX, double camY) {
		int tileW = 800;
		int tileH = 800;
		int startX = ((int) camX / tileW) * tileW;
		int startY = ((int) camY / tileH) * tileH;
		for (int x = startX; x <= camX + GameStage.WINDOW_WIDTH; x += tileW) {
			for (int y = startY; y <= camY + GameStage.WINDOW_HEIGHT; y += tileH) {
				this.gc.drawImage(this.bgTile, x - camX, y - camY);
			}
		}

		// Draw red boundary walls of the 2400x2400 map
		this.gc.setStroke(Color.RED);
		this.gc.setLineWidth(6);
		this.gc.strokeRect(0 - camX, 0 - camY, GameStage.MAP_WIDTH, GameStage.MAP_HEIGHT);
	}

	private void renderFoods(double camX, double camY) {
		for (Food f : this.food) {
			f.render(this.gc, camX, camY);
		}
	}

	private void renderEnemies(double camX, double camY) {
		for (Enemy e : this.enemy) {
			if (e.isAlive()) {
				e.render(this.gc, camX, camY);
			}
		}
	}

	private void renderPowerups(double camX, double camY) {
		for (Powerups p : this.powerups) {
			p.render(this.gc, camX, camY);
		}
	}

	private void spawnEnemies() {
		Random r = new Random();
		int maxX = GameStage.MAP_WIDTH - Enemy.INITIAL_ENEMY_WIDTH;
		int maxY = GameStage.MAP_HEIGHT - Enemy.INITIAL_ENEMY_WIDTH;

		while (this.enemy.size() < GameTimer.MAX_NUM_ENEMIES) {
			int x = r.nextInt(Math.max(1, maxX));
			int y = r.nextInt(Math.max(1, maxY));

			// Ensure safe distance from player center spawn
			double distToPlayer = Math.hypot(x - this.playerBlob.getX(), y - this.playerBlob.getY());
			if (distToPlayer < 300) {
				continue;
			}

			this.enemy.add(new Enemy(x, y));
		}
	}

	public void spawnFoods() {
		Random r = new Random();
		int maxX = GameStage.MAP_WIDTH - Food.FOOD_WIDTH;
		int maxY = GameStage.MAP_HEIGHT - Food.FOOD_WIDTH;

		while (this.food.size() < GameTimer.MAX_NUM_FOODS) {
			int x = r.nextInt(Math.max(1, maxX));
			int y = r.nextInt(Math.max(1, maxY));

			this.food.add(new Food(x, y));
			Food.increaseCount();
		}
	}

	private void spawnPowerupIfNeeded(long currentNanoTime, double camX, double camY) {
		if (currentNanoTime - this.lastPowerupSpawnNanoTime < POWERUP_SPAWN_INTERVAL_NANOS) {
			return;
		}
		this.spawnPowerup(currentNanoTime, camX, camY);
		this.lastPowerupSpawnNanoTime = currentNanoTime;
	}

	private void spawnPowerup(long spawnTimeNano, double camX, double camY) {
		Random r = new Random();
		// Spawn within the player's currently visible window
		int minX = (int) camX + 40;
		int maxX = (int) (camX + GameStage.WINDOW_WIDTH - Powerups.POWERUP_WIDTH - 40);
		int minY = (int) camY + 60; // below top HUD
		int maxY = (int) (camY + GameStage.WINDOW_HEIGHT - Powerups.POWERUP_WIDTH - 40);

		minX = Math.max(0, minX);
		maxX = Math.min(GameStage.MAP_WIDTH - Powerups.POWERUP_WIDTH, maxX);
		minY = Math.max(0, minY);
		maxY = Math.min(GameStage.MAP_HEIGHT - Powerups.POWERUP_WIDTH, maxY);

		int x = minX + (maxX > minX ? r.nextInt(maxX - minX + 1) : 0);
		int y = minY + (maxY > minY ? r.nextInt(maxY - minY + 1) : 0);

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

	// Status Bar / HUD displayed at the top of the screen
	private void drawScore() {
		this.gc.setFill(Color.rgb(0, 0, 0, 0.7));
		this.gc.fillRect(0, 0, GameStage.WINDOW_WIDTH, 44);

		this.gc.setTextAlign(TextAlignment.CENTER);
		this.gc.setTextBaseline(VPos.CENTER);
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 14));
		this.gc.setFill(Color.WHITE);

		int colW = 705 / 4;
		int textY = 22;

		this.gc.fillText("FOOD EATEN: " + PlayerBlob.getFoodEaten(), colW * 0 + colW / 2, textY);
		this.gc.fillText("BLOBS EATEN: " + PlayerBlob.getBlobEaten(), colW * 1 + colW / 2, textY);
		this.gc.fillText("CURRENT SIZE: " + this.playerBlob.getSize(), colW * 2 + colW / 2, textY);
		this.gc.fillText(String.format("TIME ALIVE: %.1fs", this.time), colW * 3 + colW / 2, textY);

		// Render Active Power-Up Countdown Badges
		long now = System.nanoTime();
		double speedRemaining = this.playerBlob.getSpeedBoostRemainingSeconds(now);
		double immunityRemaining = this.playerBlob.getImmunityRemainingSeconds(now);

		double badgeY = 52;
		double centerX = GameStage.WINDOW_WIDTH / 2.0;

		if (speedRemaining > 0 && immunityRemaining > 0) {
			drawPowerupBadge(this.gc, centerX - 105, badgeY, "⚡ SPEED BOOST: " + String.format("%.1fs", speedRemaining), Color.rgb(0, 220, 255));
			drawPowerupBadge(this.gc, centerX + 105, badgeY, "🛡 IMMUNITY: " + String.format("%.1fs", immunityRemaining), Color.rgb(255, 215, 0));
		} else if (speedRemaining > 0) {
			drawPowerupBadge(this.gc, centerX, badgeY, "⚡ SPEED BOOST: " + String.format("%.1fs", speedRemaining), Color.rgb(0, 220, 255));
		} else if (immunityRemaining > 0) {
			drawPowerupBadge(this.gc, centerX, badgeY, "🛡 IMMUNITY: " + String.format("%.1fs", immunityRemaining), Color.rgb(255, 215, 0));
		}

		// Render Power-up Pickup Announcement Banner (visible for 2s after eating)
		double announceRemaining = this.playerBlob.getLastPowerupAnnouncementRemaining(now);
		if (announceRemaining > 0) {
			drawPowerupAnnouncement(this.gc, this.playerBlob.getLastPowerupAnnouncement(), this.playerBlob.getLastPowerupColor(), announceRemaining / 2.0);
		}
	}

	private void drawPowerupBadge(GraphicsContext gc, double x, double y, String text, Color accentColor) {
		double width = 195;
		double height = 26;
		double startX = x - width / 2.0;

		gc.setFill(Color.rgb(20, 20, 20, 0.8));
		gc.fillRoundRect(startX, y, width, height, 20, 20);

		gc.setStroke(accentColor);
		gc.setLineWidth(1.8);
		gc.strokeRoundRect(startX, y, width, height, 20, 20);

		gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 12));
		gc.setFill(accentColor);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		gc.fillText(text, x, y + height / 2.0);
	}

	private void drawPowerupAnnouncement(GraphicsContext gc, String text, Color color, double remainingRatio) {
		double width = 360;
		double height = 30;
		double x = GameStage.WINDOW_WIDTH / 2.0;
		double y = 86;
		double startX = x - width / 2.0;
		double opacity = Math.max(0.0, Math.min(1.0, remainingRatio));

		Color bgColor = Color.rgb(15, 15, 15, 0.85 * opacity);
		gc.setFill(bgColor);
		gc.fillRoundRect(startX, y, width, height, 24, 24);

		Color strokeColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.95 * opacity);
		gc.setStroke(strokeColor);
		gc.setLineWidth(2);
		gc.strokeRoundRect(startX, y, width, height, 24, 24);

		gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 14));
		gc.setFill(strokeColor);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		gc.fillText(text, x, y + height / 2.0);
	}

	private void drawGameOver() {
		this.gc.clearRect(0, 0, GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
		try {
			Image bg = new Image("images/extras_bg.png");
			this.gc.drawImage(bg, 0, 0);
		} catch (Exception e) {}

		double centerX = GameStage.WINDOW_WIDTH / 2.0;

		this.gc.setTextAlign(TextAlignment.CENTER);
		this.gc.setTextBaseline(VPos.CENTER);

		// Render GAME OVER title
		this.gc.setFill(Color.BLACK);
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 46));
		this.gc.fillText("GAME OVER", centerX, 265);

		// Render Stats
		this.gc.setFill(Color.rgb(45, 38, 35));
		this.gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 22));

		this.gc.fillText("FOOD EATEN: " + PlayerBlob.getFoodEaten(), centerX, 355);
		this.gc.fillText("BLOBS EATEN: " + PlayerBlob.getBlobEaten(), centerX, 400);
		this.gc.fillText("CURRENT SIZE: " + this.playerBlob.getSize(), centerX, 445);
		this.gc.fillText(String.format("TIME ALIVE: %.1fs", this.time), centerX, 490);

		if (this.gameStage != null) {
			this.gameStage.showGameOverButton();
		}
	}

	private void handleKeyPressEvent() {
		this.theScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (e.getCode() == KeyCode.SPACE) {
					playerBlob.split();
				} else {
					movePlayerBlob(e.getCode());
				}
			}
		});

		this.theScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (e.getCode() != KeyCode.SPACE) {
					stopPlayerBlob(e.getCode());
				}
			}
		});
	}

	private void updatePlayerMovement() {
		int dx = 0;
		int dy = 0;
		int currentSpeed = this.playerBlob.getSpeed();

		if (this.activeKeys.contains(KeyCode.W) || this.activeKeys.contains(KeyCode.UP)) {
			dy -= currentSpeed;
		}
		if (this.activeKeys.contains(KeyCode.S) || this.activeKeys.contains(KeyCode.DOWN)) {
			dy += currentSpeed;
		}
		if (this.activeKeys.contains(KeyCode.A) || this.activeKeys.contains(KeyCode.LEFT)) {
			dx -= currentSpeed;
		}
		if (this.activeKeys.contains(KeyCode.D) || this.activeKeys.contains(KeyCode.RIGHT)) {
			dx += currentSpeed;
		}

		this.playerBlob.setDX(dx);
		this.playerBlob.setDY(dy);
	}

	private void movePlayerBlob(KeyCode ke) {
		this.activeKeys.add(ke);
		this.updatePlayerMovement();
	}

	private void stopPlayerBlob(KeyCode ke) {
		this.activeKeys.remove(ke);
		this.updatePlayerMovement();
	}

	private void moveEnemies() {
		for (int i = 0; i < this.enemy.size(); i++) {
			Enemy e = this.enemy.get(i);
			if (e.isAlive()) {
				e.move(this.enemy);
			} else {
				this.enemy.remove(i);
				i--;
			}
		}
	}

	public ArrayList<Food> getFoods() {
		return this.food;
	}

	public ArrayList<Enemy> getEnemies() {
		return this.enemy;
	}
}

