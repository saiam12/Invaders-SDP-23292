package screen;

import java.awt.event.KeyEvent;
import java.util.List;

import engine.Achievement;
import engine.AchievementManager;
import engine.Core;

public class AchievementScreen extends Screen {

    public AchievementScreen(int width, int height, int fps) {
        super(width, height, fps);
        this.returnCode = 1; // Default return code
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public int run() {
        super.run();
        return this.returnCode;
    }

    @Override
    protected void update() {
        super.update();
        draw();
        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            this.isRunning = false;
        }
    }

    private void draw() {
        drawManager.initDrawing(this);
        List<Achievement> achievements = AchievementManager.getInstance().getAchievements();
        drawManager.drawAchievements(this, achievements);
        drawManager.completeDrawing(this);
    }
}
