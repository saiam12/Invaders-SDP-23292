package engine.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ActionPacket DTO.
 * Tests data encapsulation, validation, and edge cases.
 */
class ActionPacketTest {

    private ActionPacket actionPacket;

    @BeforeEach
    void setUp() {
        actionPacket = new ActionPacket();
    }

    @Test
    void testDefaultConstructor_InitializesWithDefaults() {
        assertNotNull(actionPacket, "ActionPacket should be instantiated");
        assertEquals(0, actionPacket.moveX, "Default moveX should be 0");
        assertEquals(0, actionPacket.moveY, "Default moveY should be 0");
        assertFalse(actionPacket.shoot, "Default shoot should be false");
    }

    @Test
    void testMoveX_SetAndGet() {
        actionPacket.moveX = 1;
        assertEquals(1, actionPacket.moveX, "moveX should be set to 1");
        
        actionPacket.moveX = -1;
        assertEquals(-1, actionPacket.moveX, "moveX should be set to -1");
        
        actionPacket.moveX = 0;
        assertEquals(0, actionPacket.moveX, "moveX should be set to 0");
    }

    @Test
    void testMoveY_SetAndGet() {
        actionPacket.moveY = 1;
        assertEquals(1, actionPacket.moveY, "moveY should be set to 1");
        
        actionPacket.moveY = -1;
        assertEquals(-1, actionPacket.moveY, "moveY should be set to -1");
        
        actionPacket.moveY = 0;
        assertEquals(0, actionPacket.moveY, "moveY should be set to 0");
    }

    @Test
    void testShoot_SetAndGet() {
        actionPacket.shoot = true;
        assertTrue(actionPacket.shoot, "shoot should be true");
        
        actionPacket.shoot = false;
        assertFalse(actionPacket.shoot, "shoot should be false");
    }

    @Test
    void testCombinedMovement_RightUp() {
        actionPacket.moveX = 1;
        actionPacket.moveY = -1;
        actionPacket.shoot = false;
        
        assertEquals(1, actionPacket.moveX, "Should move right");
        assertEquals(-1, actionPacket.moveY, "Should move up");
        assertFalse(actionPacket.shoot, "Should not shoot");
    }

    @Test
    void testCombinedMovement_LeftDown() {
        actionPacket.moveX = -1;
        actionPacket.moveY = 1;
        actionPacket.shoot = false;
        
        assertEquals(-1, actionPacket.moveX, "Should move left");
        assertEquals(1, actionPacket.moveY, "Should move down");
        assertFalse(actionPacket.shoot, "Should not shoot");
    }

    @Test
    void testCombinedMovement_WithShooting() {
        actionPacket.moveX = 1;
        actionPacket.moveY = 1;
        actionPacket.shoot = true;
        
        assertEquals(1, actionPacket.moveX, "Should move right");
        assertEquals(1, actionPacket.moveY, "Should move down");
        assertTrue(actionPacket.shoot, "Should shoot");
    }

    @Test
    void testStationary_WithShooting() {
        actionPacket.moveX = 0;
        actionPacket.moveY = 0;
        actionPacket.shoot = true;
        
        assertEquals(0, actionPacket.moveX, "Should not move horizontally");
        assertEquals(0, actionPacket.moveY, "Should not move vertically");
        assertTrue(actionPacket.shoot, "Should shoot");
    }

    @Test
    void testStationary_NoShooting() {
        actionPacket.moveX = 0;
        actionPacket.moveY = 0;
        actionPacket.shoot = false;
        
        assertEquals(0, actionPacket.moveX, "Should not move horizontally");
        assertEquals(0, actionPacket.moveY, "Should not move vertically");
        assertFalse(actionPacket.shoot, "Should not shoot");
    }

    @Test
    void testExtremeValues_MoveX() {
        actionPacket.moveX = Integer.MAX_VALUE;
        assertEquals(Integer.MAX_VALUE, actionPacket.moveX, "Should handle MAX_VALUE");
        
        actionPacket.moveX = Integer.MIN_VALUE;
        assertEquals(Integer.MIN_VALUE, actionPacket.moveX, "Should handle MIN_VALUE");
    }

    @Test
    void testExtremeValues_MoveY() {
        actionPacket.moveY = Integer.MAX_VALUE;
        assertEquals(Integer.MAX_VALUE, actionPacket.moveY, "Should handle MAX_VALUE");
        
        actionPacket.moveY = Integer.MIN_VALUE;
        assertEquals(Integer.MIN_VALUE, actionPacket.moveY, "Should handle MIN_VALUE");
    }

    @Test
    void testMultipleStateChanges() {
        // First state
        actionPacket.moveX = 1;
        actionPacket.moveY = 0;
        actionPacket.shoot = true;
        
        assertEquals(1, actionPacket.moveX);
        assertEquals(0, actionPacket.moveY);
        assertTrue(actionPacket.shoot);
        
        // Second state
        actionPacket.moveX = -1;
        actionPacket.moveY = -1;
        actionPacket.shoot = false;
        
        assertEquals(-1, actionPacket.moveX);
        assertEquals(-1, actionPacket.moveY);
        assertFalse(actionPacket.shoot);
        
        // Third state
        actionPacket.moveX = 0;
        actionPacket.moveY = 1;
        actionPacket.shoot = true;
        
        assertEquals(0, actionPacket.moveX);
        assertEquals(1, actionPacket.moveY);
        assertTrue(actionPacket.shoot);
    }

    @Test
    void testAllEightDirections() {
        // Up
        actionPacket.moveX = 0;
        actionPacket.moveY = -1;
        assertEquals(0, actionPacket.moveX);
        assertEquals(-1, actionPacket.moveY);
        
        // Up-Right
        actionPacket.moveX = 1;
        actionPacket.moveY = -1;
        assertEquals(1, actionPacket.moveX);
        assertEquals(-1, actionPacket.moveY);
        
        // Right
        actionPacket.moveX = 1;
        actionPacket.moveY = 0;
        assertEquals(1, actionPacket.moveX);
        assertEquals(0, actionPacket.moveY);
        
        // Down-Right
        actionPacket.moveX = 1;
        actionPacket.moveY = 1;
        assertEquals(1, actionPacket.moveX);
        assertEquals(1, actionPacket.moveY);
        
        // Down
        actionPacket.moveX = 0;
        actionPacket.moveY = 1;
        assertEquals(0, actionPacket.moveX);
        assertEquals(1, actionPacket.moveY);
        
        // Down-Left
        actionPacket.moveX = -1;
        actionPacket.moveY = 1;
        assertEquals(-1, actionPacket.moveX);
        assertEquals(1, actionPacket.moveY);
        
        // Left
        actionPacket.moveX = -1;
        actionPacket.moveY = 0;
        assertEquals(-1, actionPacket.moveX);
        assertEquals(0, actionPacket.moveY);
        
        // Up-Left
        actionPacket.moveX = -1;
        actionPacket.moveY = -1;
        assertEquals(-1, actionPacket.moveX);
        assertEquals(-1, actionPacket.moveY);
    }
}