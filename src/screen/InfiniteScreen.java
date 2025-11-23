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
    /** Height of the items separation line (above items). */
    private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;

    private static final int INPUT_DELAY = 6000;
    private static final int SEPARATION_LINE_HEIGHT = 45;

    /** EnemyShip spawn interval - random between min and max */
    private static final int MIN_SPAWN_INTERVAL = 500;
    private static final int MAX_SPAWN_INTERVAL = 1500;

    private Cooldown enemySpawnCooldown;

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

    /** Timer to track elapsed time in infinite mode */
    private GameTimer gameTimer;
    /** Elapsed time since the game started. */
    private long elapsedTime;

    /** Custom formation manager for Infinite Mode enemies */
    private InfiniteEnemyFormation enemyManager;
    /** Random generator */
    private Random random;

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

        this.ship = new Ship(this.width / 2, ITEMS_SEPARATION_LINE_HEIGHT - 20, Color.green);
        this.ship.setPlayerId(1);
        this.dropItems = new HashSet<DropItem>();
        this.bullets = new HashSet<Bullet>();

        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();

        // Initialize enemy spawn cooldown
        int randomInterval = MIN_SPAWN_INTERVAL + random.nextInt(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
        this.enemySpawnCooldown = Core.getCooldown(randomInterval);
        this.enemySpawnCooldown.reset();

        this.gameTimer = new GameTimer();
        this.elapsedTime = 0;

        this.gameTimer.start();
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void update() {
        super.update();

        spawnEnemies();

        if (!DropItem.isTimeFreezeActive()) {
            this.enemyManager.update();
        }

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

    /** Spawn enemies with random intervals */
    protected void spawnEnemies() {
        if (this.enemySpawnCooldown.checkFinished()) {
            InfiniteEnemyShip.MovementPattern pattern;
            int x = 0, y = 0;
            int typeRoll = random.nextInt(3); // 0, 1, 2 (3가지 타입)

            switch (typeRoll) {
                case 0: // STRAIGHT_DOWN - Falls straight down
                    pattern = InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN;
                    x = random.nextInt(this.width - 30);
                    y = -50;
                    break;

                case 1: // ZIGZAG_DOWN - Falls in a zigzag pattern
                    pattern = InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN;
                    x = random.nextInt(this.width - 30);
                    y = -50;
                    break;

                case 2: // HORIZONTAL_MOVE - Horizontal movement
                default:
                    pattern = InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE;
                    y = random.nextInt((int)(this.height * 0.3)); // 상단 30% 영역
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

            // Set a random cooldown for the next spawn
            int randomInterval = MIN_SPAWN_INTERVAL + random.nextInt(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
            this.enemySpawnCooldown.setMilliseconds(randomInterval);
            this.enemySpawnCooldown.reset();
        }
    }

    /** Draw the current game screen */
    protected void drawInfiniteMode() {
        drawManager.initDrawing(this);

        drawManager.drawEntity(this.ship, this.ship.getPositionX(), this.ship.getPositionY());


        this.enemyManager.draw();

        for (Bullet bullet : this.bullets)
            drawManager.drawEntity(bullet, bullet.getPositionX(), bullet.getPositionY());

        for (DropItem dropItem : this.dropItems)
            drawManager.drawEntity(dropItem, dropItem.getPositionX(), dropItem.getPositionY());


        drawManager.drawScore(this, this.score);
        drawManager.drawLives(this, this.lives);
        drawManager.drawCoin(this, this.coin);
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

    // CollisionContext interface implementations
    @Override
    public Set<Bullet> getBullets() { return this.bullets; }
    @Override
    public Set<BossBullet> getBossBullets() { return new HashSet<>(); } // 무한 모드에서는 보스 불릿 없음
    @Override
    public EnemyShipFormation getEnemyShipFormation() { return null; } // 무한 모드에서는 사용 안함
    @Override
    public EnemyShipSpecialFormation getEnemyShipSpecialFormation() { return null; } // 무한 모드에서는 사용 안함
    @Override
    public InfiniteEnemyFormation getInfiniteEnemyFormation() { return this.enemyManager; } // 무한 모드 전용
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
    public MidBoss getOmegaBoss() { return null; } // 무한 모드에서는 보스 없음
    @Override
    public FinalBoss getFinalBoss() { return null; } // 무한 모드에서는 보스 없음
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
    public boolean isLevelFinished() { return true; } // 무한 모드에서는 항상 진행 중
    @Override
    public Ship getShipP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public void setLivesP2(int v) { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public int getLivesP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public void gainLifeP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
}