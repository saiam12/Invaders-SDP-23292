package engine.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for StatePacket DTO.
 * Tests state representation, list handling, and edge cases.
 */
class StatePacketTest {

    private StatePacket statePacket;

    @BeforeEach
    void setUp() {
        statePacket = new StatePacket();
    }

    @Test
    void testDefaultConstructor_InitializesWithDefaults() {
        assertNotNull(statePacket, "StatePacket should be instantiated");
        assertEquals(0, statePacket.playerX, "Default playerX should be 0");
        assertEquals(0, statePacket.playerY, "Default playerY should be 0");
        assertEquals(0, statePacket.playerHp, "Default playerHp should be 0");
        assertEquals(0, statePacket.score, "Default score should be 0");
        assertNull(statePacket.enemies, "Default enemies should be null");
        assertNull(statePacket.bullets, "Default bullets should be null");
        assertNull(statePacket.items, "Default items should be null");
        assertNull(statePacket.boss, "Default boss should be null");
        assertNull(statePacket.enemyDamageEvents, "Default enemyDamageEvents should be null");
    }

    @Test
    void testPlayerPosition_SetAndGet() {
        statePacket.playerX = 100;
        statePacket.playerY = 200;
        
        assertEquals(100, statePacket.playerX, "playerX should be set correctly");
        assertEquals(200, statePacket.playerY, "playerY should be set correctly");
    }

    @Test
    void testPlayerPosition_NegativeValues() {
        statePacket.playerX = -50;
        statePacket.playerY = -100;
        
        assertEquals(-50, statePacket.playerX, "Should handle negative playerX");
        assertEquals(-100, statePacket.playerY, "Should handle negative playerY");
    }

    @Test
    void testPlayerPosition_ExtremeValues() {
        statePacket.playerX = Integer.MAX_VALUE;
        statePacket.playerY = Integer.MAX_VALUE;
        
        assertEquals(Integer.MAX_VALUE, statePacket.playerX, "Should handle MAX_VALUE");
        assertEquals(Integer.MAX_VALUE, statePacket.playerY, "Should handle MAX_VALUE");
    }

    @Test
    void testPlayerHp_SetAndGet() {
        statePacket.playerHp = 3;
        assertEquals(3, statePacket.playerHp, "playerHp should be set correctly");
        
        statePacket.playerHp = 0;
        assertEquals(0, statePacket.playerHp, "playerHp should handle 0");
        
        statePacket.playerHp = 100;
        assertEquals(100, statePacket.playerHp, "playerHp should handle large values");
    }

    @Test
    void testPlayerHp_NegativeValue() {
        statePacket.playerHp = -1;
        assertEquals(-1, statePacket.playerHp, "Should allow negative HP for edge cases");
    }

    @Test
    void testScore_SetAndGet() {
        statePacket.score = 1000;
        assertEquals(1000, statePacket.score, "score should be set correctly");
        
        statePacket.score = 0;
        assertEquals(0, statePacket.score, "score should handle 0");
        
        statePacket.score = 999999;
        assertEquals(999999, statePacket.score, "score should handle large values");
    }

    @Test
    void testScore_NegativeValue() {
        statePacket.score = -100;
        assertEquals(-100, statePacket.score, "Should allow negative scores");
    }

    @Test
    void testEnemies_EmptyList() {
        statePacket.enemies = new ArrayList<>();
        assertNotNull(statePacket.enemies, "Enemies list should not be null");
        assertTrue(statePacket.enemies.isEmpty(), "Enemies list should be empty");
    }

    @Test
    void testEnemies_SingleEnemy() {
        List<List<Object>> enemies = new ArrayList<>();
        List<Object> enemy = new ArrayList<>();
        enemy.add(100);  // x
        enemy.add(50);   // y
        enemy.add(10);   // hp
        enemy.add(1);    // type
        enemies.add(enemy);
        
        statePacket.enemies = enemies;
        
        assertNotNull(statePacket.enemies, "Enemies list should not be null");
        assertEquals(1, statePacket.enemies.size(), "Should have 1 enemy");
        assertEquals(100, statePacket.enemies.get(0).get(0), "Enemy X should be correct");
        assertEquals(50, statePacket.enemies.get(0).get(1), "Enemy Y should be correct");
    }

    @Test
    void testEnemies_MultipleEnemies() {
        List<List<Object>> enemies = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<Object> enemy = new ArrayList<>();
            enemy.add(i * 10);  // x
            enemy.add(i * 5);   // y
            enemy.add(5);       // hp
            enemy.add(i % 3);   // type
            enemies.add(enemy);
        }
        
        statePacket.enemies = enemies;
        
        assertEquals(10, statePacket.enemies.size(), "Should have 10 enemies");
        assertEquals(90, statePacket.enemies.get(9).get(0), "Last enemy X should be correct");
    }

    @Test
    void testBullets_EmptyList() {
        statePacket.bullets = new ArrayList<>();
        assertNotNull(statePacket.bullets, "Bullets list should not be null");
        assertTrue(statePacket.bullets.isEmpty(), "Bullets list should be empty");
    }

    @Test
    void testBullets_SingleBullet() {
        List<List<Object>> bullets = new ArrayList<>();
        List<Object> bullet = new ArrayList<>();
        bullet.add(150);  // x
        bullet.add(200);  // y
        bullet.add(2);    // owner
        bullets.add(bullet);
        
        statePacket.bullets = bullets;
        
        assertNotNull(statePacket.bullets, "Bullets list should not be null");
        assertEquals(1, statePacket.bullets.size(), "Should have 1 bullet");
        assertEquals(150, statePacket.bullets.get(0).get(0), "Bullet X should be correct");
    }

    @Test
    void testBullets_MultipleBullets() {
        List<List<Object>> bullets = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<Object> bullet = new ArrayList<>();
            bullet.add(i * 15);     // x
            bullet.add(i * 10);     // y
            bullet.add(i % 3);      // owner
            bullets.add(bullet);
        }
        
        statePacket.bullets = bullets;
        
        assertEquals(20, statePacket.bullets.size(), "Should have 20 bullets");
    }

    @Test
    void testItems_EmptyList() {
        statePacket.items = new ArrayList<>();
        assertNotNull(statePacket.items, "Items list should not be null");
        assertTrue(statePacket.items.isEmpty(), "Items list should be empty");
    }

    @Test
    void testItems_SingleItem() {
        List<List<Object>> items = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(100);   // x
        item.add(150);   // y
        item.add("HP");  // type
        items.add(item);
        
        statePacket.items = items;
        
        assertNotNull(statePacket.items, "Items list should not be null");
        assertEquals(1, statePacket.items.size(), "Should have 1 item");
        assertEquals("HP", statePacket.items.get(0).get(2), "Item type should be correct");
    }

    @Test
    void testItems_MultipleTypes() {
        List<List<Object>> items = new ArrayList<>();
        String[] types = {"HP", "POWER", "SPEED", "BOMB"};
        
        for (int i = 0; i < types.length; i++) {
            List<Object> item = new ArrayList<>();
            item.add(i * 20);
            item.add(i * 30);
            item.add(types[i]);
            items.add(item);
        }
        
        statePacket.items = items;
        
        assertEquals(4, statePacket.items.size(), "Should have 4 items");
        assertEquals("HP", statePacket.items.get(0).get(2), "First item should be HP");
        assertEquals("BOMB", statePacket.items.get(3).get(2), "Last item should be BOMB");
    }

    @Test
    void testBoss_NullInitially() {
        assertNull(statePacket.boss, "Boss should be null initially");
    }

    @Test
    void testBoss_SetBossData() {
        List<Object> boss = new ArrayList<>();
        boss.add(200);  // x
        boss.add(100);  // y
        boss.add(500);  // hp
        
        statePacket.boss = boss;
        
        assertNotNull(statePacket.boss, "Boss should not be null");
        assertEquals(200, statePacket.boss.get(0), "Boss X should be correct");
        assertEquals(100, statePacket.boss.get(1), "Boss Y should be correct");
        assertEquals(500, statePacket.boss.get(2), "Boss HP should be correct");
    }

    @Test
    void testEnemyDamageEvents_EmptyList() {
        statePacket.enemyDamageEvents = new ArrayList<>();
        assertNotNull(statePacket.enemyDamageEvents, "Damage events should not be null");
        assertTrue(statePacket.enemyDamageEvents.isEmpty(), "Damage events should be empty");
    }

    @Test
    void testEnemyDamageEvents_SingleEvent() {
        List<List<Object>> events = new ArrayList<>();
        List<Object> event = new ArrayList<>();
        event.add(5);   // enemy id
        event.add(10);  // damage
        events.add(event);
        
        statePacket.enemyDamageEvents = events;
        
        assertNotNull(statePacket.enemyDamageEvents, "Damage events should not be null");
        assertEquals(1, statePacket.enemyDamageEvents.size(), "Should have 1 event");
        assertEquals(5, statePacket.enemyDamageEvents.get(0).get(0), "Enemy ID should be correct");
        assertEquals(10, statePacket.enemyDamageEvents.get(0).get(1), "Damage should be correct");
    }

    @Test
    void testEnemyDamageEvents_MultipleEvents() {
        List<List<Object>> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Object> event = new ArrayList<>();
            event.add(i);       // enemy id
            event.add(i * 5);   // damage
            events.add(event);
        }
        
        statePacket.enemyDamageEvents = events;
        
        assertEquals(5, statePacket.enemyDamageEvents.size(), "Should have 5 events");
    }

    @Test
    void testCompleteGameState() {
        // Player state
        statePacket.playerX = 224;
        statePacket.playerY = 400;
        statePacket.playerHp = 3;
        statePacket.score = 5000;
        
        // Enemies
        List<List<Object>> enemies = new ArrayList<>();
        List<Object> enemy = new ArrayList<>();
        enemy.add(100);
        enemy.add(50);
        enemy.add(10);
        enemy.add(1);
        enemies.add(enemy);
        statePacket.enemies = enemies;
        
        // Bullets
        List<List<Object>> bullets = new ArrayList<>();
        List<Object> bullet = new ArrayList<>();
        bullet.add(225);
        bullet.add(380);
        bullet.add(2);
        bullets.add(bullet);
        statePacket.bullets = bullets;
        
        // Items
        List<List<Object>> items = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(150);
        item.add(200);
        item.add("HP");
        items.add(item);
        statePacket.items = items;
        
        // Boss
        List<Object> boss = new ArrayList<>();
        boss.add(200);
        boss.add(100);
        boss.add(1000);
        statePacket.boss = boss;
        
        // Verify all state
        assertEquals(224, statePacket.playerX);
        assertEquals(400, statePacket.playerY);
        assertEquals(3, statePacket.playerHp);
        assertEquals(5000, statePacket.score);
        assertEquals(1, statePacket.enemies.size());
        assertEquals(1, statePacket.bullets.size());
        assertEquals(1, statePacket.items.size());
        assertNotNull(statePacket.boss);
    }

    @Test
    void testStateModification() {
        // Initial state
        statePacket.playerHp = 3;
        statePacket.score = 0;
        
        // Take damage
        statePacket.playerHp = 2;
        assertEquals(2, statePacket.playerHp, "HP should decrease");
        
        // Gain score
        statePacket.score = 500;
        assertEquals(500, statePacket.score, "Score should increase");
    }

    @Test
    void testNullLists_DoNotCauseErrors() {
        statePacket.enemies = null;
        statePacket.bullets = null;
        statePacket.items = null;
        statePacket.boss = null;
        statePacket.enemyDamageEvents = null;
        
        assertNull(statePacket.enemies, "Enemies can be null");
        assertNull(statePacket.bullets, "Bullets can be null");
        assertNull(statePacket.items, "Items can be null");
        assertNull(statePacket.boss, "Boss can be null");
        assertNull(statePacket.enemyDamageEvents, "Damage events can be null");
    }
}