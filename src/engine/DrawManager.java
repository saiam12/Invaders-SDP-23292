package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import entity.FinalBoss;
import entity.Ship;
import screen.Screen;
import entity.Entity;

/**
 * Manages screen drawing.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class DrawManager {

	/** Singleton instance of the class. */
	private static DrawManager instance;
	/** Current frame. */
	private static Frame frame;
	/** FileManager instance. */
	private static FileManager fileManager;
	/** Application logger. */
	private static Logger logger;
	/** Graphics context. */
	private static Graphics graphics;
	/** Buffer Graphics. */
	private static Graphics backBufferGraphics;
	/** Buffer image. */
	private static BufferedImage backBuffer;
	/** Normal sized font. */
	private static Font fontRegular;
	/** Normal sized font properties. */
	private static FontMetrics fontRegularMetrics;
	/** Big sized font. */
	private static Font fontBig;
	/** Big sized font properties. */
	private static FontMetrics fontBigMetrics;

	/** Sprite types mapped to their images. */
	private static Map<SpriteType, boolean[][]> spriteMap;

	/** Sprite types. */
	public static enum SpriteType {
		/** Player ship. */
		Ship,
		/** Destroyed player ship. */
		ShipDestroyed,
		/** Player bullet. */
		Bullet,
		/** Enemy bullet. */
		EnemyBullet,
		/** First enemy ship - first form. */
		EnemyShipA1,
		/** First enemy ship - second form. */
		EnemyShipA2,
		/** Second enemy ship - first form. */
		EnemyShipB1,
		/** Second enemy ship - second form. */
		EnemyShipB2,
		/** Third enemy ship - first form. */
		EnemyShipC1,
		/** Third enemy ship - second form. */
		EnemyShipC2,
		/** Bonus ship. */
		EnemyShipSpecial,
        /** Final boss - first form */
        FinalBoss,
        /** Final boss - second form */
        FinalBoss2,
		/** Destroyed enemy ship. */
		Explosion,
		/** Active sound button. */
		SoundOn,
		/** Deactive sound button. */
		SoundOff,
				/** Items */
		Item_MultiShot,
		Item_Atkspeed,
		Item_Penetrate,
		Item_Explode,
		Item_Slow,
		Item_Stop,
		Item_Push,
		Item_Shield,
		Item_Heal
	};

	/**
	 * Private constructor.
	 */
	private DrawManager() {
		fileManager = Core.getFileManager();
		logger = Core.getLogger();
		logger.info("Started loading resources.");

		try {
			spriteMap = new LinkedHashMap<SpriteType, boolean[][]>();

			spriteMap.put(SpriteType.Ship, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipDestroyed, new boolean[13][8]);
			spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);

            /** when final boss' spritetype is implemeted, need to add */
            //spriteMap.put(SpriteType.FinalBoss, new boolean[100][80]);
            //spriteMap.put(SpriteType.FinalBoss2, new boolean[100][80]);

			spriteMap.put(SpriteType.Explosion, new boolean[13][7]);
			spriteMap.put(SpriteType.SoundOn, new boolean[15][15]);
			spriteMap.put(SpriteType.SoundOff, new boolean[15][15]);
			spriteMap.put(SpriteType.Item_Explode, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Slow, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Stop, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Push, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Shield, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Heal, new boolean[5][5]);
			fileManager.loadSprite(spriteMap);
			logger.info("Finished loading the sprites.");

			// Font loading
			fontRegular = fileManager.loadFont(14f);
			fontBig = fileManager.loadFont(24f);
			logger.info("Finished loading the fonts.");


		} catch (IOException e) {
			logger.warning("Loading failed.");
		}
		catch (FontFormatException e) {
			logger.warning("Font formating failed.");
		}
	}

	/**
	 * Returns shared instance of DrawManager.
	 * 
	 * @return Shared instance of DrawManager.
	 */
	protected static DrawManager getInstance() {
		if (instance == null)
			instance = new DrawManager();
		return instance;
	}

	/**
	 * Sets the frame to draw the image on.
	 * 
	 * @param currentFrame
	 *            Frame to draw on.
	 */
	public void setFrame(final Frame currentFrame) {
		frame = currentFrame;
	}

	/**
	 * First part of the drawing process. Initialices buffers, draws the
	 * background and prepares the images.
	 * 
	 * @param screen
	 *            Screen to draw in.
	 */
	public void initDrawing(final Screen screen) {
		backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		graphics = frame.getGraphics();
		backBufferGraphics = backBuffer.getGraphics();

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics
				.fillRect(0, 0, screen.getWidth(), screen.getHeight());

		fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
		fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);

		// drawBorders(screen);
		// drawGrid(screen);
	}

	/**
	 * Draws the completed drawing on screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 */
	public void completeDrawing(final Screen screen) {
		graphics.drawImage(backBuffer, frame.getInsets().left,
				frame.getInsets().top, frame);
	}

	/**
	 * Draws an entity, using the apropiate image.
	 * 
	 * @param entity
	 *            Entity to be drawn.
	 * @param positionX
	 *            Coordinates for the left side of the image.
	 * @param positionY
	 *            Coordinates for the upper side of the image.
	 */
	public void drawEntity(final Entity entity, final int positionX,
			final int positionY) {
		boolean[][] image = spriteMap.get(entity.getSpriteType());

		backBufferGraphics.setColor(entity.getColor());
		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.drawRect(positionX + i * 2, positionY
							+ j * 2, 1, 1);

        /** draw hitbox of final boss because final boss' spritetype is not implemented(for test) */
        if( entity instanceof FinalBoss){
            backBufferGraphics.setColor(Color.RED);
            backBufferGraphics.drawRect(
                    entity.getPositionX(),
                    entity.getPositionY(),
                    entity.getWidth(),
                    entity.getHeight()
            );
        }
	}

	/**
	 * For debugging purpouses, draws the canvas borders.
	 * 
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawBorders(final Screen screen) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
		backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
		backBufferGraphics.drawLine(screen.getWidth() - 1, 0,
				screen.getWidth() - 1, screen.getHeight() - 1);
		backBufferGraphics.drawLine(0, screen.getHeight() - 1,
				screen.getWidth() - 1, screen.getHeight() - 1);
	}

	/**
	 * For debugging purpouses, draws a grid over the canvas.
	 * 
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawGrid(final Screen screen) {
		backBufferGraphics.setColor(Color.DARK_GRAY);
		for (int i = 0; i < screen.getHeight() - 1; i += 2)
			backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
		for (int j = 0; j < screen.getWidth() - 1; j += 2)
			backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
	}

	/**
	 * Draws current score on screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Current score.
	 */
	public void drawScore(final Screen screen, final int score) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		String scoreString = String.format("Score:%04d", score);
		backBufferGraphics.drawString(scoreString, screen.getWidth() - 120, 25);
	}

	/**
     * Draws the elapsed time on screen.
     *
     * @param screen
     * 				Screen to draw on.
     * @param milliseconds
     * 				Elapsed time in milliseconds.
     */
    public void drawTime(final Screen screen, final long milliseconds) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        String timeString = String.format("Time: %02d:%02d", minutes, seconds);
        backBufferGraphics.drawString(timeString, screen.getWidth() / 2 - fontRegularMetrics.stringWidth(timeString) / 2, 25);
    }


    /**
     * Draws current score on screen.
     *
     * @param screen
     *            Screen to draw on.
     * @param coin
     *            Current coin.
     */
    public void drawCoin(final Screen screen, final int coin) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        String scoreString = String.format("%03d$", coin);
        backBufferGraphics.drawString(scoreString, screen.getWidth() - 200, 25);
    }

	/**
	 * Draws number of remaining lives on screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param lives
	 *            Current lives.
	 */
	public void drawLives(final Screen screen, final int lives) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(Integer.toString(lives), 20, 25);
		Ship dummyShip = new Ship(0, 0);
		for (int i = 0; i < lives; i++)
			drawEntity(dummyShip, 40 + 35 * i, 10);
	}

	/**
	 * Draws the items HUD (shop items and dropped items).
	 * 
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawItemsHUD(final Screen screen) {
		ItemHUDManager itemHUD = ItemHUDManager.getInstance();
		itemHUD.initialize(screen);
		itemHUD.drawItems(screen, backBufferGraphics);
	}

    /**
     * Draws an achievement pop-up message on the screen.
     *
     * @param screen Screen where the pop-up will be drawn.
     *
     * @param text   The achievement message to display.
     */
    public void drawAchievementPopup(final Screen screen, final String text) {
        int popupWidth = 250;
        int popupHeight = 50;
        int x = screen.getWidth() / 2 - popupWidth / 2;
        int y = 80;

        backBufferGraphics.setColor(new Color(0, 0, 0, 200));
        backBufferGraphics.fillRoundRect(x, y, popupWidth, popupHeight, 15, 15);

        backBufferGraphics.setColor(Color.YELLOW);
        backBufferGraphics.drawRoundRect(x, y, popupWidth, popupHeight, 15, 15);

        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        drawCenteredRegularString(screen, text, y + popupHeight / 2 + 5);
    }

    /**
     * Draws a notification popup for changes in health
     *
     * @param screen
     *          Screen to draw on.
     * @param text
     *          Text to display change in health (+1 Health / -1 Health).
     */
    public void drawHealthPopup(final Screen screen, final String text) {
        int popupWidth = 250;
        int popupHeight = 40;
        int x = screen.getWidth() / 2 - popupWidth / 2;
        int y = 100;

        backBufferGraphics.setColor(new Color(0, 0, 0, 200));
        backBufferGraphics.fillRoundRect(x, y, popupWidth, popupHeight, 15, 15);

        Color textColor;
        if (text.startsWith("+")) {
            textColor = new Color(50, 255, 50);
        } else {
            textColor = new Color(255, 50, 50);
        }

        backBufferGraphics.setColor(textColor);
        drawCenteredBigString(screen, text, y + popupHeight / 2 + 5);
    }


	/**
	 * Draws a thick line from side to side of the screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param positionY
	 *            Y coordinate of the line.
	 */
	public void drawHorizontalLine(final Screen screen, final int positionY) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, positionY, screen.getWidth(), positionY);
		backBufferGraphics.drawLine(0, positionY + 1, screen.getWidth(),
				positionY + 1);
	}

	/**
	 * Draws game title.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawTitle(final Screen screen) {
		String titleString = "Invaders";
		String instructionsString =
				"select with w+s / arrows, confirm with space";

		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString,
				screen.getHeight() / 2);

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 3);
	}

	/**
	 * Draws main menu.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param option
	 *            Option selected.
	 */
	public void drawMenu(final Screen screen, final int option) {
		String playString = "Play";
		String highScoresString = "High scores";
		String shopString = "shop";
		String exitString = "exit";

		if (option == 2)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, playString,
				screen.getHeight() / 3 * 2);
		if (option == 3)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, highScoresString, screen.getHeight()
				/ 3 * 2 + fontRegularMetrics.getHeight() * 2);
		if (option == 4)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, shopString, screen.getHeight()
				/ 3 * 2 + fontRegularMetrics.getHeight() * 4);
		if (option == 0)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, exitString, screen.getHeight() / 3
				* 2 + fontRegularMetrics.getHeight() * 6);
	}

	/**
	 * Draws game results.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Score obtained.
	 * @param livesRemaining
	 *            Lives remaining when finished.
	 * @param shipsDestroyed
	 *            Total ships destroyed.
	 * @param accuracy
	 *            Total accuracy.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawResults(final Screen screen, final int score,
			final int livesRemaining, final int shipsDestroyed,
			final float accuracy, final boolean isNewRecord) {
		String scoreString = String.format("score %04d", score);
		String livesRemainingString = "lives remaining " + livesRemaining;
		String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
		String accuracyString = String
				.format("accuracy %.2f%%", accuracy * 100);

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, scoreString, screen.getHeight()
				/ height);
		drawCenteredRegularString(screen, livesRemainingString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 2);
		drawCenteredRegularString(screen, shipsDestroyedString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 4);
		drawCenteredRegularString(screen, accuracyString, screen.getHeight()
				/ height + fontRegularMetrics.getHeight() * 6);
	}

	/**
	 * Draws interactive characters for name input.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param name
	 *            Current name selected.
	 * @param nameCharSelected
	 *            Current character selected for modification.
	 */
	public void drawNameInput(final Screen screen, final char[] name,
			final int nameCharSelected) {
		String newRecordString = "New Record!";
		String introduceNameString = "Introduce name:";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredRegularString(screen, newRecordString, screen.getHeight()
				/ 4 + fontRegularMetrics.getHeight() * 10);
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, introduceNameString,
				screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 12);

		// 3 letters name.
		int positionX = screen.getWidth()
				/ 2
				- (fontRegularMetrics.getWidths()[name[0]]
						+ fontRegularMetrics.getWidths()[name[1]]
						+ fontRegularMetrics.getWidths()[name[2]]
								+ fontRegularMetrics.getWidths()[' ']) / 2;

		for (int i = 0; i < 3; i++) {
			if (i == nameCharSelected)
				backBufferGraphics.setColor(Color.GREEN);
			else
				backBufferGraphics.setColor(Color.WHITE);

			positionX += fontRegularMetrics.getWidths()[name[i]] / 2;
			positionX = i == 0 ? positionX
					: positionX
							+ (fontRegularMetrics.getWidths()[name[i - 1]]
									+ fontRegularMetrics.getWidths()[' ']) / 2;

			backBufferGraphics.drawString(Character.toString(name[i]),
					positionX,
					screen.getHeight() / 4 + fontRegularMetrics.getHeight()
							* 14);
		}
	}

	/**
	 * Draws basic content of game over screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param acceptsInput
	 *            If the screen accepts input.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawGameOver(final Screen screen, final boolean acceptsInput,
			final boolean isNewRecord) {
		String gameOverString = "Game Over";
		String continueOrExitString =
				"Press Space to play again, Escape to exit";

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, gameOverString, screen.getHeight()
				/ height - fontBigMetrics.getHeight() * 2);

		if (acceptsInput)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, continueOrExitString,
				screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 10);
	}

	/**
	 * Draws high score screen title and instructions.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawHighScoreMenu(final Screen screen) {
		String highScoreString = "High Scores";
		String instructionsString = "Press Space to return";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, highScoreString, screen.getHeight() / 8);

		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString,
				screen.getHeight() / 5);
	}

	/**
	 * Draws high scores.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param highScores
	 *            List of high scores.
	 */
	public void drawHighScores(final Screen screen,
			final List<Score> highScores) {
		backBufferGraphics.setColor(Color.WHITE);
		int i = 0;
		String scoreString = "";

		for (Score score : highScores) {
			scoreString = String.format("%s        %04d", score.getName(),
					score.getScore());
			drawCenteredRegularString(screen, scoreString, screen.getHeight()
					/ 4 + fontRegularMetrics.getHeight() * (i + 1) * 2);
			i++;
		}
	}

	/**
	 * Draws a centered string on regular font.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredRegularString(final Screen screen,
			final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws a centered string on big font.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredBigString(final Screen screen, final String string,
			final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontBigMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Countdown to game start.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param level
	 *            Game difficulty level.
	 * @param number
	 *            Countdown number.
	 * @param bonusLife
	 *            Checks if a bonus life is received.
	 */
	public void drawCountDown(final Screen screen, final int level,
			final int number, final boolean bonusLife) {
		int rectWidth = screen.getWidth();
		int rectHeight = screen.getHeight() / 6;
		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(0, screen.getHeight() / 2 - rectHeight / 2,
				rectWidth, rectHeight);
		backBufferGraphics.setColor(Color.GREEN);
		if (number >= 4)
			if (!bonusLife) {
				drawCenteredBigString(screen, "Level " + level,
						screen.getHeight() / 2
						+ fontBigMetrics.getHeight() / 3);
			} else {
				drawCenteredBigString(screen, "Level " + level
						+ " - Bonus life!",
						screen.getHeight() / 2
						+ fontBigMetrics.getHeight() / 3);
			}
		else if (number != 0)
			drawCenteredBigString(screen, Integer.toString(number),
					screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
		else
			drawCenteredBigString(screen, "GO!", screen.getHeight() / 2
					+ fontBigMetrics.getHeight() / 3);
	}
	/**
	 * Draws the complete shop screen with all items and levels.
	 *
	 * @param screen Screen to draw on.
	 * @param coinBalance Player's current coin balance.
	 * @param selectedItem Currently selected item index.
	 * @param selectionMode 0 = selecting item, 1 = selecting level.
	 * @param selectedLevel Currently selected level for purchase.
	 * @param totalItems Total number of items.
	 * @param itemNames Array of item names.
	 * @param itemDescriptions Array of item descriptions.
	 * @param itemPrices 2D array of prices [item][level].
	 * @param maxLevels Array of maximum levels per item.
	 * @param shopScreen Reference to shop screen for getting current levels.
	 */
	public void drawShopScreen(final Screen screen, final int coinBalance,
							   final int selectedItem, final int selectionMode,
							   final int selectedLevel, final int totalItems,
							   final String[] itemNames,
							   final String[] itemDescriptions,
							   final int[][] itemPrices,
							   final int[] maxLevels,
							   final screen.ShopScreen shopScreen) {
		// Draw title
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, "SHOP", screen.getHeight() / 8);

		// Draw coin balance
		backBufferGraphics.setColor(Color.YELLOW);
		String balanceString = String.format("Your Balance: %d coins", coinBalance);
		drawCenteredRegularString(screen, balanceString, 120);

		// Draw instructions based on mode
		backBufferGraphics.setColor(Color.GRAY);
		String instructions = "";
		if (selectionMode == 0) {
			instructions = "W/S: Navigate | SPACE: Select | ESC: Exit";
		} else {
			instructions = "A/D: Change Level | SPACE: Buy | ESC: Back";
		}
		drawCenteredRegularString(screen, instructions, 145);

		// Draw separator line
		//backBufferGraphics.setColor(Color.GREEN);
		//drawHorizontalLine(screen, 165);

		// FIXED: Draw items with proper spacing
		int currentY = 170;  // Start position
		int baseSpacing = 65;  // Space between each item

		for (int i = 0; i < totalItems; i++) {
			boolean isSelected = (i == selectedItem) && (selectionMode == 0);
			boolean isLevelSelection = (i == selectedItem && selectionMode == 1);
			int currentLevel = shopScreen.getItemCurrentLevel(i);

			drawShopItem(screen, itemNames[i], itemDescriptions[i],
					itemPrices[i], maxLevels[i], currentLevel,
					currentY, isSelected, coinBalance,
					isLevelSelection, selectedLevel);

			// Add extra space when showing level selection buttons
			if (isLevelSelection) {
				currentY += baseSpacing + 55;
			} else {
				currentY += baseSpacing;
			}
		}

		// Draw exit option
		int exitY = screen.getHeight() - 25;
		if (selectedItem == totalItems && selectionMode == 0) {
			backBufferGraphics.setColor(Color.GREEN);
		} else {
			backBufferGraphics.setColor(Color.WHITE);
		}

        if (shopScreen.betweenLevels)
        {
            drawCenteredRegularString(screen, "< Back to Game >", exitY);
        } else {
            drawCenteredRegularString(screen, "< Back to Main Menu >", exitY);
        }
	}

	/**
	 * Draws a single shop item with level indicators.
	 *
	 * @param screen Screen to draw on.
	 * @param itemName Name of the item.
	 * @param description Description of the item.
	 * @param prices Array of prices for each level.
	 * @param maxLevel Maximum level for this item.
	 * @param currentLevel Current owned level.
	 * @param yPosition Y position to draw.
	 * @param isSelected Whether item is selected.
	 * @param playerCoins Player's current coin balance.
	 * @param isLevelSelection Whether in level selection mode.
	 * @param selectedLevel Currently selected level for purchase.
	 */
	public void drawShopItem(final Screen screen, final String itemName,
							 final String description, final int[] prices,
							 final int maxLevel, final int currentLevel,
							 final int yPosition, final boolean isSelected,
							 final int playerCoins, final boolean isLevelSelection,
							 final int selectedLevel) {

		// Draw item name
		if (isSelected || isLevelSelection) {
			backBufferGraphics.setColor(Color.GREEN);
		} else {
			backBufferGraphics.setColor(Color.WHITE);
		}

		String levelInfo = currentLevel > 0 ?
				" [Lv." + currentLevel + "/" + maxLevel + "]" : " [Not Owned]";
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(itemName + levelInfo, 30, yPosition);

		// Draw description if selected
		if (isSelected || isLevelSelection) {
			backBufferGraphics.setColor(Color.GRAY);
			backBufferGraphics.drawString(description, 30, yPosition + 15);
		}

		// Draw level options if in level selection mode for this item
		if (isLevelSelection) {
			int levelStartX = 30;
			int levelY = yPosition + 35;

			for (int lvl = 1; lvl <= maxLevel; lvl++) {
				int price = prices[lvl - 1];
				boolean canAfford = playerCoins >= price;
				boolean isOwned = currentLevel >= lvl;
				boolean isThisLevel = (lvl == selectedLevel);

				// Set color based on state
				if (isOwned) {
					backBufferGraphics.setColor(Color.DARK_GRAY);
				} else if (isThisLevel) {
					backBufferGraphics.setColor(Color.GREEN);
				} else if (canAfford) {
					backBufferGraphics.setColor(Color.WHITE);
				} else {
					backBufferGraphics.setColor(Color.RED);
				}

				String levelText = "Lv." + lvl + " (" + price + "$)";
				if (isOwned) {
					levelText = "Lv." + lvl + " [OWNED]";
				}

				backBufferGraphics.drawString(levelText,
						levelStartX + ((lvl - 1) * 95),
						levelY);
			}
		}
	}

	/**
	 * Draws purchase feedback message.
	 *
	 * @param screen Screen to draw on.
	 * @param message Feedback message to display.
	 */
	public void drawShopFeedback(final Screen screen, final String message) {
		int popupWidth = 300;
		int popupHeight = 50;
		int x = screen.getWidth() / 2 - popupWidth / 2;
		int y = 70;

		// Draw background
		backBufferGraphics.setColor(new Color(0, 0, 0, 200));
		backBufferGraphics.fillRoundRect(x, y, popupWidth, popupHeight, 15, 15);

		// Draw border
		if (message.contains("Purchased")) {
			backBufferGraphics.setColor(Color.GREEN);
		} else if (message.contains("Not enough") || message.contains("failed")) {
			backBufferGraphics.setColor(Color.RED);
		} else {
			backBufferGraphics.setColor(Color.YELLOW);
		}
		backBufferGraphics.drawRoundRect(x, y, popupWidth, popupHeight, 15, 15);

		// Draw message
		backBufferGraphics.setFont(fontRegular);
		drawCenteredRegularString(screen, message, y + popupHeight / 2 + 5);
	}
}
