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
import java.awt.event.KeyEvent;

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

    private static final int INPUT_DELAY = 6000;
    private static final int SEPARATION_LINE_HEIGHT = 45;

    /** EnemyShip spawn interval - random between min and max */
    private static final int MIN_SPAWN_INTERVAL = 500;
    private static final int MAX_SPAWN_INTERVAL = 1500;

    private Cooldown enemySpawnCooldown;
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
    /** Shop screen instance for infinite mode. */
    private ShopScreen shopScreen;
    /** Cooldown for shop toggle. */
    private Cooldown shopToggleCooldown;
    /** Whether shop is currently open. */
    private boolean isShopOpen;

    /** Timer to track elapsed time in infinite mode */
    private GameTimer gameTimer;
    /** Elapsed time since the game started. */
    private long elapsedTime;

    /** Custom formation manager for Infinite Mode enemies */
    private InfiniteEnemyFormation enemyManager;
    /** Random generator */
    private Random random;
    /** Selected item in shop */
    private int selectedShopItem = 0;
    /** Shop selection cooldown */
    private Cooldown shopSelectionCooldown;
    /** Maximum levels for each item. */
    private static final int[] MAX_LEVELS = {3, 5, 2, 3, 5};
    /** Item levels */
    private int[] itemlevel = {
            0,                          // MultiShot: Max Level 3
            0,                          // Rapid Fire: Max Level 5
            0,                          // Penetration: Max Level 2
            0,                          // Bullet Speed: MAx Level 3
            0                           // Ship Speed: Max Level 5
    };
    /** Item prices */
    private int[][] prices = {
            {30, 60, 100},              // MultiShot: Level 1-3
            {25, 50, 75, 100, 150},     // Rapid Fire: Level 1-5
            {40, 80},                   // Penetration: Level 1-2
            {35, 70, 110},              // Bullet Speed: Level 1-3
            {20, 40, 60, 80, 100}       // Ship Speed: Level 1-5
    };

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

        // initialize spawn cooldown
        int randomInterval = MIN_SPAWN_INTERVAL + random.nextInt(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
        this.enemySpawnCooldown = Core.getCooldown(randomInterval);
        this.enemySpawnCooldown.reset();

        this.gameTimer = new GameTimer();
        this.elapsedTime = 0;

        this.gameTimer.start();

        this.shopToggleCooldown = Core.getCooldown(300);
        this.shopToggleCooldown.reset();
        this.shopSelectionCooldown = Core.getCooldown(200);
        this.shopSelectionCooldown.reset();
        this.isShopOpen = false;
        this.shopScreen = null;
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void update() {
        super.update();

        handleShopToggle();

        // If shop is open, pause game and skip game logic
        if (isShopOpen) {
            // Pause game timer
            if (this.gameTimer.isRunning()) {
                this.gameTimer.stop();
            }
            // Still draw game frame (so overlay is visible)
            drawInfiniteMode();
            // Handle shop-specific input (navigation, buy, close)
            handleShopInput();
            return; // Skip all game logic
        }

        // Resume game timer when shop closes
        if (!this.gameTimer.isRunning()) {
            this.gameTimer.start();
        }

        gameStartTime++;
        spawnEnemies();
        scaleEnemyHealthOverTime();
        spawnBossIfNeeded();
        updateScore();
        manageItemUpgrades();
        drawInfiniteMode();

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

    /** Spawn enemies according to the elapsed time */
    protected void spawnEnemies() {
        if (this.enemySpawnCooldown.checkFinished()) {
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

            // Set random cooldown for next spawn
            int randomInterval = MIN_SPAWN_INTERVAL + random.nextInt(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
            this.enemySpawnCooldown.setMilliseconds(randomInterval);
            this.enemySpawnCooldown.reset();
        }
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
        drawManager.initDrawing(this);

        drawManager.drawEntity(ship, ship.getPositionX(), ship.getPositionY());
        enemyManager.draw();
        bullets.forEach(b -> drawManager.drawEntity(b, b.getPositionX(), b.getPositionY()));
        dropItems.forEach(d -> drawManager.drawEntity(d, d.getPositionX(), d.getPositionY()));

        // UI
        drawManager.drawScore(this, score);
        drawManager.drawLives(this, lives);
        drawManager.drawCoin(this, coin);
        drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
        drawManager.drawHorizontalLine(this, ITEMS_SEPARATION_LINE_HEIGHT);

        if (!isShopOpen) {
            drawManager.drawCenteredRegularString(this, "Press T to open shop", height - 30);
        } else {
            drawShopOverlay();
        }

        if (healthPopupText != null && !healthPopupCooldown.checkFinished()) {
            drawManager.drawHealthPopup(this, healthPopupText);
        } else {
            healthPopupText = null;
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

    /**
     * Handles opening and closing the shop with T key.
     */
    private void handleShopToggle() {
        if (shopToggleCooldown.checkFinished() && inputManager.isKeyDown(KeyEvent.VK_T)) {
            if (isShopOpen) closeShop();
            else openShop();
            shopToggleCooldown.reset();
        }
    }

    /**
     * Opens the shop screen.
     */
    private void openShop() {
        isShopOpen = true;
        shopSelectionCooldown.reset();
        logger.info("Shop opened");
        if (gameTimer.isRunning()) gameTimer.stop();
    }

    /**
     * Closes the shop screen.
     */
    private void closeShop() {
        isShopOpen = false;
        logger.info("Shop closed");
        if (!gameTimer.isRunning()) gameTimer.start();
    }

    private void handleShopInput() {
        if (this.shopSelectionCooldown.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                closeShop();
                this.shopSelectionCooldown.reset();
                return;
            }

            if (inputManager.isKeyDown(KeyEvent.VK_W) || inputManager.isKeyDown(KeyEvent.VK_UP)) {
                selectedShopItem--;
                if (selectedShopItem < 0) selectedShopItem = 4;
                this.shopSelectionCooldown.reset();
            }

            if (inputManager.isKeyDown(KeyEvent.VK_S) || inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
                selectedShopItem++;
                if (selectedShopItem > 4) selectedShopItem = 0;
                this.shopSelectionCooldown.reset();
            }

            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                attemptPurchase(selectedShopItem);
                this.shopSelectionCooldown.reset();
            }
        }
    }

    private void attemptPurchase(int itemIndex) {
        String[] names = {"Multi Shot", "Rapid Fire", "Penetration", "Bullet Speed", "Ship Speed"};

        int currentLevel = itemlevel[itemIndex];

        if (currentLevel >= MAX_LEVELS[itemIndex]) {
            this.logger.info(names[itemIndex] + " is already at max level!");
            return;
        }

        int price = prices[itemIndex][currentLevel];
        if (this.coin >= price) {
            this.coin -= price;
            this.gameState.deductCoins(price);
            itemlevel[itemIndex]++;

            applyItemUpgrade(itemIndex);

            this.logger.info("Purchased " + names[itemIndex] + " for " + price + " coins. New level: " + itemlevel[itemIndex]);
        } else {
            this.logger.info("Not enough coins for " + names[itemIndex] + ". Need: " + price + ", have: " + this.coin);
        }
    }

    private void applyItemUpgrade(int itemIndex) {
        switch (itemIndex) {
            case 0: // Multi Shot
                entity.ShopItem.setMultiShotLevel(entity.ShopItem.getMultiShotLevel() + 1);
                break;
            case 1: // Rapid Fire
                entity.ShopItem.setRapidFireLevel(entity.ShopItem.getRapidFireLevel() + 1);
                break;
            case 2: // Penetration
                entity.ShopItem.setPenetrationLevel(entity.ShopItem.getPenetrationLevel() + 1);
                break;
            case 3: // Bullet Speed
                entity.ShopItem.setBulletSpeedLevel(entity.ShopItem.getBulletSpeedLevel() + 1);
                break;
            case 4: // Ship Speed
                entity.ShopItem.setSHIPSPEED(entity.ShopItem.getSHIPSpeedCOUNT() + 5);
                break;
        }
    }

    private void drawShopOverlay() {
        Graphics g = drawManager.getBackBufferGraphics();
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.GREEN);
        drawManager.drawCenteredBigString(this, "SHOP", height / 8);
        drawManager.drawCenteredRegularString(this, "Coins: " + coin, height / 8 + 40);

        String[] names = {"Multi Shot", "Rapid Fire", "Penetration", "Bullet Speed", "Ship Speed"};
        int startY = height / 3;

        for (int i = 0; i < names.length; i++) {
            String text;
            if (itemlevel[i] >= MAX_LEVELS[i]) {
                text = names[i] + " - MAX LEVEL";
            } else {
                text = names[i] + " - " + prices[i][itemlevel[i]] + " coins";
            }

            if (i == selectedShopItem) {
                drawManager.drawCenteredBigString(this, "> " + text + " <", startY + i * 40);
            } else {
                drawManager.drawCenteredRegularString(this, text, startY + i * 40);
            }
        }

        drawManager.drawCenteredRegularString(this,
                "W/S: Select | SPACE: Buy | ESC/T: Close",
                height - 30);
    }

    @Override
    public final int run() {
        super.run();
        this.logger.info("Infinite mode ended with score: " + this.score);
        return this.returnCode;
    }

    // CollisionContext interface implementations
    @Override
    public Set<Bullet> getBullets() { return this.bullets; }
    @Override
    public Set<BossBullet> getBossBullets() { return new HashSet<>(); }
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
    public MidBoss getOmegaBoss() { return null; }
    @Override
    public FinalBoss getFinalBoss() { return null; }
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