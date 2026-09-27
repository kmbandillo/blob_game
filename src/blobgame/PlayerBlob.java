package blobgame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PlayerBlob extends Sprite {
	private String name;
	private boolean alive;
	private int size;
	private int speed;
	private int baseSpeed;
	private long speedBoostEndsAtNanoTime;
	private long immunityEndsAtNanoTime;
	private String lastPowerupAnnouncement = null;
	private long lastPowerupEatNanoTime = 0;
	private Color lastPowerupColor = Color.CYAN;
	private static int foodEaten = 0;
	private static int blobsEaten = 0;

	private boolean isSplit = false;
	private ArrayList<SubBlob> subBlobs = new ArrayList<>();

	public final static Image MAIN_IMAGE = new Image("images/anya.png", PlayerBlob.MAIN_WIDTH, PlayerBlob.MAIN_WIDTH, false, false);
	public final static int MAIN_WIDTH = 40;

	public static class SubBlob {
		private double x, y;
		private double offsetX, offsetY;
		private int size;
		private Image img;

		public SubBlob(double x, double y, double offsetX, double offsetY, int size) {
			this.x = x;
			this.y = y;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.size = size;
			this.img = new Image("images/anya.png", size, size, false, false);
		}

		public double getX() { return this.x; }
		public double getY() { return this.y; }
		public double getOffsetX() { return this.offsetX; }
		public double getOffsetY() { return this.offsetY; }
		public int getSize() { return this.size; }
		public Image getImage() { return this.img; }

		public void setPosition(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public void setOffsets(double offX, double offY) {
			this.offsetX = offX;
			this.offsetY = offY;
		}

		public void increaseSize(int value) {
			this.size += value;
			this.img = new Image("images/anya.png", this.size, this.size, false, false);
		}

		public Rectangle2D getBounds() {
			return new Rectangle2D(this.x, this.y, this.size, this.size);
		}

		public boolean collidesWith(Sprite other) {
			return this.getBounds().intersects(other.getBounds());
		}

		public boolean collidesWith(SubBlob other) {
			double c1x = this.x + this.size / 2.0;
			double c1y = this.y + this.size / 2.0;
			double c2x = other.x + other.size / 2.0;
			double c2y = other.y + other.size / 2.0;
			double dist = Math.hypot(c1x - c2x, c1y - c2y);
			return dist < (this.size / 2.0 + other.size / 2.0) - 2.0;
		}
	}

	public PlayerBlob(int x, int y) {
		super(x, y);
		this.alive = true;
		this.size = MAIN_WIDTH;
		this.baseSpeed = Math.max(1, 120 / this.size);
		this.speed = this.baseSpeed;
		this.loadImage(PlayerBlob.MAIN_IMAGE);
		this.isSplit = false;
		this.subBlobs.clear();
		clampPosition();
	}

	public static void resetStats() {
		foodEaten = 0;
		blobsEaten = 0;
	}

	public boolean isSplit() {
		return this.isSplit;
	}

	public ArrayList<SubBlob> getSubBlobs() {
		return this.subBlobs;
	}

	private double[][] generateFilledCircleOffsets(int count, double spacing) {
		double[][] offsets = new double[count][2];
		if (count <= 1) {
			if (count == 1) {
				offsets[0][0] = 0;
				offsets[0][1] = 0;
			}
			return offsets;
		}
		if (count == 2) {
			offsets[0][0] = -spacing / 2.0;
			offsets[0][1] = 0;
			offsets[1][0] = spacing / 2.0;
			offsets[1][1] = 0;
			return offsets;
		}
		if (count == 3) {
			double r = spacing / Math.sqrt(3.0);
			for (int i = 0; i < 3; i++) {
				double angle = i * (2.0 * Math.PI / 3.0) - Math.PI / 2.0;
				offsets[i][0] = Math.cos(angle) * r;
				offsets[i][1] = Math.sin(angle) * r;
			}
			return offsets;
		}

		// For count >= 4: 1 blob occupies the center (filled in the middle),
		// and the rest form concentric circular rings around it!
		offsets[0][0] = 0;
		offsets[0][1] = 0;

		int placed = 1;
		int ring = 1;
		while (placed < count) {
			int ringCapacity = (ring == 1) ? Math.min(6, count - placed) : Math.min(6 * ring, count - placed);
			double ringRadius = ring * spacing;
			double angleOffset = (ring % 2 == 0) ? Math.PI / ringCapacity : 0.0;

			for (int i = 0; i < ringCapacity; i++) {
				double angle = angleOffset + i * (2.0 * Math.PI / ringCapacity);
				offsets[placed][0] = Math.cos(angle) * ringRadius;
				offsets[placed][1] = Math.sin(angle) * ringRadius;
				placed++;
			}
			ring++;
		}

		return offsets;
	}

	public void split() {
		if (!this.isSplit) {
			// Single blob: split if size is 80 or above
			if (this.size < 80) {
				return;
			}

			int numBlobs = this.size / 40;
			if (numBlobs < 2) {
				return;
			}

			this.isSplit = true;
			this.subBlobs.clear();

			double centerX = this.x + this.size / 2.0;
			double centerY = this.y + this.size / 2.0;

			int baseSize = this.size / numBlobs;
			int remainder = this.size % numBlobs;

			// Circular cluster filled in the middle
			double[][] clusterOffsets = generateFilledCircleOffsets(numBlobs, 68.0);

			for (int i = 0; i < numBlobs; i++) {
				int s = baseSize + (i < remainder ? 1 : 0);
				double offX = clusterOffsets[i][0];
				double offY = clusterOffsets[i][1];
				double subX = centerX + offX - s / 2.0;
				double subY = centerY + offY - s / 2.0;
				this.subBlobs.add(new SubBlob(subX, subY, offX, offY, s));
			}

			this.x = (int) centerX;
			this.y = (int) centerY;

			separateBlobs(25.0);
			this.setSpeed();

		} else {
			// When already split: find the bigger blob that qualifies (size >= 80)
			SubBlob largest = null;
			for (SubBlob sb : this.subBlobs) {
				if (sb.getSize() >= 80) {
					if (largest == null || sb.getSize() > largest.getSize()) {
						largest = sb;
					}
				}
			}

			if (largest == null) {
				return;
			}

			int bigSize = largest.getSize();
			int numNew = bigSize / 40;
			if (numNew < 2) {
				return;
			}

			this.subBlobs.remove(largest);

			double origOffX = largest.getOffsetX();
			double origOffY = largest.getOffsetY();
			double origCenterX = largest.getX() + bigSize / 2.0;
			double origCenterY = largest.getY() + bigSize / 2.0;

			int baseSize = bigSize / numNew;
			int remainder = bigSize % numNew;

			// Circular cluster filled in the middle around the split blob's location
			double[][] localOffsets = generateFilledCircleOffsets(numNew, 68.0);

			for (int i = 0; i < numNew; i++) {
				int s = baseSize + (i < remainder ? 1 : 0);
				double localOffX = localOffsets[i][0];
				double localOffY = localOffsets[i][1];
				double newOffX = origOffX + localOffX;
				double newOffY = origOffY + localOffY;
				double subX = origCenterX + localOffX - s / 2.0;
				double subY = origCenterY + localOffY - s / 2.0;
				this.subBlobs.add(new SubBlob(subX, subY, newOffX, newOffY, s));
			}

			separateBlobs(25.0);
			this.setSpeed();
		}
	}

	private void separateBlobs(double minGap) {
		if (this.subBlobs.size() < 2) return;

		for (int iter = 0; iter < 25; iter++) {
			boolean movedAny = false;
			for (int i = 0; i < this.subBlobs.size(); i++) {
				SubBlob b1 = this.subBlobs.get(i);
				for (int j = i + 1; j < this.subBlobs.size(); j++) {
					SubBlob b2 = this.subBlobs.get(j);

					double dx = b1.getOffsetX() - b2.getOffsetX();
					double dy = b1.getOffsetY() - b2.getOffsetY();
					double dist = Math.hypot(dx, dy);
					double reqDist = (b1.getSize() + b2.getSize()) / 2.0 + minGap;

					if (dist < reqDist) {
						movedAny = true;
						double overlap = reqDist - dist;
						double nx, ny;
						if (dist < 0.001) {
							double angle = (i + j + 1) * 1.5;
							nx = Math.cos(angle);
							ny = Math.sin(angle);
						} else {
							nx = dx / dist;
							ny = dy / dist;
						}
						double shift = overlap / 2.0;
						b1.setOffsets(b1.getOffsetX() + nx * shift, b1.getOffsetY() + ny * shift);
						b2.setOffsets(b2.getOffsetX() - nx * shift, b2.getOffsetY() - ny * shift);
					}
				}
			}
			if (!movedAny) break;
		}

		for (SubBlob sb : this.subBlobs) {
			double targetX = this.x + sb.getOffsetX() - sb.getSize() / 2.0;
			double targetY = this.y + sb.getOffsetY() - sb.getSize() / 2.0;
			targetX = Math.max(0, Math.min(GameStage.MAP_WIDTH - sb.getSize(), targetX));
			targetY = Math.max(0, Math.min(GameStage.MAP_HEIGHT - sb.getSize(), targetY));
			sb.setPosition(targetX, targetY);
		}

		Rectangle2D bounds = this.getBounds();
		this.x = (int) (bounds.getMinX() + bounds.getWidth() / 2.0);
		this.y = (int) (bounds.getMinY() + bounds.getHeight() / 2.0);
		for (SubBlob sb : this.subBlobs) {
			sb.setOffsets((sb.getX() + sb.getSize() / 2.0) - this.x, (sb.getY() + sb.getSize() / 2.0) - this.y);
		}
	}

	private void consolidateToSingleBlob() {
		if (this.subBlobs.size() == 1) {
			SubBlob last = this.subBlobs.get(0);
			this.isSplit = false;
			this.size = last.getSize();
			this.x = (int) last.getX();
			this.y = (int) last.getY();
			Image newImg = new Image("images/anya.png", this.size, this.size, false, false);
			this.loadImage(newImg);
			this.subBlobs.clear();
			this.clampPosition();
			this.setSpeed();
		}
	}

	public void checkSubBlobCollisions() {
		if (!this.isSplit || this.subBlobs.size() < 2) return;

		boolean absorbedAny = false;
		boolean foundCollision;
		do {
			foundCollision = false;
			SubBlob toAbsorb = null;
			SubBlob absorber = null;

			for (int i = 0; i < this.subBlobs.size(); i++) {
				SubBlob b1 = this.subBlobs.get(i);
				for (int j = i + 1; j < this.subBlobs.size(); j++) {
					SubBlob b2 = this.subBlobs.get(j);
					if (b1.collidesWith(b2)) {
						if (b1.getSize() > b2.getSize()) {
							absorber = b1;
							toAbsorb = b2;
							foundCollision = true;
							break;
						} else if (b2.getSize() > b1.getSize()) {
							absorber = b2;
							toAbsorb = b1;
							foundCollision = true;
							break;
						}
					}
				}
				if (foundCollision) break;
			}

			if (foundCollision && absorber != null && toAbsorb != null) {
				absorber.increaseSize(toAbsorb.getSize());
				this.subBlobs.remove(toAbsorb);
				absorbedAny = true;
			}
		} while (foundCollision && this.subBlobs.size() > 1);

		if (absorbedAny) {
			if (this.subBlobs.size() == 1) {
				consolidateToSingleBlob();
			} else {
				this.setSpeed();
			}
		}
	}

	void checkCollisionsWithFood(PlayerBlob playerBlob, ArrayList<Food> food) {
		if (this.isSplit) {
			Iterator<Food> it = food.iterator();
			while (it.hasNext()) {
				Food f = it.next();
				for (SubBlob sb : this.subBlobs) {
					if (sb.collidesWith(f)) {
						sb.increaseSize(10);
						this.increaseSize(10);
						setFoodEaten();
						this.setSpeed();
						it.remove();
						Food.decreaseCount();
						break;
					}
				}
			}
			this.checkSubBlobCollisions();
		} else {
			Iterator<Food> it = food.iterator();
			while (it.hasNext()) {
				Food f = it.next();
				if (playerBlob.collidesWith(f)) {
					playerBlob.increaseSize(10);
					setFoodEaten();
					this.setSpeed();
					Image newImage = new Image("images/anya.png", this.size, this.size, false, false);
					playerBlob.loadImage(newImage);
					playerBlob.clampPosition();
					it.remove();
					Food.decreaseCount();
				}
			}
		}
	}

	void checkCollisionsWithPowerUps(PlayerBlob playerBlob, ArrayList<Powerups> powerups, long currentNanoTime) {
		Iterator<Powerups> it = powerups.iterator();
		long now = System.nanoTime();
		while (it.hasNext()) {
			Powerups p = it.next();
			boolean hit = false;
			double pw = Powerups.POWERUP_WIDTH;
			double pcx = p.getX() + pw / 2.0;
			double pcy = p.getY() + pw / 2.0;

			if (this.isSplit) {
				for (SubBlob sb : this.subBlobs) {
					double scx = sb.getX() + sb.getSize() / 2.0;
					double scy = sb.getY() + sb.getSize() / 2.0;
					double dist = Math.hypot(scx - pcx, scy - pcy);
					if (dist <= (sb.getSize() / 2.0 + pw / 2.0 + 4.0) || sb.collidesWith(p)) {
						hit = true;
						break;
					}
				}
			} else {
				double mcx = this.x + this.size / 2.0;
				double mcy = this.y + this.size / 2.0;
				double dist = Math.hypot(mcx - pcx, mcy - pcy);
				if (dist <= (this.size / 2.0 + pw / 2.0 + 4.0) || playerBlob.collidesWith(p)) {
					hit = true;
				}
			}

			if (hit) {
				if (p.getType() == Powerups.SPEED_BOOST) {
					this.activateSpeedBoost(now);
				} else if (p.getType() == Powerups.IMMUNITY) {
					this.activateImmunity(now);
				}
				it.remove();
			}
		}
	}

	void checkCollisionsWithEnemies(PlayerBlob playerBlob, ArrayList<Enemy> enemy, long currentNanoTime) {
		boolean immune = this.isImmune(currentNanoTime);
		if (this.isSplit) {
			Iterator<Enemy> it = enemy.iterator();
			while (it.hasNext()) {
				Enemy e = it.next();
				ArrayList<SubBlob> lostSubBlobs = new ArrayList<>();
				for (SubBlob sb : this.subBlobs) {
					if (sb.collidesWith(e)) {
						if (sb.getSize() >= e.getSize()) {
							// Sub-blob absorbs enemy and grows
							sb.increaseSize(e.getSize());
							this.increaseSize(e.getSize());
							setBlobEaten();
							this.setSpeed();
							it.remove();
							break;
						} else if (!immune) {
							// Enemy absorbs this sub-blob
							e.increaseSize(sb.getSize());
							Image newImage = new Image("images/dog" + e.getImageType() + ".png", e.getSize(), e.getSize(), false, false);
							e.loadImage(newImage);
							e.setSpeed();
							lostSubBlobs.add(sb);
						}
					}
				}
				if (!lostSubBlobs.isEmpty()) {
					for (SubBlob lost : lostSubBlobs) {
						this.subBlobs.remove(lost);
						this.size = Math.max(0, this.size - lost.getSize());
					}
					if (this.subBlobs.isEmpty()) {
						this.alive = false;
						break;
					} else if (this.subBlobs.size() == 1) {
						consolidateToSingleBlob();
					} else {
						this.setSpeed();
					}
				}
			}
			this.checkSubBlobCollisions();
		} else {
			Iterator<Enemy> it = enemy.iterator();
			while (it.hasNext()) {
				Enemy e = it.next();
				if (playerBlob.collidesWith(e)) {
					if (this.size >= e.getSize()) {
						playerBlob.increaseSize(e.getSize());
						Image newImage = new Image("images/anya.png", this.size, this.size, false, false);
						playerBlob.loadImage(newImage);
						playerBlob.clampPosition();
						setBlobEaten();
						this.setSpeed();
						it.remove();
					} else if (!immune) {
						e.increaseSize(this.size);
						Image newImage = new Image("images/dog" + e.getImageType() + ".png", e.getSize(), e.getSize(), false, false);
						e.loadImage(newImage);
						e.setSpeed();
						this.alive = false;
						break;
					}
				}
			}
		}
	}

	public void clampPosition() {
		if (this.x < 0) this.x = 0;
		if (this.y < 0) this.y = 0;
		if (this.x + this.size > GameStage.MAP_WIDTH) this.x = GameStage.MAP_WIDTH - this.size;
		if (this.y + this.size > GameStage.MAP_HEIGHT) this.y = GameStage.MAP_HEIGHT - this.size;
	}

	public void move() {
		this.x += this.dx;
		this.y += this.dy;

		if (this.isSplit && !this.subBlobs.isEmpty()) {
			for (SubBlob sb : this.subBlobs) {
				double newX = sb.getX() + this.dx;
				double newY = sb.getY() + this.dy;
				newX = Math.max(0, Math.min(GameStage.MAP_WIDTH - sb.getSize(), newX));
				newY = Math.max(0, Math.min(GameStage.MAP_HEIGHT - sb.getSize(), newY));
				sb.setPosition(newX, newY);
			}

			// Update cluster reference (this.x, this.y) to the cluster center
			Rectangle2D bounds = this.getBounds();
			this.x = (int) (bounds.getMinX() + bounds.getWidth() / 2.0);
			this.y = (int) (bounds.getMinY() + bounds.getHeight() / 2.0);

			// Update offsets relative to cluster center
			for (SubBlob sb : this.subBlobs) {
				sb.setOffsets((sb.getX() + sb.getSize() / 2.0) - this.x, (sb.getY() + sb.getSize() / 2.0) - this.y);
			}

			// Check sub-blob collisions with each other
			this.checkSubBlobCollisions();
		} else {
			clampPosition();
		}
	}

	@Override
	public Rectangle2D getBounds() {
		if (this.isSplit && !this.subBlobs.isEmpty()) {
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			for (SubBlob sb : this.subBlobs) {
				if (sb.getX() < minX) minX = sb.getX();
				if (sb.getY() < minY) minY = sb.getY();
				if (sb.getX() + sb.getSize() > maxX) maxX = sb.getX() + sb.getSize();
				if (sb.getY() + sb.getSize() > maxY) maxY = sb.getY() + sb.getSize();
			}
			return new Rectangle2D(minX, minY, Math.max(40, maxX - minX), Math.max(40, maxY - minY));
		}
		return new Rectangle2D(this.x, this.y, this.size, this.size);
	}

	@Override
	public void render(GraphicsContext gc) {
		render(gc, 0, 0);
	}

	@Override
	public void render(GraphicsContext gc, double camX, double camY) {
		long now = System.nanoTime();
		boolean immune = this.isImmune(now);
		boolean speedBoost = this.hasSpeedBoost(now);

		if (this.isSplit && !this.subBlobs.isEmpty()) {
			for (SubBlob sb : this.subBlobs) {
				double drawX = sb.getX() - camX;
				double drawY = sb.getY() - camY;
				double s = sb.getSize();

				// Draw blob image first
				gc.drawImage(sb.getImage(), drawX, drawY);

				// Draw glowing aura rings ON TOP of image so they are completely visible
				if (immune && speedBoost) {
					gc.setStroke(Color.GOLD);
					gc.setLineWidth(4.5);
					gc.strokeOval(drawX - 5, drawY - 5, s + 10, s + 10);

					gc.setStroke(Color.rgb(0, 220, 255));
					gc.setLineWidth(3);
					gc.strokeOval(drawX - 1, drawY - 1, s + 2, s + 2);
				} else if (immune) {
					gc.setStroke(Color.GOLD);
					gc.setLineWidth(4.5);
					gc.strokeOval(drawX - 4, drawY - 4, s + 8, s + 8);
				} else if (speedBoost) {
					gc.setStroke(Color.rgb(0, 220, 255));
					gc.setLineWidth(4.5);
					gc.strokeOval(drawX - 4, drawY - 4, s + 8, s + 8);
				}
			}
		} else {
			double drawX = this.x - camX;
			double drawY = this.y - camY;

			// Draw blob image first
			gc.drawImage(this.img, drawX, drawY);

			// Draw glowing aura rings ON TOP of image so they are completely visible
			if (immune && speedBoost) {
				gc.setStroke(Color.GOLD);
				gc.setLineWidth(5);
				gc.strokeOval(drawX - 6, drawY - 6, this.size + 12, this.size + 12);

				gc.setStroke(Color.rgb(0, 220, 255));
				gc.setLineWidth(3.5);
				gc.strokeOval(drawX - 2, drawY - 2, this.size + 4, this.size + 4);
			} else if (immune) {
				gc.setStroke(Color.GOLD);
				gc.setLineWidth(5);
				gc.strokeOval(drawX - 4, drawY - 4, this.size + 8, this.size + 8);
			} else if (speedBoost) {
				gc.setStroke(Color.rgb(0, 220, 255));
				gc.setLineWidth(5);
				gc.strokeOval(drawX - 4, drawY - 4, this.size + 8, this.size + 8);
			}
		}

		// Floating status tag directly above the player's blob center for immediate visibility
		if (speedBoost || immune) {
			double centerX = getCenterX() - camX;
			double topY;
			if (this.isSplit && !this.subBlobs.isEmpty()) {
				Rectangle2D b = this.getBounds();
				topY = b.getMinY() - camY - 14;
			} else {
				topY = (this.y - camY) - 14;
			}

			gc.setFont(Font.font("Century Gothic", FontWeight.BOLD, 12));
			gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
			gc.setTextBaseline(javafx.geometry.VPos.CENTER);

			String tag;
			Color tagColor;
			if (speedBoost && immune) {
				tag = "⚡ SPEED & 🛡️ IMMUNITY";
				tagColor = Color.rgb(255, 230, 80);
			} else if (speedBoost) {
				tag = "⚡ 2X SPEED (" + String.format("%.1fs", getSpeedBoostRemainingSeconds(now)) + ")";
				tagColor = Color.rgb(0, 230, 255);
			} else {
				tag = "🛡️ IMMUNITY (" + String.format("%.1fs", getImmunityRemainingSeconds(now)) + ")";
				tagColor = Color.rgb(255, 215, 0);
			}

			// Black text shadow/outline for high contrast
			gc.setStroke(Color.rgb(0, 0, 0, 0.85));
			gc.setLineWidth(3);
			gc.strokeText(tag, centerX, topY);

			gc.setFill(tagColor);
			gc.fillText(tag, centerX, topY);
		}
	}

	public boolean isAlive() {
		return this.alive;
	}

	public String getName() {
		return this.name;
	}

	public int getWidth() {
		if (this.isSplit && !this.subBlobs.isEmpty()) {
			int maxSubSize = 40;
			for (SubBlob sb : this.subBlobs) {
				if (sb.getSize() > maxSubSize) maxSubSize = sb.getSize();
			}
			return maxSubSize;
		}
		return this.size;
	}

	public int getSize() {
		return this.size;
	}

	public int getHeight() {
		if (this.isSplit && !this.subBlobs.isEmpty()) {
			int maxSubSize = 40;
			for (SubBlob sb : this.subBlobs) {
				if (sb.getSize() > maxSubSize) maxSubSize = sb.getSize();
			}
			return maxSubSize;
		}
		return this.size;
	}

	public void die() {
		this.alive = false;
	}

	public void increaseSize(int value) {
		this.size += value;
		if (!this.isSplit) {
			this.width = this.size;
			this.height = this.size;
		}
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed() {
		int effectiveSize = this.size;
		if (this.isSplit && !this.subBlobs.isEmpty()) {
			int maxSubSize = 40;
			for (SubBlob sb : this.subBlobs) {
				if (sb.getSize() > maxSubSize) {
					maxSubSize = sb.getSize();
				}
			}
			effectiveSize = maxSubSize;
		}
		this.baseSpeed = Math.max(1, 120 / effectiveSize);
		if (this.speedBoostEndsAtNanoTime > 0 && System.nanoTime() < this.speedBoostEndsAtNanoTime) {
			this.speed = Math.max(this.baseSpeed * 2, this.baseSpeed + 3);
		} else {
			this.speed = this.baseSpeed;
		}
	}

	public void activateSpeedBoost(long currentNanoTime) {
		this.speedBoostEndsAtNanoTime = currentNanoTime + TimeUnit.SECONDS.toNanos(5);
		this.lastPowerupAnnouncement = "⚡ SPEED BOOST ACTIVATED! (2X SPEED)";
		this.lastPowerupColor = Color.rgb(0, 220, 255);
		this.lastPowerupEatNanoTime = currentNanoTime;
		this.setSpeed();
	}

	public void activateImmunity(long currentNanoTime) {
		this.immunityEndsAtNanoTime = currentNanoTime + TimeUnit.SECONDS.toNanos(5);
		this.lastPowerupAnnouncement = "🛡️ IMMUNITY ACTIVATED! (INVULNERABLE)";
		this.lastPowerupColor = Color.rgb(255, 215, 0);
		this.lastPowerupEatNanoTime = currentNanoTime;
	}

	public void updatePowerupEffects(long currentNanoTime) {
		if (this.speedBoostEndsAtNanoTime > 0 && currentNanoTime >= this.speedBoostEndsAtNanoTime) {
			this.speedBoostEndsAtNanoTime = 0;
			this.setSpeed();
		}
	}

	public boolean isImmune(long currentNanoTime) {
		return this.immunityEndsAtNanoTime > 0 && currentNanoTime < this.immunityEndsAtNanoTime;
	}

	public double getSpeedBoostRemainingSeconds(long currentNanoTime) {
		if (this.speedBoostEndsAtNanoTime > currentNanoTime) {
			return (this.speedBoostEndsAtNanoTime - currentNanoTime) / 1_000_000_000.0;
		}
		return 0;
	}

	public double getImmunityRemainingSeconds(long currentNanoTime) {
		if (this.immunityEndsAtNanoTime > currentNanoTime) {
			return (this.immunityEndsAtNanoTime - currentNanoTime) / 1_000_000_000.0;
		}
		return 0;
	}

	public boolean hasSpeedBoost(long currentNanoTime) {
		return getSpeedBoostRemainingSeconds(currentNanoTime) > 0;
	}

	public String getLastPowerupAnnouncement() {
		return this.lastPowerupAnnouncement;
	}

	public Color getLastPowerupColor() {
		return this.lastPowerupColor;
	}

	public void adjustPauseTime(long pauseDurationNano) {
		if (this.speedBoostEndsAtNanoTime > 0) {
			this.speedBoostEndsAtNanoTime += pauseDurationNano;
		}
		if (this.immunityEndsAtNanoTime > 0) {
			this.immunityEndsAtNanoTime += pauseDurationNano;
		}
		if (this.lastPowerupEatNanoTime > 0) {
			this.lastPowerupEatNanoTime += pauseDurationNano;
		}
	}

	public double getLastPowerupAnnouncementRemaining(long currentNanoTime) {
		if (this.lastPowerupEatNanoTime > 0) {
			double elapsed = (currentNanoTime - this.lastPowerupEatNanoTime) / 1_000_000_000.0;
			if (elapsed < 2.0) {
				return 2.0 - elapsed;
			}
		}
		return 0;
	}

	public static void setFoodEaten() {
		foodEaten++;
	}

	public static void setBlobEaten() {
		blobsEaten++;
	}

	public static int getFoodEaten() {
		return foodEaten;
	}

	public static int getBlobEaten() {
		return blobsEaten;
	}

	public double getCenterX() {
		if (this.isSplit && !this.subBlobs.isEmpty()) {
			Rectangle2D b = this.getBounds();
			return b.getMinX() + b.getWidth() / 2.0;
		}
		return this.x + this.size / 2.0;
	}

	public double getCenterY() {
		if (this.isSplit && !this.subBlobs.isEmpty()) {
			Rectangle2D b = this.getBounds();
			return b.getMinY() + b.getHeight() / 2.0;
		}
		return this.y + this.size / 2.0;
	}
}
