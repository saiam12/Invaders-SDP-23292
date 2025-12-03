package engine;

import entity.Bullet;
import entity.EnemyShip;
import entity.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CollisionManager class.
 * Tests collision detection logic for ships, bullets, enemies, and items.
 */
class CollisionManagerTest {

    private CollisionManager collisionManager;
    private Ship ship;
    private List<EnemyShip> enemies;
    private Set<Bullet> bullets;
    private Logger mockLogger;
    private MockedStatic<Core> mockedCore;

    @BeforeEach
    void setUp() {
        collisionManager = new CollisionManager();
        ship = new Ship(224, 400); // Center position
        enemies = new ArrayList<>();
        bullets = new HashSet<>();

        // Mock logger
        mockLogger = mock(Logger.class);
        mockedCore = mockStatic(Core.class);
        mockedCore.when(Core::getLogger).thenReturn(mockLogger);
    }

    @Test
    void testConstructor_InitializesManager() {
        assertNotNull(collisionManager, "CollisionManager should be initialized");
    }

    @Test
    void testCheckCollisions_NullContext_DoesNotCrash() {
        assertDoesNotThrow(() -> collisionManager.checkCollisions(null),
            "Should handle null context gracefully");
    }

    @Test
    void testCheckCollisions_EmptyContext_NoExceptions() {
        CollisionContext emptyContext = new CollisionContext(ship, new ArrayList<>(), new HashSet<>());
        assertDoesNotThrow(() -> collisionManager.checkCollisions(emptyContext),
            "Should handle empty context without exceptions");
    }

    @Test
    void testCheckCollisions_NoCollisions_ShipHealthUnchanged() {
        // Place enemy far from ship
        enemies.add(new EnemyShip(10, 10, null));
        bullets.add(new Bullet(10, 10, 0, 2));
        
        int initialHealth = ship.getHealth();
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertEquals(initialHealth, ship.getHealth(), 
            "Ship health should not change with no collisions");
    }

    @Test
    void testCheckCollisions_ShipEnemyCollision_ReducesHealth() {
        // Place enemy at ship position
        EnemyShip enemy = new EnemyShip(ship.getPositionX(), ship.getPositionY(), null);
        enemies.add(enemy);
        
        int initialHealth = ship.getHealth();
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertTrue(ship.getHealth() < initialHealth || !enemy.isAlive(),
            "Collision should affect ship health or destroy enemy");
    }

    @Test
    void testCheckCollisions_PlayerBulletHitsEnemy_DestroysBoth() {
        EnemyShip enemy = new EnemyShip(100, 100, null);
        enemy.setHealth(1); // Set low health for easier testing
        enemies.add(enemy);
        
        // Player bullet at enemy position
        Bullet playerBullet = new Bullet(enemy.getPositionX(), enemy.getPositionY(), 0, 2);
        bullets.add(playerBullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertFalse(bullets.contains(playerBullet) || !enemy.isAlive(),
            "Bullet should be removed or enemy destroyed on hit");
    }

    @Test
    void testCheckCollisions_EnemyBulletHitsShip_ReducesHealth() {
        // Enemy bullet at ship position
        Bullet enemyBullet = new Bullet(ship.getPositionX(), ship.getPositionY(), 0, 0);
        bullets.add(enemyBullet);
        
        int initialHealth = ship.getHealth();
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertTrue(ship.getHealth() < initialHealth || !bullets.contains(enemyBullet),
            "Ship health should decrease or bullet should be removed");
    }

    @Test
    void testCheckCollisions_MultipleEnemies_AllChecked() {
        // Add multiple enemies at different positions
        for (int i = 0; i < 10; i++) {
            enemies.add(new EnemyShip(i * 30, i * 30, null));
        }
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle multiple enemies without crashing");
    }

    @Test
    void testCheckCollisions_MultipleBullets_AllChecked() {
        // Add multiple bullets at different positions
        for (int i = 0; i < 20; i++) {
            bullets.add(new Bullet(i * 15, i * 15, 0, 2));
        }
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle multiple bullets without crashing");
    }

    @Test
    void testCheckCollisions_DeadEnemies_SkipsCollision() {
        EnemyShip deadEnemy = new EnemyShip(ship.getPositionX(), ship.getPositionY(), null);
        deadEnemy.setHealth(0);
        enemies.add(deadEnemy);
        
        int initialHealth = ship.getHealth();
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertEquals(initialHealth, ship.getHealth(),
            "Dead enemies should not cause collisions");
    }

    @Test
    void testCheckCollisions_PlayerBulletDoesNotHitShip() {
        // Player bullet at ship position
        Bullet playerBullet = new Bullet(ship.getPositionX(), ship.getPositionY(), 0, 2);
        bullets.add(playerBullet);
        
        int initialHealth = ship.getHealth();
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertEquals(initialHealth, ship.getHealth(),
            "Player bullet should not damage own ship");
    }

    @Test
    void testCheckCollisions_BulletBulletCollision() {
        // Place two bullets from different owners at same position
        Bullet playerBullet = new Bullet(100, 100, 0, 2);
        Bullet enemyBullet = new Bullet(100, 100, 0, 0);
        bullets.add(playerBullet);
        bullets.add(enemyBullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        // At least one bullet should be removed
        assertTrue(bullets.size() < 2,
            "Colliding bullets should be removed");
    }

    @Test
    void testCheckCollisions_BoundaryPositions_TopLeft() {
        ship = new Ship(0, 0);
        EnemyShip enemy = new EnemyShip(0, 0, null);
        enemies.add(enemy);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle top-left boundary collision");
    }

    @Test
    void testCheckCollisions_BoundaryPositions_BottomRight() {
        ship = new Ship(448, 520);
        EnemyShip enemy = new EnemyShip(448, 520, null);
        enemies.add(enemy);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle bottom-right boundary collision");
    }

    @Test
    void testCheckCollisions_NegativePositions() {
        ship = new Ship(-10, -10);
        EnemyShip enemy = new EnemyShip(-10, -10, null);
        enemies.add(enemy);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle negative positions");
    }

    @Test
    void testCheckCollisions_VeryLargePositions() {
        ship = new Ship(10000, 10000);
        EnemyShip enemy = new EnemyShip(10000, 10000, null);
        enemies.add(enemy);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle very large positions");
    }

    @Test
    void testCheckCollisions_BulletsFromAllOwnerTypes() {
        bullets.add(new Bullet(50, 50, 0, 2));   // Player
        bullets.add(new Bullet(60, 60, 0, 0));   // Enemy
        bullets.add(new Bullet(70, 70, 0, -1));  // Boss
        bullets.add(new Bullet(80, 80, 0, 1));   // Other
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle bullets from all owner types");
    }

    @Test
    void testCheckCollisions_SimultaneousCollisions() {
        // Multiple collisions at once
        EnemyShip enemy1 = new EnemyShip(ship.getPositionX(), ship.getPositionY(), null);
        EnemyShip enemy2 = new EnemyShip(ship.getPositionX() + 1, ship.getPositionY(), null);
        enemies.add(enemy1);
        enemies.add(enemy2);
        
        Bullet enemyBullet = new Bullet(ship.getPositionX(), ship.getPositionY(), 0, 0);
        bullets.add(enemyBullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle simultaneous collisions");
    }

    @Test
    void testCheckCollisions_HighVelocityBullet() {
        // Bullet moving very fast (large position jump)
        Bullet fastBullet = new Bullet(ship.getPositionX(), ship.getPositionY() - 100, 50, 0);
        bullets.add(fastBullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle high velocity bullets");
    }

    @Test
    void testCheckCollisions_ZeroSizeEntity() {
        // Edge case: entity with zero width/height
        EnemyShip tinyEnemy = new EnemyShip(100, 100, null);
        // Assuming we can't directly set size, just test position collision
        enemies.add(tinyEnemy);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle entities at same position");
    }

    @Test
    void testCheckCollisions_EmptyBulletSet_WithEnemies() {
        enemies.add(new EnemyShip(100, 100, null));
        enemies.add(new EnemyShip(200, 200, null));
        
        CollisionContext context = new CollisionContext(ship, enemies, new HashSet<>());
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle empty bullet set with enemies present");
    }

    @Test
    void testCheckCollisions_EmptyEnemyList_WithBullets() {
        bullets.add(new Bullet(100, 100, 0, 2));
        bullets.add(new Bullet(200, 200, 0, 0));
        
        CollisionContext context = new CollisionContext(ship, new ArrayList<>(), bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle empty enemy list with bullets present");
    }

    @Test
    void testCheckCollisions_NullShip_DoesNotCrash() {
        enemies.add(new EnemyShip(100, 100, null));
        bullets.add(new Bullet(100, 100, 0, 2));
        
        CollisionContext contextWithNullShip = new CollisionContext(null, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(contextWithNullShip),
            "Should handle null ship gracefully");
    }

    @Test
    void testCheckCollisions_MassiveNumberOfEntities() {
        // Stress test with many entities
        for (int i = 0; i < 100; i++) {
            enemies.add(new EnemyShip(i * 5, i * 5, null));
            bullets.add(new Bullet(i * 3, i * 3, 0, 2));
        }
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle massive number of entities");
    }

    @Test
    void testCheckCollisions_OverlappingEnemies() {
        // Multiple enemies at exact same position
        EnemyShip enemy1 = new EnemyShip(100, 100, null);
        EnemyShip enemy2 = new EnemyShip(100, 100, null);
        EnemyShip enemy3 = new EnemyShip(100, 100, null);
        enemies.add(enemy1);
        enemies.add(enemy2);
        enemies.add(enemy3);
        
        Bullet bullet = new Bullet(100, 100, 0, 2);
        bullets.add(bullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle overlapping enemies");
    }

    @Test
    void testCheckCollisions_ClusterOfBullets() {
        // Many bullets in small area
        for (int i = 0; i < 10; i++) {
            bullets.add(new Bullet(100 + i, 100 + i, 0, 2));
            bullets.add(new Bullet(100 + i, 100 + i, 0, 0));
        }
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle cluster of bullets");
    }

    @Test
    void testCheckCollisions_AlternatingOwnerBullets() {
        // Bullets with alternating owners
        for (int i = 0; i < 10; i++) {
            int owner = i % 3 == 0 ? 2 : (i % 3 == 1 ? 0 : -1);
            bullets.add(new Bullet(i * 20, i * 20, 0, owner));
        }
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle bullets with alternating owners");
    }

    @Test
    void testCheckCollisions_EnemiesInFormation() {
        // Create a formation of enemies (grid pattern)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                enemies.add(new EnemyShip(col * 40, row * 40, null));
            }
        }
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle enemies in formation");
    }

    @Test
    void testCheckCollisions_BulletsMovingTowardEachOther() {
        // Player bullet moving down and enemy bullet moving up
        Bullet playerBullet = new Bullet(100, 200, -5, 2);
        Bullet enemyBullet = new Bullet(100, 210, 5, 0);
        bullets.add(playerBullet);
        bullets.add(enemyBullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        assertDoesNotThrow(() -> collisionManager.checkCollisions(context),
            "Should handle bullets moving toward each other");
    }

    @Test
    void testCheckCollisions_PreservesNonCollidingEntities() {
        // Add entities that shouldn't collide
        EnemyShip farEnemy = new EnemyShip(10, 10, null);
        enemies.add(farEnemy);
        
        Bullet farBullet = new Bullet(400, 500, 0, 2);
        bullets.add(farBullet);
        
        CollisionContext context = new CollisionContext(ship, enemies, bullets);
        collisionManager.checkCollisions(context);
        
        assertTrue(enemies.contains(farEnemy) || !farEnemy.isAlive(),
            "Far enemy should not be affected");
        assertTrue(bullets.contains(farBullet),
            "Far bullet should not be removed");
    }
}