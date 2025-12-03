package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String username;
    private String password;
    private Map<String, Boolean> achievements;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.achievements = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }



    public Map<String, Boolean> getAchievements() {
        return achievements;
    }
}