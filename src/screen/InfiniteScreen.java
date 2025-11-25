package screen;

import engine.*;
import engine.GameState;
import engine.level.Level;
import entity.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Random;

public class InfiniteScreen extends Screen implements CollisionContext {

    // Fields for game state
    /** Bonus life awarded this level. */
    private boolean bonusLife;
    /** Player score. */
    private int score;
    /** Player object. */
    private Ship ship;
    /** Moment the game starts. */
    private long gameStartTime;
    /** Whether boss has appeared. */
    private Cooldown bossSpawnCooldown;
    /** Whether boss has appeared. */
    private boolean bossSpawned;
    /** Height of the items separation line (above items). */
    private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;

    private static final int INPUT_DELAY = 6000;
    private static final int LIFE_SCORE = 100;
    private static final int SEPARATION_LINE_HEIGHT = 45;
    private static final int SCREEN_CHANGE_INTERVAL = 1500;

    /** EnemyShip spawn interval :
     * every 15 seconds, enemyship spawn time is reduced by 0.1 seconds from 1.2 second to minimum 0.2 seconds
     * Every start begins with INITIAL_SPAWN_INTERVAL minus SPAWN_INTERVAL_DECREASE.*/
    private static final int INITIAL_SPAWN_INTERVAL = 1300;
    private static final int MIN_SPAWN_INTERVAL = 200;
    private static final int SPAWN_INTERVAL_DECREASE = 100;
    private static final int SPAWN_INTERVAL_DECREASE_TIME = 15000;
    /** Boss spawn interval: 5 minute (300000 milliseconds) */
    private static final int BOSS_SPAWN_INTERVAL = 300000;
    private static int BOSS_SPAWN_COUNT = 0;

    private Cooldown enemySpawnCooldown;
    private int currentSpawnInterval;
    private Cooldown difficultyIncreaseCooldown;
    public static int getItemsSeparationLineHeight() {return ITEMS_SEPARATION_LINE_HEIGHT;}
    /** Gotten coin. */
    private int coin;
    /** Player lives */
    private int lives;
    /** Maximum lives player can have */
    private int maxLives;
    /** Get game information. */
    private GameState gameState;
    /** Total bullets shot by the player. */
    private int bulletsShot;
    /** Set of all bullets fired by on-screen ships. */
    private Set<Bullet> bullets;
    /** Total ships destroyed by the player. */
    private int shipsDestroyed;
    /** Manages collisions between entities. */
    private CollisionManager collisionManager;
    /** Set of all dropItems dropped by on screen ships. */
    private Set<DropItem> dropItems;
    /** Health change popup. */
    private String healthPopupText;
    private Cooldown healthPopupCooldown;
    /** bossBullets carry bullets which Boss fires */
    private Set<BossBullet> bossBullets;
    /** Is the bullet on the screen erased */
    private boolean is_cleared = false;
    /** FinalBoss */
    private FinalBoss finalBoss;
    /** OmegaBoss */
    private MidBoss omegaBoss;

    /** Timer to track elapsed time in infinite mode */
    private GameTimer gameTimer;
    /** Elapsed time since the game started. */
    private long elapsedTime;
    /** Custom formation manager for Infinite Mode enemies */
    private InfiniteEnemyFormation enemyManager;
    /** Random generator */
    private Random random;
    /** Check Boss exists or not */
    private boolean bossActive = false;
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param bonusLife Checks if a bonus life is awarded this level.
     * @param maxLives Maximum number of lives.
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public InfiniteScreen(final GameState gameState,
                          final boolean bonusLife, final int maxLives,
                          final int width, final int height, final int fps) {
        super(width, height, fps);

        this.bonusLife = bonusLife;
        this.maxLives = maxLives;
        this.score = gameState.getScore();
        this.coin = gameState.getCoin();
        this.lives = gameState.getLivesRemaining();
        this.gameState = gameState;
        if (this.bonusLife) this.lives++;
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();
        this.random = new Random();
    }

    /** Initializes basic screen properties, and adds necessary elements. */
    public void initialize() {
        super.initialize();

        this.collisionManager = new CollisionManager(this);
        this.enemyManager = new InfiniteEnemyFormation();
        /** Initialize the bullet Boss fired */
        this.bossBullets = new HashSet<>();
        this.ship = new Ship(this.width / 2, ITEMS_SEPARATION_LINE_HEIGHT - 20,Color.green);
        this.ship.setPlayerId(1);
        this.bossBullets = new HashSet<>();
        this.bullets = new HashSet<Bullet>();
        this.dropItems = new HashSet<DropItem>();

        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();

        this.currentSpawnInterval = INITIAL_SPAWN_INTERVAL;
        this.enemySpawnCooldown = Core.getCooldown(currentSpawnInterval);
        this.difficultyIncreaseCooldown = Core.getCooldown(SPAWN_INTERVAL_DECREASE_TIME);
        this.enemySpawnCooldown.reset();

        this.bossSpawnCooldown = Core.getCooldown(BOSS_SPAWN_INTERVAL);
        this.bossSpawnCooldown.reset();
        this.bossSpawned = false;
        BOSS_SPAWN_COUNT = 0;

        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();

        this.gameTimer = new GameTimer();
        this.elapsedTime = 0;
        this.finalBoss = null;
        this.omegaBoss = null;
        this.enemySpawnCooldown = Core.getCooldown(INITIAL_SPAWN_INTERVAL);
        this.enemySpawnCooldown.reset();

        this.gameTimer.start();
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void update() {
        super.update();

        if (this.gameTimer.isRunning()) {
            this.elapsedTime = this.gameTimer.getElapsedTime();
        }
        if (this.bossActive) {
            updateBoss();
        }
        else {
            spawnEnemies();
        }
        scaleEnemyHealthOverTime();
        spawnBoss();
        updateScore();
//        manageItemUpgrades();

        if (!DropItem.isTimeFreezeActive()) {
            this.enemyManager.update();
        }

        if (this.lives > 0 && !this.ship.isDestroyed()) {
            boolean p1Right = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_D) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_RIGHT);
            boolean p1Left  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_A) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_LEFT);
            boolean p1Up    = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_W) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_UP);
            boolean p1Down  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_S) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_DOWN);
            boolean p1Fire  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_SPACE) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_ENTER);

            boolean isRightBorder = this.ship.getPositionX()
                    + this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
            boolean isLeftBorder = this.ship.getPositionX() - this.ship.getSpeed() < 1;
            boolean isUpBorder = this.ship.getPositionY() - this.ship.getSpeed() < SEPARATION_LINE_HEIGHT;
            boolean isDownBorder = this.ship.getPositionY()
                    + this.ship.getHeight() + this.ship.getSpeed() > ITEMS_SEPARATION_LINE_HEIGHT;

            if (p1Right && !isRightBorder) this.ship.moveRight();
            if (p1Left  && !isLeftBorder)  this.ship.moveLeft();
            if (p1Up    && !isUpBorder)    this.ship.moveUp();
            if (p1Down  && !isDownBorder)  this.ship.moveDown();

            if (p1Fire) {
                if (this.ship.shoot(this.bullets)) {
                    this.bulletsShot++;
                    AchievementManager.getInstance().onShotFired();
                }
            }
        }
        this.ship.update();
        cleanItems();
        collisionManager.manageCollisions();
        cleanBullets();

        if (this.lives == 0) {
            if (this.gameTimer.isRunning()) {
                this.gameTimer.stop();
            }
        }
        drawInfiniteMode();
    }

    /** Scale enemy health over time to increase difficulty */
    protected void scaleEnemyHealthOverTime() {
        // TODO: increase each enemy's health based on elapsedTime
    }

    /** Spawn a boss if the conditions are met */
    protected void spawnBoss() {
        if (this.bossSpawnCooldown.checkFinished() && !this.bossSpawned) {
            this.bossSpawnCooldown.reset();
            this.enemyManager.clear();
            if (BOSS_SPAWN_COUNT == 0) {
                BOSS_SPAWN_COUNT ++;
                this.omegaBoss = new OmegaBoss(Color.ORANGE, ITEMS_SEPARATION_LINE_HEIGHT);
                this.omegaBoss.attach(this);
                this.bossActive = true;
                this.bossSpawned = true;
                this.logger.info("Omega Boss has spawned!");
            }
            else {
                 this.finalBoss = new FinalBoss(this.width / 2 - 50, 50, this.width, this.height);
                 this.bossActive = true;
                 this.bossSpawned = true;
                 this.logger.info("Final Boss has spawned!");
            }
            this.logger.info("========== BOSS SPAWNED! ==========");
            this.logger.info("Elapsed time: " + (this.gameStartTime/ 1000) + " seconds");
            this.logger.info("All regular enemies have been cleared!");
            this.logger.info("===================================");
        }
    }
    private void updateBoss() {
        if (this.omegaBoss != null && !this.omegaBoss.isDestroyed()) {
//            this.bossActive = true;
            this.omegaBoss.update();
        } else if (this.omegaBoss != null && this.omegaBoss.isDestroyed()) {
            this.bossActive = false;
            this.bossSpawned = false;
            this.omegaBoss = null;
//            this.finalBoss = new FinalBoss(this.width / 2 - 50, 50, this.width, this.height);
//            this.logger.info("Final Boss has spawned!");
            return;
        }
        if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
//            this.bossActive = true;
            this.finalBoss.update();

            if (this.finalBoss.getHealPoint() > this.finalBoss.getMaxHp() / 4) {
                bossBullets.addAll(this.finalBoss.shoot1());
                bossBullets.addAll(this.finalBoss.shoot2());
            } else {
                if (!is_cleared) {
                    bossBullets.clear();
                    is_cleared = true;
                    logger.info("boss is angry");
                } else {
                    bossBullets.addAll(this.finalBoss.shoot3());
                }
            }

            Set<BossBullet> bulletsToRemove = new HashSet<>();

            for (BossBullet b : bossBullets) {
                b.update();
                if (b.isOffScreen(width, height)) {
                    bulletsToRemove.add(b);
                }
            }
            bossBullets.removeAll(bulletsToRemove);
        } else if (this.finalBoss != null && this.finalBoss.isDestroyed()) {
            this.finalBoss = null;
            this.bossActive = false;
            this.bossSpawned = false;
            this.is_cleared = false;
            this.logger.info("Boss defeated! Resuming normal spawning...");
        }
    }

    /** Update the score based on defeated enemies or achievements */
    protected void updateScore() {
        // TODO: increment score based on enemy defeats or milestones
    }
    /** Spawn enemies with random intervals */
    protected void spawnEnemies() {
        if (this.difficultyIncreaseCooldown.checkFinished()) {
            if (this.currentSpawnInterval > MIN_SPAWN_INTERVAL) {
                this.difficultyIncreaseCooldown.reset();
                this.currentSpawnInterval -= SPAWN_INTERVAL_DECREASE;
                if (this.currentSpawnInterval < MIN_SPAWN_INTERVAL) {
                    this.currentSpawnInterval = MIN_SPAWN_INTERVAL;
                }
                this.enemySpawnCooldown.setMilliseconds(this.currentSpawnInterval);

                this.logger.info("Difficulty increased! New spawn interval: " + this.currentSpawnInterval + "ms");
            }
        }
        if (this.enemySpawnCooldown.checkFinished() && !this.bossActive) {
            this.enemySpawnCooldown.reset();
            InfiniteEnemyShip.MovementPattern pattern;
            int x = 0, y = 0;
            int typeRoll = random.nextInt(3); // 0, 1, 2 (3 types)

            switch (typeRoll) {
                case 0: // STRAIGHT_DOWN
                    pattern = InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN;
                    x = random.nextInt(this.width - 30);
                    y = -50;
                    break;

                case 1: // ZIGZAG_DOWN
                    pattern = InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN;
                    x = random.nextInt(this.width - 30);
                    y = -50;
                    break;

                case 2: // HORIZONTAL_MOVE - horizontal movement
                default:
                    pattern = InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE;
                    int minY = SEPARATION_LINE_HEIGHT + 10;
                    int maxY = (int)(this.height * 0.3); // Upper 30% of screen
                    y = minY + random.nextInt(maxY - minY);

                    boolean startLeft = random.nextBoolean();
                    if (startLeft) {
                        x = -50; // Start from left
                    } else {
                        x = this.width + 10; // Start from right
                    }
                    break;
            }

            InfiniteEnemyShip enemy = new InfiniteEnemyShip(x, y, pattern, this.width, this.height);
            this.enemyManager.addEnemy(enemy);
        }
    }

    /** Draw the current game screen */
    protected void drawInfiniteMode() {
        drawManager.initDrawing(this);

        drawManager.drawEntity(this.ship, this.ship.getPositionX(), this.ship.getPositionY());

        this.enemyManager.draw();

        if (this.omegaBoss != null && !this.omegaBoss.isDestroyed()) {
            this.omegaBoss.draw(drawManager);
            drawManager.drawBossHealthBar(this.omegaBoss.getPositionX(), this.omegaBoss.getPositionY(), "OMEGA",
                    this.omegaBoss.getHealPoint(), this.omegaBoss.getMaxHp());
        }

        if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
            // Draw boss bullets
            for (BossBullet bossBullet : bossBullets) {
                drawManager.drawEntity(bossBullet, bossBullet.getPositionX(), bossBullet.getPositionY());
            }
            drawManager.drawEntity(this.finalBoss, this.finalBoss.getPositionX(), this.finalBoss.getPositionY());
            drawManager.drawBossHealthBar(this.finalBoss.getPositionX(), this.finalBoss.getPositionY(), "FINAL",
                    this.finalBoss.getHealPoint(), this.finalBoss.getMaxHp());
        }

        for (Bullet bullet : this.bullets)
            drawManager.drawEntity(bullet, bullet.getPositionX(), bullet.getPositionY());

        for (DropItem dropItem : this.dropItems)
            drawManager.drawEntity(dropItem, dropItem.getPositionX(), dropItem.getPositionY());

        // UI
        drawManager.drawScore(this, this.score);
        drawManager.drawLives(this, this.lives);
        drawManager.drawCoin(this, this.coin);
        drawManager.drawTime(this, this.elapsedTime);
        drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
        drawManager.drawHorizontalLine(this, ITEMS_SEPARATION_LINE_HEIGHT);

        if (this.healthPopupText != null && !this.healthPopupCooldown.checkFinished()) {
            drawManager.drawHealthPopup(this, this.healthPopupText);
        } else {
            this.healthPopupText = null;
        }

        drawManager.completeDrawing(this);
    }
    /**
     * Cleans bullets that go off screen.
     */
    private void cleanBullets() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            bullet.update();
            if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
                    || bullet.getPositionY() > this.height)
                recyclable.add(bullet);
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Cleans Items that go off screen.
     */

    private void cleanItems() {
        Set<DropItem> recyclable = new HashSet<DropItem>();
        for (DropItem dropItem : this.dropItems) {
            dropItem.update();
            if (dropItem.getPositionY() < SEPARATION_LINE_HEIGHT
                    || dropItem.getPositionY() > this.height)
                recyclable.add(dropItem);
        }
        this.dropItems.removeAll(recyclable);
        ItemPool.recycle(recyclable);
    }

    public Color getColorForHealth(final int health, final int maxHealth) {
        double ratio = (double) health / maxHealth;

        if (ratio > 0.75) {
            return new Color(0x3DDC84); // Green: Full HP
        } else if (ratio > 0.5) {
            return new Color(0xFFC107); // Yellow: Middle HP
        } else if (ratio > 0.25) {
            return new Color(0xFF9800); // Orange: Low HP
        } else {
            return new Color(0xF44336); // Red: Critical HP
        }
    }

    @Override
    public Set<Bullet> getBullets() { return this.bullets; }
    @Override
    public Set<BossBullet> getBossBullets() { return this.bossBullets; }
    @Override
    public EnemyShipFormation getEnemyShipFormation() { return null; }
    @Override
    public EnemyShipSpecialFormation getEnemyShipSpecialFormation() { return null; }
    @Override
    public InfiniteEnemyFormation getInfiniteEnemyFormation() { return this.enemyManager; }
    @Override
    public Set<DropItem> getDropItems() { return this.dropItems; }
    @Override
    public Ship getShip() { return this.ship; }
    @Override
    public void setLivesP1(int v) { this.lives = v; }
    @Override
    public int getLivesP1() { return this.lives; }
    @Override
    public void gainLife() { if (this.lives < this.maxLives) this.lives++; }
    @Override
    public MidBoss getOmegaBoss() { return this.omegaBoss; }
    @Override
    public FinalBoss getFinalBoss() { return this.finalBoss; }
    @Override
    public void addPointsFor(Bullet b, int p) { this.score += p; }
    @Override
    public void setCoin(int c) { this.coin = c; }
    @Override
    public int getCoin() { return this.coin; }
    @Override
    public void setShipsDestroyed(int shipsDestroyed) { this.shipsDestroyed = shipsDestroyed; }
    @Override
    public int getShipsDestroyed() { return this.shipsDestroyed; }
    @Override
    public Logger getLogger() { return this.logger; }
    @Override
    public Level getCurrentLevel() { return null; }
    @Override
    public void showHealthPopup(String msg) {
        this.healthPopupText = msg;
        this.healthPopupCooldown = Core.getCooldown(500);
        this.healthPopupCooldown.reset();
    }
    @Override
    public boolean isTwoPlayerMode() { return false; }
    @Override
    public boolean isLevelFinished() { return true; }
    @Override
    public Ship getShipP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public void setLivesP2(int v) { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public int getLivesP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public void gainLifeP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
}