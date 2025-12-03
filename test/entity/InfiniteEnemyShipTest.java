package entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import entity.InfiniteEnemyShip.MovementPattern;

class InfiniteEnemyShipTest {

    private InfiniteEnemyShip straightShip;
    private InfiniteEnemyShip zigzagShip;
    private int screenWidth = 800;
    private int screenHeight = 600;

    @BeforeEach
    void setUp() {
        // Create test enemies at position (100, 100)
        straightShip = new InfiniteEnemyShip(100, 100, MovementPattern.STRAIGHT_DOWN, screenWidth, screenHeight);
        zigzagShip = new InfiniteEnemyShip(100, 100, MovementPattern.ZIGZAG_DOWN, screenWidth, screenHeight);
    }

    @Test
    @DisplayName("Test Straight Ship Initialization")
    void testStraightShipInitialization() {
        assertNotNull(straightShip);
        assertEquals(100, straightShip.getPositionX());
        assertEquals(100, straightShip.getPositionY());
        assertEquals(MovementPattern.STRAIGHT_DOWN, straightShip.getPattern());
        assertFalse(straightShip.isDestroyed());
    }

    @Test
    @DisplayName("Test Straight Movement Pattern - Y should increase")
    void testStraightMovement() {
        int initialY = straightShip.getPositionY();
        int initialX = straightShip.getPositionX();

        straightShip.update(); // Update one frame

        assertTrue(straightShip.getPositionY() > initialY, "Y coordinate should increase (moving down).");
        assertEquals(initialX, straightShip.getPositionX(), "X coordinate should not change for straight movement.");
    }

    @Test
    @DisplayName("Test Zigzag Movement Pattern - X and Y should change")
    void testZigzagMovement() {
        int initialY = zigzagShip.getPositionY();
        int initialX = zigzagShip.getPositionX();

        zigzagShip.update();

        assertTrue(zigzagShip.getPositionY() > initialY, "Y coordinate should always increase.");
        assertNotEquals(initialX, zigzagShip.getPositionX(), "X coordinate should change for zigzag movement.");
    }

    @Test
    @DisplayName("Test Damage and Destruction")
    void testDamageAndDestruction() {
        // Check initial health (Default is 1 in code)
        int initialHealth = straightShip.getHealth();

        // Apply damage equal to health
        straightShip.takeDamage(initialHealth);

        assertTrue(straightShip.isDestroyed(), "Ship should be destroyed when health reaches 0.");
    }

    @Test
    @DisplayName("Test Despawn Condition (Off-screen)")
    void testShouldDespawn() {
        // Inside screen bounds
        assertFalse(straightShip.shouldDespawn());

        // Move ship below the screen
        straightShip.setPositionY(screenHeight + 100);

        assertTrue(straightShip.shouldDespawn(), "Should return true when ship moves off-screen.");
    }
}