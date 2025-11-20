package screen;

import engine.*;
import engine.GameState;
import engine.level.Level;
import entity.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class InfiniteScreen extends Screen implements CollisionContext {

    // Fields for game state
    /** Bonus life awarded this level. */
    private boolean bonusLife;
    /** Current lives of player. */
    private int currentLives;
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
//    Usage unclear for now
//    private static final int BOSS_EXPLOSION = 600;

    /** EnemyShip spawn interval :
     * every 20 seconds, enemyship spawn time is reduced by 0.1 seconds from 1 second to minimum 0.2 seconds*/
    private static final int INITIAL_SPAWN_INTERVAL = 1000;
    private static final int MIN_SPAWN_INTERVAL = 200;
    private static final int SPAWN_INTERVAL_DECREASE = 100;
    private static final int SPAWN_INTERVAL_DECREASE_TIME = 20000;
    private static int BOSS_SPAWN_COUNT = 0;
    /** Boss spawn interval: 2 minutes (120000 milliseconds) */
    private static final int BOSS_SPAWN_INTERVAL = 120000;

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
    /** bossBullets carry bullets which Boss fires */
    private Set<BossBullet> bossBullets;
    /** Total ships destroyed by the player. */
    private int shipsDestroyed;
    /** Manages collisions between entities. */
    private CollisionManager collisionManager;
    /** Set of all dropItems dropped by on screen ships. */
    private Set<DropItem> dropItems;
    /** Omega boss */
    private MidBoss omegaBoss;
    /** Final boss */
    private FinalBoss finalBoss;
    /** Health change popup. */
    private String healthPopupText;
    private Cooldown healthPopupCooldown;
    private boolean is_cleared = false;
//    Usage unclear for now
//    private Cooldown bossExplosionCooldown;

    /** Timer to track elapsed time in infinite mode */
    private GameTimer gameTimer;
    /** Elapsed time since the game started. */
    private long elapsedTime;
    /** Cooldown before screen changes */
    private Cooldown screenFinishedCooldown;
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
    }

    /** Initializes basic screen properties, and adds necessary elements. */
    public void initialize() {
        super.initialize();

        this.collisionManager = new CollisionManager(this);
        /** Initialize the bullet Boss fired */
        this.bossBullets = new HashSet<>();
        this.ship = new Ship(this.width / 2, ITEMS_SEPARATION_LINE_HEIGHT - 20,Color.green);
        this.ship.setPlayerId(1);
        this.dropItems = new HashSet<DropItem>();
        this.bossBullets = new HashSet<>();
//        Usage unclear for now
//        this.bossExplosionCooldown = Core
//                .getCooldown(BOSS_EXPLOSION);
        this.bullets = new HashSet<Bullet>();
        this.dropItems = new HashSet<DropItem>();

        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();

        this.currentSpawnInterval = INITIAL_SPAWN_INTERVAL;
        this.enemySpawnCooldown = Core.getCooldown(currentSpawnInterval);
        this.enemySpawnCooldown.reset();
        this.difficultyIncreaseCooldown = Core.getCooldown(SPAWN_INTERVAL_DECREASE_TIME);
        this.difficultyIncreaseCooldown.reset();

        this.bossSpawnCooldown = Core.getCooldown(BOSS_SPAWN_INTERVAL);
        this.bossSpawnCooldown.reset();
        this.bossSpawned = false;

        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();

        this.gameTimer = new GameTimer();
        this.elapsedTime = 0;
        this.finalBoss = null;
        this.omegaBoss = null;

        this.gameTimer.start();
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void update() {
        super.update();

        gameStartTime++;
        spawnEnemies();
        scaleEnemyHealthOverTime();
        spawnBoss();
        updateScore();
        manageItemUpgrades();

        if (this.lives > 0 && !this.ship.isDestroyed()) {
            boolean p1Right = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_D);
            boolean p1Left  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_A);
            boolean p1Up    = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_W);
            boolean p1Down  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_S);
            boolean p1Fire  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_SPACE);

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
        /**need to write enemyshipSpecialFormation*/
        cleanItems();
        collisionManager.manageCollisions();
        cleanBullets();

        if (this.lives == 0) {
            this.screenFinishedCooldown.reset();
            if (this.gameTimer.isRunning()) {
                this.gameTimer.stop();
            }
        }

    }

    /** Spawn enemies according to the elapsed time */
    protected void spawnEnemies() {
        // TODO: add enemy objects to the 'enemies' list based on elapsedTime
    }

    /** Scale enemy health over time to increase difficulty */
    protected void scaleEnemyHealthOverTime() {
        // TODO: increase each enemy's health based on elapsedTime
    }

    /** Spawn a boss if the conditions are met */
    protected void spawnBoss() {
        if (this.bossSpawnCooldown.checkFinished() && !this.bossSpawned) {
            this.bossSpawnCooldown.reset();
            clearAllEnemies();
            if (BOSS_SPAWN_COUNT == 0) {
                BOSS_SPAWN_COUNT ++;
                this.omegaBoss = new OmegaBoss(Color.ORANGE, ITEMS_SEPARATION_LINE_HEIGHT);
//                this.omegaBoss.attach(this);
                this.bossSpawned = true;
                this.logger.info("Omega Boss has spawned!");
            }
            else {
                 this.finalBoss = new FinalBoss(this.width / 2 - 50, 50, this.width, this.height);
                 this.bossSpawned = true;
                 this.logger.info("Final Boss has spawned!");
            }
            this.logger.info("========== BOSS SPAWNED! ==========");
            this.logger.info("Elapsed time: " + (this.gameStartTime/ 1000) + " seconds");
            this.logger.info("All regular enemies have been cleared!");
            this.logger.info("===================================");
        }
    }

    public void FinalBossManage(){
        if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
            this.finalBoss.update();
            /** called the boss shoot logic */
            if (this.finalBoss.getHealPoint() > this.finalBoss.getMaxHp() / 4) {
                bossBullets.addAll(this.finalBoss.shoot1());
                bossBullets.addAll(this.finalBoss.shoot2());
            } else {
                /** Is the bullet on the screen erased */
                if (!is_cleared) {
                    bossBullets.clear();
                    is_cleared = true;
                    logger.info("boss is angry");
                } else {
                    bossBullets.addAll(this.finalBoss.shoot3());
                }
            }

            /** bullets to erase */
            Set<BossBullet> bulletsToRemove = new HashSet<>();

            for (BossBullet b : bossBullets) {
                b.update();
                /** If the bullet goes off the screen */
                if (b.isOffScreen(width, height)) {
                    /** bulletsToRemove carry bullet */
                    bulletsToRemove.add(b);
                }
            }
            /** all bullets are removed */
            bossBullets.removeAll(bulletsToRemove);
            if (this.finalBoss != null && this.finalBoss.isDestroyed()) {
                bossSpawned = false;
            }
        }
    }

    private void clearAllEnemies() {
    }

    /** Update the score based on defeated enemies or achievements */
    protected void updateScore() {
        // TODO: increment score based on enemy defeats or milestones
    }

    /** Handle item acquisition and player upgrades */
    protected void manageItemUpgrades() {
        // TODO: check if player collects items and apply upgrades
    }

    /** Return the current game state */
    public GameState getInfiniteGameState() {
        // TODO: return a GameState object representing current game situation
        return null; // placeholder
    }

    /** Draw the current game screen */
    protected void drawInfiniteMode() {
        // TODO: render player, enemies, score, and UI elements on screen
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
    public EnemyShipFormation getEnemyShipFormation() { return null; } // must fill in
    @Override
    public EnemyShipSpecialFormation getEnemyShipSpecialFormation() { return null; } // must fill in
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
    public boolean isTwoPlayerMode() {return false;}
    @Override
    public boolean isLevelFinished() {throw new UnsupportedOperationException("Infinite mode does not support Player 2.");}
    @Override
    public Ship getShipP2() {throw new UnsupportedOperationException("Infinite mode does not support Player 2.");}
    @Override
    public void setLivesP2(int v) {throw new UnsupportedOperationException("Infinite mode does not support Player 2.");}
    @Override
    public int getLivesP2() {throw new UnsupportedOperationException("Infinite mode does not support Player 2.");}
    @Override
    public void gainLifeP2() {throw new UnsupportedOperationException("Infinite mode does not support Player 2.");}
}
