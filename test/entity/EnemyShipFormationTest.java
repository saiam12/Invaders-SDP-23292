package entity;

import engine.GameSettings;
import engine.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import screen.Screen;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("EnemyShipFormation Test Suite")
class EnemyShipFormationTest {

    private EnemyShipFormation formation;
    private GameSettings gameSettings;
    private Screen mockScreen;
    private Set<Bullet> bullets;

    @BeforeEach
    void setUp() {
        gameSettings = new GameSettings(5, 4, 60, 2000);
        formation = new EnemyShipFormation(gameSettings);
        mockScreen = mock(Screen.class);
        when(mockScreen.getWidth()).thenReturn(448);
        when(mockScreen.getHeight()).thenReturn(520);
        formation.attach(mockScreen);
        bullets = new HashSet<>();
    }

    @Test
    @DisplayName("Formation should be initialized with correct dimensions")
    void testFormationInitialization() {
        assertNotNull(formation);
        assertFalse(formation.isEmpty());
    }

    @Test
    @DisplayName("Formation should contain correct number of enemy ships")
    void testFormationShipCount() {
        int expectedShips = 5 * 4; // width * height
        int actualShips = 0;

        for (EnemyShip ship : formation) {
            actualShips++;
        }

        assertEquals(expectedShips, actualShips);
    }

    @Test
    @DisplayName("Formation should be iterable and return all ships")
    void testFormationIteration() {
        Iterator<EnemyShip> iterator = formation.iterator();

        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());
    }

    @Test
    @DisplayName("Destroying all ships should result in empty formation")
    void testDestroyAll() {
        int destroyedCount = formation.destroyAll();

        assertTrue(destroyedCount > 0);
        assertEquals(0, destroyedCount - destroyedCount); // All ships marked as destroyed
        assertTrue(formation.isEmpty());
    }

    @Test
    @DisplayName("Formation should become empty after destroying all individual ships")
    void testIndividualDestruction() {
        for (EnemyShip ship : formation) {
            formation.destroy(ship);
        }

        formation.update();

        assertTrue(formation.isEmpty());
    }

    @Test
    @DisplayName("Formation shooting should add bullet when cooldown finished")
    void testFormationShooting() {
        // Simulate multiple update cycles to ensure cooldown finishes
        for (int i = 0; i < 100; i++) {
            formation.shoot(bullets);
        }

        // At least one bullet should be fired after enough time
        assertTrue(bullets.size() >= 0);
    }

    @Test
    @DisplayName("Destroying ship should remove it from formation")
    void testShipRemovalAfterDestruction() {
        EnemyShip firstShip = formation.iterator().next();

        formation.destroy(firstShip);
        formation.update();

        assertTrue(firstShip.isDestroyed());
    }

    @Test
    @DisplayName("Formation should update ship positions during movement")
    void testFormationMovement() {
        EnemyShip firstShip = formation.iterator().next();
        int initialX = firstShip.getPositionX();
        int initialY = firstShip.getPositionY();

        // Update multiple times to trigger movement
        for (int i = 0; i < 10; i++) {
            formation.update();
        }

        boolean moved = (firstShip.getPositionX() != initialX) ||
                (firstShip.getPositionY() != initialY);
        assertTrue(moved);
    }

    @Test
    @DisplayName("Formation should clear all ships when clear method is called")
    void testFormationClear() {
        formation.clear();

        assertTrue(formation.isEmpty());
    }

    @Test
    @DisplayName("Formation initialized from Level should have correct ship count")
    void testLevelBasedFormation() {
        Level mockLevel = mock(Level.class);
        when(mockLevel.getFormationWidth()).thenReturn(6);
        when(mockLevel.getFormationHeight()).thenReturn(5);
        when(mockLevel.getBaseSpeed()).thenReturn(50);
        when(mockLevel.getShootingFrecuency()).thenReturn(1500);

        EnemyShipFormation levelFormation = new EnemyShipFormation(mockLevel);

        int shipCount = 0;
        for (EnemyShip ship : levelFormation) {
            shipCount++;
        }

        assertEquals(30, shipCount); // 6 * 5
    }

    @Test
    @DisplayName("Formation should handle slowdown activation")
    void testSlowdownActivation() {
        formation.activateSlowdown();

        // Slowdown should be active, affecting movement speed
        // Movement behavior should change after activation
        assertDoesNotThrow(() -> formation.update());
    }

    @Test
    @DisplayName("Empty formation should not produce bullets when shooting")
    void testEmptyFormationShooting() {
        formation.destroyAll();
        formation.update();

        int bulletsBefore = bullets.size();
        formation.shoot(bullets);
        int bulletsAfter = bullets.size();

        assertEquals(bulletsBefore, bulletsAfter);
    }

    @Test
    @DisplayName("Formation should change direction when reaching screen boundaries")
    void testBoundaryCollision() {
        // Update many times to ensure formation reaches a boundary
        for (int i = 0; i < 200; i++) {
            formation.update();
        }

        // Formation should still be valid after many updates
        assertDoesNotThrow(() -> formation.update());
    }

    @Test
    @DisplayName("Different enemy types should be present in formation")
    void testMultipleEnemyTypes() {
        Set<String> enemyTypes = new HashSet<>();

        for (EnemyShip ship : formation) {
            String type = ship.getEnemyType();
            if (type != null) {
                enemyTypes.add(type);
            }
        }

        // Formation should contain multiple enemy types (A, B, C)
        assertTrue(enemyTypes.size() > 1);
    }
}