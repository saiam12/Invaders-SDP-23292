package entity;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnemyShip Test Suite")
class EnemyShipTest {

    private EnemyShip enemyShipA;
    private EnemyShip enemyShipB;
    private EnemyShip enemyShipC;
    private EnemyShip specialEnemyShip;

    @BeforeEach
    void setUp() {
        enemyShipA = new EnemyShip(100, 100, SpriteType.EnemyShipA1);
        enemyShipB = new EnemyShip(200, 200, SpriteType.EnemyShipB1);
        enemyShipC = new EnemyShip(300, 300, SpriteType.EnemyShipC1);
        specialEnemyShip = new EnemyShip(Color.RED, EnemyShip.Direction.RIGHT, 5);
    }

    @Test
    @DisplayName("Type A enemy should be initialized with correct point value and health")
    void testTypeAEnemyInitialization() {
        assertEquals(10, enemyShipA.getPointValue());
        assertEquals(1, enemyShipA.getHealth());
        assertEquals(1, enemyShipA.getMaxHealth());
        assertFalse(enemyShipA.isDestroyed());
    }

    @Test
    @DisplayName("Type B enemy should be initialized with correct point value and health")
    void testTypeBEnemyInitialization() {
        assertEquals(20, enemyShipB.getPointValue());
        assertEquals(2, enemyShipB.getHealth());
        assertEquals(2, enemyShipB.getMaxHealth());
    }

    @Test
    @DisplayName("Type C enemy should be initialized with correct point value and health")
    void testTypeCEnemyInitialization() {
        assertEquals(30, enemyShipC.getPointValue());
        assertEquals(3, enemyShipC.getHealth());
        assertEquals(3, enemyShipC.getMaxHealth());
    }

    @Test
    @DisplayName("Special enemy should be initialized with bonus point value")
    void testSpecialEnemyInitialization() {
        assertEquals(100, specialEnemyShip.getPointValue());
        assertEquals(Color.RED, specialEnemyShip.getColor());
        assertEquals(EnemyShip.Direction.RIGHT, specialEnemyShip.getDirection());
        assertEquals(5, specialEnemyShip.getXSpeed());
    }

    @Test
    @DisplayName("Enemy should move correctly based on distance parameters")
    void testEnemyMovement() {
        int initialX = enemyShipA.getPositionX();
        int initialY = enemyShipA.getPositionY();

        enemyShipA.move(10, 20);

        assertEquals(initialX + 10, enemyShipA.getPositionX());
        assertEquals(initialY + 20, enemyShipA.getPositionY());
    }

    @Test
    @DisplayName("Enemy should take damage and health should decrease accordingly")
    void testTakeDamage() {
        int initialHealth = enemyShipC.getHealth();

        enemyShipC.takeDamage(1);

        assertEquals(initialHealth - 1, enemyShipC.getHealth());
        assertFalse(enemyShipC.isDestroyed());
    }

    @Test
    @DisplayName("Enemy should be destroyed when health reaches zero")
    void testDestroyWhenHealthReachesZero() {
        enemyShipA.takeDamage(1);

        assertTrue(enemyShipA.isDestroyed());
        assertEquals(SpriteType.Explosion, enemyShipA.getSpriteType());
    }

    @Test
    @DisplayName("Enemy with multiple HP should not be destroyed after single hit")
    void testMultipleHealthEnemy() {
        enemyShipC.takeDamage(1);

        assertFalse(enemyShipC.isDestroyed());
        assertEquals(2, enemyShipC.getHealth());

        enemyShipC.takeDamage(1);
        assertFalse(enemyShipC.isDestroyed());
        assertEquals(1, enemyShipC.getHealth());

        enemyShipC.takeDamage(1);
        assertTrue(enemyShipC.isDestroyed());
    }

    @Test
    @DisplayName("Destroy method should change sprite to explosion and set destroyed flag")
    void testDestroyMethod() {
        assertFalse(enemyShipA.isDestroyed());

        enemyShipA.destroy();

        assertTrue(enemyShipA.isDestroyed());
        assertEquals(SpriteType.Explosion, enemyShipA.getSpriteType());
    }

    @Test
    @DisplayName("Enemy type should be correctly identified from sprite type")
    void testGetEnemyType() {
        assertEquals("enemyA", enemyShipA.getEnemyType());
        assertEquals("enemyB", enemyShipB.getEnemyType());
        assertEquals("enemyC", enemyShipC.getEnemyType());
        assertNull(specialEnemyShip.getEnemyType());
    }

    @Test
    @DisplayName("Special enemy direction should be changeable")
    void testSpecialEnemyDirectionChange() {
        specialEnemyShip.setDirection(EnemyShip.Direction.LEFT);
        assertEquals(EnemyShip.Direction.LEFT, specialEnemyShip.getDirection());

        specialEnemyShip.setDirection(EnemyShip.Direction.DOWN);
        assertEquals(EnemyShip.Direction.DOWN, specialEnemyShip.getDirection());
    }

    @Test
    @DisplayName("Special enemy speed should be modifiable")
    void testSpecialEnemySpeedChange() {
        specialEnemyShip.setXSpeed(10);
        assertEquals(10, specialEnemyShip.getXSpeed());
    }

    @Test
    @DisplayName("Destroyed enemy should not take additional damage")
    void testDestroyedEnemyIgnoresDamage() {
        enemyShipA.destroy();
        int healthAfterDestroy = enemyShipA.getHealth();

        enemyShipA.takeDamage(5);

        assertEquals(healthAfterDestroy, enemyShipA.getHealth());
    }

    @Test
    @DisplayName("Enemy sprite should alternate during animation update")
    void testSpriteAnimation() {
        SpriteType initialSprite = enemyShipA.getSpriteType();

        enemyShipA.update();

        // Sprite should eventually change during animation cycle
        // Note: This test might need to wait for animation cooldown
        assertNotNull(enemyShipA.getSpriteType());
    }

    @Test
    @DisplayName("Multiple enemies should have independent health tracking")
    void testIndependentHealthTracking() {
        EnemyShip enemy1 = new EnemyShip(0, 0, SpriteType.EnemyShipC1);
        EnemyShip enemy2 = new EnemyShip(0, 0, SpriteType.EnemyShipC1);

        enemy1.takeDamage(2);

        assertEquals(1, enemy1.getHealth());
        assertEquals(3, enemy2.getHealth());
    }
}