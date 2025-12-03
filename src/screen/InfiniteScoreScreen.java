package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import engine.Cooldown;
import engine.Core;
import engine.GameState;
import engine.Score;

/**
 * Implements the score screen for Infinite Mode.
 */
public class InfiniteScoreScreen extends Screen{

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;

    /** Current score. */
    private int score;
    /** Player lives left. */
    private int livesRemaining;
    /** Total bullets shot by the player. */
    private int bulletsShot;
    /** Total ships destroyed by the player. */
    private int shipsDestroyed;
    /** Checks if current score is a new high score. */
    private boolean isNewRecord;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     * @param gameState
     *            Current game state.
     */
    public InfiniteScoreScreen(final int width, final int height, final int fps,
                       final GameState gameState) {
        super(width, height, fps);

        this.score = gameState.getScore();
        this.livesRemaining = gameState.getLivesRemaining();
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();
        this.isNewRecord = false;
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        draw();
        if (this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                // Return to main menu.
                this.returnCode = 1;
                this.isRunning = false;
            } else if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                // Play again.
                this.returnCode = 9;
                this.isRunning = false;
            }
        }
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawGameOver(this, this.inputDelay.checkFinished(),
                this.isNewRecord);
        drawManager.drawResults(this, this.score, this.livesRemaining,
                this.shipsDestroyed, (float) this.shipsDestroyed
                        / (this.bulletsShot > 0 ? this.bulletsShot : 1), this.isNewRecord);

        drawManager.completeDrawing(this);
    }


}

