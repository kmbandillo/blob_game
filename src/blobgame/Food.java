package blobgame;

import javafx.scene.image.Image;

public class Food extends Sprite {
	private static int count = 0;
	public final static int FOOD_WIDTH = 20;
	public final static Image FOOD_IMAGE1 = new Image("images/cookie.png", FOOD_WIDTH, FOOD_WIDTH, false, false);
	private Boolean isEaten = false;

	public Food(int x, int y) {
		super(x, y);
		this.loadImage(FOOD_IMAGE1);
	}

	public static void increaseCount() {
		count++;
	}

	public static void decreaseCount() {
		count--;
	}

	public static void resetCount() {
		count = 0;
	}

	public static int getCount() {
		return count;
	}

	public Boolean isEaten() {
		return this.isEaten;
	}

	public void setEaten(Boolean eaten) {
		this.isEaten = eaten;
	}
}
