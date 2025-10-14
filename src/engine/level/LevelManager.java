package engine.level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private static final String MAPS_FILE = "maps.example.json";
    private List<Level> levels;

    public LevelManager() {
        loadLevels();
    }

    /**
     * Loads the levels from the maps.json file.
     */
    private void loadLevels() {
        try {
            // TODO: Implement JSON parsing here. When implemented, the hardcoded fallback below can be removed.
            // com.google.gson.Gson gson = new com.google.gson.Gson();
            // String jsonContent = new String(Files.readAllBytes(Paths.get(MAPS_FILE)));
            //
            // // 1. Parse the root object to get the list of level maps
            // Map<String, Object> root = gson.fromJson(jsonContent, new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());
            // List<Map<String, Object>> levelMaps = (List<Map<String, Object>>) root.get("levels");
            //
            // // 2. Create Level objects from the maps
            // this.levels = new ArrayList<>();
            // for (Map<String, Object> map : levelMaps) {
            //     this.levels.add(new Level(map));
            // }
        } catch (Exception e) {
            // In a real implementation, this error should be logged properly.
            e.printStackTrace();
        }

        // If loading from JSON fails or is not yet implemented, use hardcoded levels as a fallback.
        if (this.levels == null) {
            // TODO: This hardcoded data should be removed once JSON loading is functional.
            this.levels = new ArrayList<>();
            this.levels.add(new Level(1, 5, 4, 60, 2000));
            this.levels.add(new Level(2, 5, 5, 50, 2500));
            this.levels.add(new Level(3, 6, 5, 40, 1500));
            this.levels.add(new Level(4, 6, 6, 30, 1500));
            this.levels.add(new Level(5, 7, 6, 20, 1000));
            this.levels.add(new Level(6, 7, 7, 10, 1000));
            this.levels.add(new Level(7, 8, 7, 2, 500));
        }
    }
    /**
     * Gets the settings for a specific level.
     *
     * @param levelNumber The level number to get the settings for.
     * @return The Level object for the specified level number, or null if not found.
     */
    public Level getLevel(int levelNumber) {
        if (levels == null) {
            return null;
        }
        // Level numbers are 1-based, but list is 0-based.
        if (levelNumber > 0 && levelNumber <= levels.size()) {
            return levels.get(levelNumber - 1);
        }
        return null;
    }

    /**
     * Returns the total number of levels loaded.
     * @return The number of levels.
     */
    public int getNumberOfLevels() {
        if (this.levels == null) {
            return 0;
        }
        return this.levels.size();
    }
}
