package engine;

import entity.Bullet;
import entity.EnemyShip;
import entity.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for CollisionContext class.
 * Tests collision detection context setup and state management.
 */
class CollisionContextTest {

    private CollisionContext context;
    private Ship ship;
    private List<EnemyShip> enemies;
    private Set<Bullet> bullets;

    @BeforeEach
    void setUp() {
        ship = new Ship(100, 100);
        enemies = new ArrayList<>();
        bullets = new HashSet<>();
        
        // Add some test enemies
        enemies.add(new EnemyShip(50, 50, null));
        enemies.add(new EnemyShip(150, 150, null));
        
        // Add some test bullets
        bullets.add(new Bullet(75, 75, 0, 2));
        bullets.add(new Bullet(125, 125, 0, 2));
        
        context = new CollisionContext(ship, enemies, bullets);
    }

    @Test
    void testConstructor_InitializesAllFields() {
        assertNotNull(context, "Context should be initialized");
        assertNotNull(context.getShip(), "Ship should not be null");
        assertNotNull(context.getEnemies(), "Enemies list should not be null");
        assertNotNull(context.getBullets(), "Bullets set should not be null");
    }

    @Test
    void testGetShip_ReturnsCorrectShip() {
        Ship retrievedShip = context.getShip();
        assertSame(ship, retrievedShip, "Should return the same ship instance");
        assertEquals(100, retrievedShip.getPositionX(), "Ship X position should match");
        assertEquals(100, retrievedShip.getPositionY(), "Ship Y position should match");
    }

    @Test
    void testGetEnemies_ReturnsCorrectList() {
        List<EnemyShip> retrievedEnemies = context.getEnemies();
        assertSame(enemies, retrievedEnemies, "Should return the same enemies list");
        assertEquals(2, retrievedEnemies.size(), "Should have 2 enemies");
    }

    @Test
    void testGetBullets_ReturnsCorrectSet() {
        Set<Bullet> retrievedBullets = context.getBullets();
        assertSame(bullets, retrievedBullets, "Should return the same bullets set");
        assertEquals(2, retrievedBullets.size(), "Should have 2 bullets");
    }

    @Test
    void testConstructor_WithEmptyEnemies() {
        CollisionContext emptyContext = new CollisionContext(ship, new ArrayList<>(), bullets);
        assertNotNull(emptyContext.getEnemies(), "Enemies list should not be null");
        assertTrue(emptyContext.getEnemies().isEmpty(), "Enemies list should be empty");
    }

    @Test
    void testConstructor_WithEmptyBullets() {
        CollisionContext emptyContext = new CollisionContext(ship, enemies, new HashSet<>());
        assertNotNull(emptyContext.getBullets(), "Bullets set should not be null");
        assertTrue(emptyContext.getBullets().isEmpty(), "Bullets set should be empty");
    }

    @Test
    void testConstructor_WithNullShip() {
        CollisionContext nullShipContext = new CollisionContext(null, enemies, bullets);
        assertNull(nullShipContext.getShip(), "Ship should be null");
        assertNotNull(nullShipContext.getEnemies(), "Enemies should still be set");
        assertNotNull(nullShipContext.getBullets(), "Bullets should still be set");
    }

    @Test
    void testConstructor_WithNullEnemies() {
        CollisionContext nullEnemiesContext = new CollisionContext(ship, null, bullets);
        assertNotNull(nullEnemiesContext.getShip(), "Ship should still be set");
        assertNull(nullEnemiesContext.getEnemies(), "Enemies should be null");
        assertNotNull(nullEnemiesContext.getBullets(), "Bullets should still be set");
    }

    @Test
    void testConstructor_WithNullBullets() {
        CollisionContext nullBulletsContext = new CollisionContext(ship, enemies, null);
        assertNotNull(nullBulletsContext.getShip(), "Ship should still be set");
        assertNotNull(nullBulletsContext.getEnemies(), "Enemies should still be set");
        assertNull(nullBulletsContext.getBullets(), "Bullets should be null");
    }

    @Test
    void testConstructor_WithAllNull() {
        CollisionContext allNullContext = new CollisionContext(null, null, null);
        assertNull(allNullContext.getShip(), "Ship should be null");
        assertNull(allNullContext.getEnemies(), "Enemies should be null");
        assertNull(allNullContext.getBullets(), "Bullets should be null");
    }

    @Test
    void testContextWithManyEnemies() {
        List<EnemyShip> manyEnemies = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyEnemies.add(new EnemyShip(i * 10, i * 10, null));
        }
        
        CollisionContext largeContext = new CollisionContext(ship, manyEnemies, bullets);
        assertEquals(100, largeContext.getEnemies().size(), "Should handle 100 enemies");
    }

    @Test
    void testContextWithManyBullets() {
        Set<Bullet> manyBullets = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            manyBullets.add(new Bullet(i * 5, i * 5, 0, 2));
        }
        
        CollisionContext largeContext = new CollisionContext(ship, enemies, manyBullets);
        assertEquals(100, largeContext.getBullets().size(), "Should handle 100 bullets");
    }

    @Test
    void testContextImmutability_ShipReference() {
        Ship originalShip = context.getShip();
        Ship retrievedShip = context.getShip();
        
        assertSame(originalShip, retrievedShip, 
            "Multiple calls to getShip should return same reference");
    }

    @Test
    void testContextImmutability_EnemiesReference() {
        List<EnemyShip> originalEnemies = context.getEnemies();
        List<EnemyShip> retrievedEnemies = context.getEnemies();
        
        assertSame(originalEnemies, retrievedEnemies, 
            "Multiple calls to getEnemies should return same reference");
    }

    @Test
    void testContextImmutability_BulletsReference() {
        Set<Bullet> originalBullets = context.getBullets();
        Set<Bullet> retrievedBullets = context.getBullets();
        
        assertSame(originalBullets, retrievedBullets, 
            "Multiple calls to getBullets should return same reference");
    }

    @Test
    void testContextWithDifferentBulletOwners() {
        Set<Bullet> mixedBullets = new HashSet<>();
        mixedBullets.add(new Bullet(50, 50, 0, 2));   // Player bullet
        mixedBullets.add(new Bullet(60, 60, 0, 0));   // Enemy bullet
        mixedBullets.add(new Bullet(70, 70, 0, -1));  // Boss bullet
        
        CollisionContext mixedContext = new CollisionContext(ship, enemies, mixedBullets);
        assertEquals(3, mixedContext.getBullets().size(), "Should handle bullets from different owners");
    }

    @Test
    void testContextCreation_DifferentShipPositions() {
        Ship topLeft = new Ship(0, 0);
        Ship bottomRight = new Ship(448, 520);
        
        CollisionContext context1 = new CollisionContext(topLeft, enemies, bullets);
        CollisionContext context2 = new CollisionContext(bottomRight, enemies, bullets);
        
        assertEquals(0, context1.getShip().getPositionX(), "Top-left ship X should be 0");
        assertEquals(448, context2.getShip().getPositionX(), "Bottom-right ship X should be 448");
    }

    @Test
    void testContextWithOverlappingEntities() {
        // Create enemies and bullets at same position
        List<EnemyShip> overlappingEnemies = new ArrayList<>();
        overlappingEnemies.add(new EnemyShip(100, 100, null));
        
        Set<Bullet> overlappingBullets = new HashSet<>();
        overlappingBullets.add(new Bullet(100, 100, 0, 2));
        
        Ship overlappingShip = new Ship(100, 100);
        
        CollisionContext overlappingContext = new CollisionContext(
            overlappingShip, overlappingEnemies, overlappingBullets);
        
        assertNotNull(overlappingContext, "Should handle overlapping entities");
        assertEquals(100, overlappingContext.getShip().getPositionX());
        assertEquals(1, overlappingContext.getEnemies().size());
        assertEquals(1, overlappingContext.getBullets().size());
    }
}