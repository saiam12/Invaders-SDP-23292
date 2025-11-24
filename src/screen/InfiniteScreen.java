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
    private boolean bossSpawned;
    /** Height of the items separation line (above items). */
    private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;
    /** Returns the Y-coordinate of the bottom boundary for enemies (above items HUD) */
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
    private GameTimer gameTimer;
    private long lastScoreAdded;
    private static final int timeInterval = 1000;
    private static final int pointsPerSecond = 10;



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
        this.gameTimer = new GameTimer();
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
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void update() {
        super.update();

        if (this.inputDelay.checkFinished()) {
            if (!this.gameTimer.isRunning()) {
                this.gameTimer.start();
                this.lastScoreAdded = System.currentTimeMillis();
            }

            spawnEnemies();
            scaleEnemyHealthOverTime();
            spawnBossIfNeeded();
            updateScore();
            manageItemUpgrades();

            if (this.lives <= 0 && !this.ship.isDestroyed()) {
                this.isRunning = false;
            }
        }
        // TODO: update player, check collisions, and remove defeated enemies
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
    protected void spawnBossIfNeeded() {
        if (!bossSpawned && gameStartTime > 300) { // Example: spawn boss after 5 minutes
            // TODO: create and add boss enemy
            bossSpawned = true;
        }
    }

    /** Update the score based on defeated enemies or achievements */
    protected void updateScore() {
        if (this.gameTimer.isRunning()) {
            long currentTime = System.currentTimeMillis();
            // Calculate time passed since last score update
            if (currentTime - lastScoreAdded >= timeInterval) {
                this.score += pointsPerSecond;
                this.lastScoreAdded = currentTime;
            }
        }
    }

    public GameState getGameState() {
        return new GameState(0,this.score,this.lives,0,
                this.bulletsShot,this.shipsDestroyed,this.coin,false);
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
