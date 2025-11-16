package screen;

import engine.GameState;
import entity.*;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class InfiniteScreen extends Screen {

    // Fields for game state
    private int bonusLife;         // Bonus life awarded this level
    private int maxLives;          // Maximum lives player can have
    private int currentLives;      // Current lives of player
    private int score;             // Player score
    private Ship ship;             // Player object
    private long gameStartTime;       // Moment the game starts
    private boolean bossSpawned;   // Whether boss has appeared
    private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;
    /** Returns the Y-coordinate of the bottom boundary for enemies (above items HUD) */
    public static int getItemsSeparationLineHeight() {
        return ITEMS_SEPARATION_LINE_HEIGHT;
    }

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param bonusLife Checks if a bonus life is awarded this level.
     * @param maxLives Maximum number of lives.
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public InfiniteScreen(int bonusLife, int maxLives, int width, int height, int fps) {
        super(width, height, fps);
        this.bonusLife = bonusLife;
        this.maxLives = maxLives;
        this.currentLives = maxLives;
        this.score = 0;
        this.gameStartTime = 0;
        this.bossSpawned = false;
        this.ship = new Ship(this.width / 2, ITEMS_SEPARATION_LINE_HEIGHT - 20, Color.green); // Placeholder, implement Player class separately
    }

    /** Initializes basic screen properties, and adds necessary elements. */
    public void initializeInfiniteMode() {
        // TODO: add initial enemies, power-ups, and other game elements
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void updateInfiniteMode() {
        gameStartTime++;
        spawnEnemies();
        scaleEnemyHealthOverTime();
        spawnBossIfNeeded();
        updateScore();
        manageItemUpgrades();
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
}
