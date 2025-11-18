package engine;

import screen.Screen;
import entity.Entity;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import engine.DrawManager.SpriteType;

public class AnimatedBackground {

    /**
	 * A simple class to represent a star for the animated background.
	 * Stores the non-rotating base coordinates and speed.
	 */
	public static class Star {
		public float baseX;
		public float baseY;
		public float speed;
		public float brightness;
        public float brightnessOffset;

		public Star(float baseX, float baseY, float speed) {
			this.baseX = baseX;
			this.baseY = baseY;
			this.speed = speed;
			this.brightness = 0;
			this.brightnessOffset = (float) (Math.random() * Math.PI * 2);
		}
	}

	/**
	 * A simple class to represent a shooting star.
	 */
	public static class ShootingStar {
		public float x;
		public float y;
		public float speedX;
		public float speedY;

		public ShootingStar(float x, float y, float speedX, float speedY) {
			this.x = x;
			this.y = y;
			this.speedX = speedX;
			this.speedY = speedY;
		}
	}

	/**
	 * A simple class to represent a background enemy.
	 */
	private static class BackgroundEnemy extends Entity {
		private int speed;

		public BackgroundEnemy(int positionX, int positionY, int speed, SpriteType spriteType) {
			super(positionX, positionY, 12 * 2, 8 * 2, Color.WHITE);
			this.speed = speed;
			this.spriteType = spriteType;
		}

		public int getSpeed() {
			return speed;
		}
	}

    /** Number of stars in the background. */
	private static final int NUM_STARS = 150;
	/** Speed of the rotation animation. */
    private static final float ROTATION_SPEED = 4.0f;
	/** Milliseconds between enemy spawns. */
	private static final int ENEMY_SPAWN_COOLDOWN = 2000;
	/** Probability of an enemy spawning. */
	private static final double ENEMY_SPAWN_CHANCE = 0.3;
	/** Milliseconds between shooting star spawns. */
    private static final int SHOOTING_STAR_COOLDOWN = 3000;
    /** Probability of a shooting star spawning. */
    private static final double SHOOTING_STAR_SPAWN_CHANCE = 0.2;

    /** Cooldown for enemy spawning. */
	private Cooldown enemySpawnCooldown;
	/** Cooldown for shooting star spawning. */
    private Cooldown shootingStarCooldown;

    /** List of stars for the background animation. */
	private List<Star> stars;
	/** List of background enemies. */
	private List<Entity> backgroundEnemies;
	/** List of shooting stars. */
    private List<ShootingStar> shootingStars;

    /** Current rotation angle of the starfield. */
    private float currentAngle;
    /** Target rotation angle of the starfield. */
    private float targetAngle;

    /** Random number generator. */
    private Random random;

    private int width;
    private int height;

    public AnimatedBackground(int width, int height) {
        this.width = width;
        this.height = height;

        this.enemySpawnCooldown = Core.getCooldown(ENEMY_SPAWN_COOLDOWN);
		this.shootingStarCooldown = Core.getCooldown(SHOOTING_STAR_COOLDOWN);
        this.enemySpawnCooldown.reset();
		this.shootingStarCooldown.reset();

        this.random = new Random();
		this.stars = new ArrayList<Star>();
		for (int i = 0; i < NUM_STARS; i++) {
			float speed = (float) (Math.random() * 2.5 + 0.5);
			this.stars.add(new Star((float) (Math.random() * width),
					(float) (Math.random() * height), speed));
		}

        this.backgroundEnemies = new ArrayList<Entity>();
		this.shootingStars = new ArrayList<ShootingStar>();

        this.currentAngle = 0;
		this.targetAngle = 0;
    }

    public void update() {
        // Smoothly animate the rotation angle
        if (currentAngle < targetAngle) {
            currentAngle = Math.min(currentAngle + ROTATION_SPEED, targetAngle);
        } else if (currentAngle > targetAngle) {
            currentAngle = Math.max(currentAngle - ROTATION_SPEED, targetAngle);
        }

		// Animate stars in their non-rotating space
		for (Star star : this.stars) {
			star.baseY += star.speed;
			if (star.baseY > this.height) {
				star.baseY = 0;
				star.baseX = (float) (Math.random() * this.width);
			}
			// Update brightness for twinkling effect
			star.brightness = 0.5f + (float) (Math.sin(star.brightnessOffset + System.currentTimeMillis() / 500.0) + 1.0) / 4.0f;
		}

		// Spawn and move background enemies
		if (this.enemySpawnCooldown.checkFinished()) {
			this.enemySpawnCooldown.reset();
			if (Math.random() < ENEMY_SPAWN_CHANCE) {
				SpriteType[] enemyTypes = { SpriteType.EnemyShipA1, SpriteType.EnemyShipB1, SpriteType.EnemyShipC1 };
				SpriteType randomEnemyType = enemyTypes[random.nextInt(enemyTypes.length)];
				int randomX = (int) (Math.random() * this.width);
				int speed = random.nextInt(2) + 1;
				this.backgroundEnemies.add(new BackgroundEnemy(randomX, -20, speed, randomEnemyType));
			}
		}

		java.util.Iterator<Entity> enemyIterator = this.backgroundEnemies.iterator();
		while (enemyIterator.hasNext()) {
			BackgroundEnemy enemy = (BackgroundEnemy) enemyIterator.next();
			enemy.setPositionY(enemy.getPositionY() + enemy.getSpeed());
			if (enemy.getPositionY() > this.height) {
				enemyIterator.remove();
			}
		}

		// Spawn and move shooting stars
        if (this.shootingStarCooldown.checkFinished()) {
            this.shootingStarCooldown.reset();
            if (Math.random() < SHOOTING_STAR_SPAWN_CHANCE) {
                float speedX = (float) (Math.random() * 10 + 5) * (Math.random() > 0.5 ? 1 : -1);
                float speedY = (float) (Math.random() * 10 + 5) * (Math.random() > 0.5 ? 1 : -1);
                this.shootingStars.add(new ShootingStar(random.nextInt(this.width), -10, speedX, speedY));
            }
        }

		java.util.Iterator<ShootingStar> shootingStarIterator = this.shootingStars.iterator();
        while (shootingStarIterator.hasNext()) {
            ShootingStar shootingStar = shootingStarIterator.next();
            shootingStar.x += shootingStar.speedX;
            shootingStar.y += shootingStar.speedY;
            if (shootingStar.x < -20 || shootingStar.x > this.width + 20 ||
                shootingStar.y < -20 || shootingStar.y > this.height + 20) {
                shootingStarIterator.remove();
            }
        }
    }

    public void draw(DrawManager drawManager, Screen screen) {
        // Draw stars with rotation
		drawManager.drawStars(screen, this.stars, this.currentAngle);

		// Draw shooting stars with rotation
        drawManager.drawShootingStars(screen, this.shootingStars, this.currentAngle);

		// Draw background enemies with rotation
		final double angleRad = Math.toRadians(this.currentAngle);
        final double cosAngle = Math.cos(angleRad);
        final double sinAngle = Math.sin(angleRad);
        final int centerX = this.width / 2;
        final int centerY = this.height / 2;

		for (Entity enemy : this.backgroundEnemies) {
			float relX = enemy.getPositionX() - centerX;
            float relY = enemy.getPositionY() - centerY;

            double rotatedX = relX * cosAngle - relY * sinAngle;
            double rotatedY = relX * sinAngle + relY * cosAngle;

            int screenX = (int) (rotatedX + centerX);
            int screenY = (int) (rotatedY + centerY);

			drawManager.drawEntity(enemy, screenX, screenY);
		}
    }

    public void rotateLeft() {
        this.targetAngle -= 90;
    }

    public void rotateRight() {
        this.targetAngle += 90;
    }
}
