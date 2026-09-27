package blobgame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Enemy extends Sprite {
	public final static int INITIAL_ENEMY_WIDTH = 40;
	public final static Image ENEMY_IMAGE1 = new Image("images/dog1.png", INITIAL_ENEMY_WIDTH, INITIAL_ENEMY_WIDTH, false, false);
	public final static Image ENEMY_IMAGE2 = new Image("images/dog2.png", INITIAL_ENEMY_WIDTH, INITIAL_ENEMY_WIDTH, false, false);
	public final static Image ENEMY_IMAGE3 = new Image("images/dog3.png", INITIAL_ENEMY_WIDTH, INITIAL_ENEMY_WIDTH, false, false);

	private int imageType;
	private boolean alive;
	private int size = INITIAL_ENEMY_WIDTH;
	private int speed;
	private double currentDX = 0;
	private double currentDY = 0;
	private long directionEndsAtNanoTime = 0;

	public Enemy(int x, int y) {
		super(x, y);
		this.alive = true;
		this.size = INITIAL_ENEMY_WIDTH;
		this.speed = Math.max(1, 120 / this.size);

		Random r = new Random();
		int randomEnemy = r.nextInt(3);
		if (randomEnemy == 0) {
			this.loadImage(Enemy.ENEMY_IMAGE1);
			this.imageType = 1;
		} else if (randomEnemy == 1) {
			this.loadImage(Enemy.ENEMY_IMAGE2);
			this.imageType = 2;
		} else {
			this.loadImage(Enemy.ENEMY_IMAGE3);
			this.imageType = 3;
		}
	}

	// Roam in random directions for a random duration (in seconds)
	public void move(ArrayList<Enemy> enemies) {
		long now = System.nanoTime();
		if (now >= this.directionEndsAtNanoTime) {
			Random r = new Random();
			double angle = r.nextDouble() * 2 * Math.PI;
			this.currentDX = Math.cos(angle) * this.speed;
			this.currentDY = Math.sin(angle) * this.speed;
			// Duration between 1 and 4 seconds
			long durationNanos = TimeUnit.MILLISECONDS.toNanos(1000 + r.nextInt(3000));
			this.directionEndsAtNanoTime = now + durationNanos;
		}

		this.x += (int) Math.round(this.currentDX);
		this.y += (int) Math.round(this.currentDY);

		// Clamp within map bounds
		if (this.x < 0) {
			this.x = 0;
			this.currentDX = -this.currentDX;
		} else if (this.x + this.size > GameStage.MAP_WIDTH) {
			this.x = GameStage.MAP_WIDTH - this.size;
			this.currentDX = -this.currentDX;
		}

		if (this.y < 0) {
			this.y = 0;
			this.currentDY = -this.currentDY;
		} else if (this.y + this.size > GameStage.MAP_HEIGHT) {
			this.y = GameStage.MAP_HEIGHT - this.size;
			this.currentDY = -this.currentDY;
		}
	}

	public void enemyCheckCollisionWithFood(ArrayList<Food> foods) {
		Iterator<Food> iterator = foods.iterator();
		while (iterator.hasNext()) {
			Food f = iterator.next();
			if (collidesWith(f)) {
				increaseSize(10);
				Image newImage = new Image("images/dog" + this.imageType + ".png", this.size, this.size, false, false);
				this.loadImage(newImage);
				this.setSpeed();
				iterator.remove();
				Food.decreaseCount();
			}
		}
	}

	public void enemyCheckCollisionWithOthers(ArrayList<Enemy> enemyList) {
		ArrayList<Enemy> enemiesToRemove = new ArrayList<Enemy>();
		for (Enemy e : enemyList) {
			if (!e.isAlive() || enemiesToRemove.contains(e)) continue;
			for (Enemy otherEnemy : enemyList) {
				if (e != otherEnemy && otherEnemy.isAlive() && !enemiesToRemove.contains(otherEnemy) && e.collidesWith(otherEnemy)) {
					if (e.getSize() > otherEnemy.getSize()) {
						e.increaseSize(otherEnemy.getSize());
						Image newImage = new Image("images/dog" + e.getImageType() + ".png", e.getSize(), e.getSize(), false, false);
						e.loadImage(newImage);
						e.setSpeed();
						otherEnemy.setAlive(false);
						enemiesToRemove.add(otherEnemy);
					} else if (otherEnemy.getSize() > e.getSize()) {
						otherEnemy.increaseSize(e.getSize());
						Image newImage = new Image("images/dog" + otherEnemy.getImageType() + ".png", otherEnemy.getSize(), otherEnemy.getSize(), false, false);
						otherEnemy.loadImage(newImage);
						otherEnemy.setSpeed();
						e.setAlive(false);
						enemiesToRemove.add(e);
						break;
					}
				}
			}
		}
		enemyList.removeAll(enemiesToRemove);
	}

	@Override
	public Rectangle2D getBounds() {
		return new Rectangle2D(this.x, this.y, this.size, this.size);
	}

	@Override
	public void render(GraphicsContext gc, double camX, double camY) {
		gc.drawImage(this.img, this.x - camX, this.y - camY);
	}

	public void setAlive(Boolean b) {
		this.alive = b;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed() {
		this.speed = Math.max(1, 120 / this.size);
	}

	public boolean isAlive() {
		return this.alive;
	}

	public void increaseSize(int value) {
		this.size += value;
		this.width = this.size;
		this.height = this.size;
	}

	public int getWidth() {
		return this.size;
	}

	public int getHeight() {
		return this.size;
	}

	public void die() {
		this.alive = false;
	}

	public int getSize() {
		return this.size;
	}

	public int getImageType() {
		return this.imageType;
	}
}
