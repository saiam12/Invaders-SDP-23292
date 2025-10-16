package engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AchievementManager {
    private static AchievementManager instance;
    private List<Achievement> achievements;

    private AchievementManager() {
        achievements = new ArrayList<>();
        achievements.add(new Achievement("Beginner", "Clear level 1"));
        achievements.add(new Achievement("Intermediate", "Clear level 3"));
        achievements.add(new Achievement("Boss Slayer", "Defeat a boss"));
        achievements.add(new Achievement("Mr. Greedy", "Have more than 2000 coins"));

        loadAchievements();
    }

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void unlockAchievement(String name) {
        for (Achievement achievement : achievements) {
            if (achievement.getName().equals(name) && !achievement.isUnlocked()) {
                achievement.unlock();

                saveAchievements();
                break;
            }
        }
    }

    /**
     * Loads achievement status from file and updates the current achievement list.
     * <p>
     * Requests the FileManager to load saved achievement data, then updates
     * each achievement's unlocked state accordingly.
     * </p>
     *
     * @throws RuntimeException
     *             If an I/O error occurs while loading achievements.
     */
    public void loadAchievements() {
        try {
            // Ask FileManager to load saved achievement status
            java.util.Map<String, Boolean> unlockedStatus = Core.getFileManager().loadAchievements();
            // Update the state of each achievement based on the loaded data.
            for (Achievement achievement : achievements) {
                if (unlockedStatus.getOrDefault(achievement.getName(), false)) {
                    achievement.unlock();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load achievement file! Creating a new one.");
            // If file loading fails, attempt an initial save.
            saveAchievements();
        }
    }
    /**
     * Saves the current achievement status to file.
     * <p>
     * Requests the FileManager to write all current achievements to disk.
     * </p>
     *
     * @throws RuntimeException
     *             If an I/O error occurs while saving achievements.
     */
    private void saveAchievements() {
        try {
            // Ask FileManager to save all current achievement data
            Core.getFileManager().saveAchievements(achievements);
        } catch (IOException e) {
            System.err.println("Failed to save achievement file!");
            e.printStackTrace();
        }
    }
}
