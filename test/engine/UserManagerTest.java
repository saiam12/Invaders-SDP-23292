package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserManager class.
 * Tests user authentication, registration, persistence, and singleton behavior.
 */
class UserManagerTest {

    private UserManager userManager;
    private List<User> mockUsers;
    private FileManager mockFileManager;
    private MockedStatic<Core> mockedCore;

    @BeforeEach
    void setUp() {
        // Reset singleton instance for each test
        try {
            java.lang.reflect.Field instance = UserManager.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            fail("Failed to reset UserManager singleton: " + e.getMessage());
        }

        // Create mock users
        mockUsers = new ArrayList<>();
        User user1 = new User("testuser1", "password123");
        User user2 = new User("testuser2", "securepass");
        User user3 = new User("admin", "adminpass");
        mockUsers.add(user1);
        mockUsers.add(user2);
        mockUsers.add(user3);

        // Mock FileManager and Core
        mockFileManager = mock(FileManager.class);
        mockedCore = mockStatic(Core.class);
        mockedCore.when(Core::getFileManager).thenReturn(mockFileManager);

        try {
            when(mockFileManager.loadUsers()).thenReturn(mockUsers);
        } catch (IOException e) {
            fail("Mock setup failed: " + e.getMessage());
        }

        userManager = UserManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        if (mockedCore != null) {
            mockedCore.close();
        }
    }

    @Test
    void testGetInstance_ReturnsSameInstance() {
        UserManager instance1 = UserManager.getInstance();
        UserManager instance2 = UserManager.getInstance();
        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    @Test
    void testGetInstance_InitializesUsersFromFile() throws IOException {
        verify(mockFileManager, times(1)).loadUsers();
        assertEquals(3, userManager.getUsers().size(), "Should load 3 users from file");
    }

    @Test
    void testGetUsers_ReturnsUserList() {
        List<User> users = userManager.getUsers();
        assertNotNull(users, "User list should not be null");
        assertEquals(3, users.size(), "Should return all loaded users");
    }

    @Test
    void testAuthenticateUser_ValidCredentials_ReturnsTrue() {
        boolean result = userManager.authenticateUser("testuser1", "password123");
        assertTrue(result, "Authentication should succeed with valid credentials");
    }

    @Test
    void testAuthenticateUser_InvalidPassword_ReturnsFalse() {
        boolean result = userManager.authenticateUser("testuser1", "wrongpassword");
        assertFalse(result, "Authentication should fail with invalid password");
    }

    @Test
    void testAuthenticateUser_NonexistentUser_ReturnsFalse() {
        boolean result = userManager.authenticateUser("nonexistent", "anypassword");
        assertFalse(result, "Authentication should fail for nonexistent user");
    }

    @Test
    void testAuthenticateUser_NullUsername_ReturnsFalse() {
        boolean result = userManager.authenticateUser(null, "password");
        assertFalse(result, "Authentication should fail with null username");
    }

    @Test
    void testAuthenticateUser_NullPassword_ReturnsFalse() {
        boolean result = userManager.authenticateUser("testuser1", null);
        assertFalse(result, "Authentication should fail with null password");
    }

    @Test
    void testAuthenticateUser_EmptyUsername_ReturnsFalse() {
        boolean result = userManager.authenticateUser("", "password");
        assertFalse(result, "Authentication should fail with empty username");
    }

    @Test
    void testAuthenticateUser_EmptyPassword_ReturnsFalse() {
        boolean result = userManager.authenticateUser("testuser1", "");
        assertFalse(result, "Authentication should fail with empty password");
    }

    @Test
    void testAuthenticateUser_CaseSensitiveUsername() {
        boolean result = userManager.authenticateUser("TESTUSER1", "password123");
        assertFalse(result, "Username should be case-sensitive");
    }

    @Test
    void testRegisterUser_NewUser_ReturnsTrue() throws IOException {
        boolean result = userManager.registerUser("newuser", "newpass");
        assertTrue(result, "Registration should succeed for new user");
        assertEquals(4, userManager.getUsers().size(), "User list should grow by 1");
        verify(mockFileManager, times(1)).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_ExistingUsername_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser("testuser1", "anypassword");
        assertFalse(result, "Registration should fail for existing username");
        assertEquals(3, userManager.getUsers().size(), "User list should not change");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_NullUsername_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser(null, "password");
        assertFalse(result, "Registration should fail with null username");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_NullPassword_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser("newuser", null);
        assertFalse(result, "Registration should fail with null password");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_EmptyUsername_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser("", "password");
        assertFalse(result, "Registration should fail with empty username");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_EmptyPassword_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser("newuser", "");
        assertFalse(result, "Registration should fail with empty password");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_WhitespaceUsername_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser("   ", "password");
        assertFalse(result, "Registration should fail with whitespace-only username");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_WhitespacePassword_ReturnsFalse() throws IOException {
        boolean result = userManager.registerUser("newuser", "   ");
        assertFalse(result, "Registration should fail with whitespace-only password");
        verify(mockFileManager, never()).saveUsers(anyList());
    }

    @Test
    void testRegisterUser_SaveFailure_HandlesIOException() throws IOException {
        doThrow(new IOException("Save failed")).when(mockFileManager).saveUsers(anyList());
        
        boolean result = userManager.registerUser("newuser", "newpass");
        // Should still return true even if save fails (user added to memory)
        assertTrue(result, "Registration should succeed even if save fails");
        assertEquals(4, userManager.getUsers().size(), "User should be added to memory");
    }

    @Test
    void testGetUserByUsername_ExistingUser_ReturnsUser() {
        User user = userManager.getUserByUsername("testuser1");
        assertNotNull(user, "Should find existing user");
        assertEquals("testuser1", user.getUsername(), "Should return correct user");
    }

    @Test
    void testGetUserByUsername_NonexistentUser_ReturnsNull() {
        User user = userManager.getUserByUsername("nonexistent");
        assertNull(user, "Should return null for nonexistent user");
    }

    @Test
    void testGetUserByUsername_NullUsername_ReturnsNull() {
        User user = userManager.getUserByUsername(null);
        assertNull(user, "Should return null for null username");
    }

    @Test
    void testGetUserByUsername_EmptyUsername_ReturnsNull() {
        User user = userManager.getUserByUsername("");
        assertNull(user, "Should return null for empty username");
    }

    @Test
    void testGetUserByUsername_CaseSensitive() {
        User user = userManager.getUserByUsername("TESTUSER1");
        assertNull(user, "Username lookup should be case-sensitive");
    }

    @Test
    void testLoadUsers_FileNotFound_CreatesEmptyList() throws IOException {
        // Reset and configure mock to throw IOException
        try {
            java.lang.reflect.Field instance = UserManager.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            fail("Failed to reset singleton: " + e.getMessage());
        }

        when(mockFileManager.loadUsers()).thenThrow(new IOException("File not found"));
        
        UserManager newManager = UserManager.getInstance();
        assertNotNull(newManager.getUsers(), "User list should be initialized even on error");
        assertTrue(newManager.getUsers().isEmpty(), "User list should be empty when file not found");
    }

    @Test
    void testRegisterUser_MultipleNewUsers_AllPersisted() throws IOException {
        userManager.registerUser("user4", "pass4");
        userManager.registerUser("user5", "pass5");
        userManager.registerUser("user6", "pass6");

        assertEquals(6, userManager.getUsers().size(), "Should have 6 users total");
        verify(mockFileManager, times(3)).saveUsers(anyList());
    }

    @Test
    void testAuthenticateUser_AfterRegistration_Succeeds() throws IOException {
        userManager.registerUser("newuser", "newpass");
        boolean authenticated = userManager.authenticateUser("newuser", "newpass");
        assertTrue(authenticated, "Should authenticate newly registered user");
    }

    @Test
    void testRegisterUser_SpecialCharactersInUsername() throws IOException {
        boolean result = userManager.registerUser("user@123", "password");
        assertTrue(result, "Should allow special characters in username");
    }

    @Test
    void testRegisterUser_SpecialCharactersInPassword() throws IOException {
        boolean result = userManager.registerUser("newuser", "p@ssw0rd!#$");
        assertTrue(result, "Should allow special characters in password");
    }

    @Test
    void testRegisterUser_LongUsername() throws IOException {
        String longUsername = "a".repeat(100);
        boolean result = userManager.registerUser(longUsername, "password");
        assertTrue(result, "Should handle long usernames");
    }

    @Test
    void testRegisterUser_LongPassword() throws IOException {
        String longPassword = "p".repeat(200);
        boolean result = userManager.registerUser("newuser", longPassword);
        assertTrue(result, "Should handle long passwords");
    }

    @Test
    void testThreadSafety_ConcurrentRegistrations() throws InterruptedException {
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    userManager.registerUser("concurrent" + index, "pass" + index);
                } catch (IOException e) {
                    fail("Registration failed: " + e.getMessage());
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Should have original 3 + 10 new users
        assertTrue(userManager.getUsers().size() >= 13, 
            "Should handle concurrent registrations");
    }
}