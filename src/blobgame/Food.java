package blobgame;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.image.Image;

public class Food extends Sprite{
		private static int count = 0;
		public final static int FOOD_WIDTH = 20;
		public final static Image FOOD_IMAGE1 = new Image("images/cookie.png",FOOD_WIDTH, FOOD_WIDTH,false,false);
		private Boolean isEaten = false;
		private int x;
		private int y;

		Food(int x, int y){
			super(x,y);
			this.loadImage(FOOD_IMAGE1);
			this.x = x;
			this.y = y;
		}

		public static void increaseCount() {
			count++;
		}

		public static void decreaseCount() {
			count--;
		}

	    public static int getCount(){
	        return count;
	    }
//
		public void move(ArrayList<Food> food) {
			    this.x += this.dx;
			    this.y += this.dy;
		}

		//getter

	    public int getX(){
			return this.x;
		}

		public int getY(){
			return this.y;
		}

		public void setX(int i) {
			this.x = i;
		}

		public void setY(int i) {
			this.y = i;
		}


	}

//}
