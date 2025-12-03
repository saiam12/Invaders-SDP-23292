package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * Comprehensive unit tests for the User class.
 * Tests user authentication, achievement management, and edge cases.
 */
@DisplayName("User Class Tests")
public class UserTest {

    private User user;
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPass123";

    @BeforeEach
    public void setUp() {
        user = new User(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @DisplayName("Constructor should initialize user with username and password")
    public void testConstructorInitialization() {
        assertNotNull(user, "User should not be null");
        assertEquals(TEST_USERNAME, user.getUsername(), "Username should match");
        assertEquals(TEST_PASSWORD, user.getPassword(), "Password should match");
        assertNotNull(user.getAchievements(), "Achievements map should be initialized");
    }

    @Test
    @DisplayName("Constructor should initialize empty achievements map")
    public void testConstructorInitializesEmptyAchievements() {
        assertTrue(user.getAchievements().isEmpty(),
                "Achievements map should be empty on initialization");
    }

    @Test
    @DisplayName("getUsername should return correct username")
    public void testGetUsername() {
        assertEquals(TEST_USERNAME, user.getUsername());
    }

    @Test
    @DisplayName("getPassword should return correct password")
    public void testGetPassword() {
        assertEquals(TEST_PASSWORD, user.getPassword());
    }

    @Test
    @DisplayName("getAchievements should return modifiable map")
    public void testGetAchievementsReturnsModifiableMap() {
        Map<String, Boolean> achievements = user.getAchievements();
        assertNotNull(achievements);

        // Verify we can modify the map
        achievements.put("Test Achievement", true);
        assertTrue(achievements.containsKey("Test Achievement"));
        assertTrue(achievements.get("Test Achievement"));
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    public void testNullUsername() {
        User nullUser = new User(null, TEST_PASSWORD);
        assertNull(nullUser.getUsername());
        assertEquals(TEST_PASSWORD, nullUser.getPassword());
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    public void testNullPassword() {
        User nullPassUser = new User(TEST_USERNAME, null);
        assertEquals(TEST_USERNAME, nullPassUser.getUsername());
        assertNull(nullPassUser.getPassword());
    }

    @Test
    @DisplayName("Should handle empty username")
    public void testEmptyUsername() {
        User emptyUser = new User("", TEST_PASSWORD);
        assertEquals("", emptyUser.getUsername());
    }

    @Test
    @DisplayName("Should handle empty password")
    public void testEmptyPassword() {
        User emptyPassUser = new User(TEST_USERNAME, "");
        assertEquals("", emptyPassUser.getPassword());
    }

    @Test
    @DisplayName("Multiple achievements can be added and retrieved")
    public void testMultipleAchievements() {
        Map<String, Boolean> achievements = user.getAchievements();
        achievements.put("First Blood", true);
        achievements.put("Bad Sniper", false);
        achievements.put("Beginner", true);
        achievements.put("Bear Grylls", false);

        assertEquals(4, achievements.size());
        assertTrue(achievements.get("First Blood"));
        assertFalse(achievements.get("Bad Sniper"));
        assertTrue(achievements.get("Beginner"));
        assertFalse(achievements.get("Bear Grylls"));
    }

    @Test
    @DisplayName("Achievement status can be updated")
    public void testAchievementStatusUpdate() {
        Map<String, Boolean> achievements = user.getAchievements();
        achievements.put("Boss Slayer", false);
        assertFalse(achievements.get("Boss Slayer"));

        achievements.put("Boss Slayer", true);
        assertTrue(achievements.get("Boss Slayer"));
    }

    @Test
    @DisplayName("Achievements map can be cleared")
    public void testClearAchievements() {
        Map<String, Boolean> achievements = user.getAchievements();
        achievements.put("Test1", true);
        achievements.put("Test2", false);
        assertEquals(2, achievements.size());

        achievements.clear();
        assertTrue(achievements.isEmpty());
    }

    @Test
    @DisplayName("Username with special characters should be handled")
    public void testUsernameWithSpecialCharacters() {
        User specialUser = new User("user@test.com", TEST_PASSWORD);
        assertEquals("user@test.com", specialUser.getUsername());
    }

    @Test
    @DisplayName("Password with special characters should be handled")
    public void testPasswordWithSpecialCharacters() {
        User specialUser = new User(TEST_USERNAME, "P@ssw0rd!#$");
        assertEquals("P@ssw0rd!#$", specialUser.getPassword());
    }

    @Test
    @DisplayName("Very long username should be handled")
    public void testVeryLongUsername() {
        String longUsername = "a".repeat(1000);
        User longUser = new User(longUsername, TEST_PASSWORD);
        assertEquals(1000, longUser.getUsername().length());
    }

    @Test
    @DisplayName("Very long password should be handled")
    public void testVeryLongPassword() {
        String longPassword = "p".repeat(1000);
        User longUser = new User(TEST_USERNAME, longPassword);
        assertEquals(1000, longUser.getPassword().length());
    }

    @Test
    @DisplayName("Achievement with null key should be handled")
    public void testAchievementWithNullKey() {
        Map<String, Boolean> achievements = user.getAchievements();
        achievements.put(null, true);
        assertTrue(achievements.containsKey(null));
    }

    @Test
    @DisplayName("Achievement with null value should be handled")
    public void testAchievementWithNullValue() {
        Map<String, Boolean> achievements = user.getAchievements();
        achievements.put("Test", null);
        assertNull(achievements.get("Test"));
    }
}