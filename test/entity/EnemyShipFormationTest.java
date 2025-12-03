package entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import entity.InfiniteEnemyShip.MovementPattern;

class InfiniteEnemyFormationTest {

    private InfiniteEnemyFormation formation;
    private int screenWidth = 800;
    private int screenHeight = 600;

    @BeforeEach
    void setUp() {
        formation = new InfiniteEnemyFormation();
    }

    @Test
    @DisplayName("Test Adding Enemies to Formation")
    void testAddEnemy() {
        assertTrue(formation.isEmpty(), "Formation should be empty initially.");

        InfiniteEnemyShip enemy = new InfiniteEnemyShip(100, 100, MovementPattern.STRAIGHT_DOWN, screenWidth, screenHeight);
        formation.addEnemy(enemy);

        assertFalse(formation.isEmpty(), "Formation should not be empty after adding an enemy.");
        assertEquals(1, formation.getEnemyCount(), "Enemy count should be 1.");
    }

    @Test
    @DisplayName("Test Update Moves Enemies")
    void testUpdateMovesEnemies() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(100, 100, MovementPattern.STRAIGHT_DOWN, screenWidth, screenHeight);
        formation.addEnemy(enemy);

        int initialY = enemy.getPositionY();

        // Call formation update, which should call enemy.update()
        formation.update();

        assertTrue(enemy.getPositionY() > initialY, "Enemy inside formation should move down after update.");
    }

    @Test
    @DisplayName("Test Removing Off-Screen Enemies")
    void testRemoveOffScreenEnemies() {
        // Create an enemy way below the screen (should be despawned)
        InfiniteEnemyShip offScreenEnemy = new InfiniteEnemyShip(100, screenHeight + 50, MovementPattern.STRAIGHT_DOWN, screenWidth, screenHeight);
        formation.addEnemy(offScreenEnemy);

        assertEquals(1, formation.getEnemyCount(), "Formation should have 1 enemy initially.");

        // Update formation -> should check shouldDespawn() and remove it
        formation.update();

        assertEquals(0, formation.getEnemyCount(), "Off-screen enemy should be removed.");
    }

    @Test
    @DisplayName("Test Clear Formation")
    void testClearFormation() {
        formation.addEnemy(new InfiniteEnemyShip(100, 100, MovementPattern.STRAIGHT_DOWN, screenWidth, screenHeight));
        formation.addEnemy(new InfiniteEnemyShip(200, 100, MovementPattern.STRAIGHT_DOWN, screenWidth, screenHeight));

        assertEquals(2, formation.getEnemyCount());

        formation.clear();

        assertEquals(0, formation.getEnemyCount(), "Formation should be empty after clear.");
        assertTrue(formation.isEmpty());
    }
}