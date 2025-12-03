package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import engine.AnimatedBackground.Star;
import engine.AnimatedBackground.ShootingStar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AnimatedBackground inner classes.
 * Tests Star and ShootingStar data structures and their behavior.
 */
class AnimatedBackgroundTest {

    @Test
    void testStar_Constructor_InitializesFields() {
        Star star = new Star(100.5f, 200.7f, 1.5f);
        
        assertEquals(100.5f, star.baseX, 0.001f, "BaseX should be initialized correctly");
        assertEquals(200.7f, star.baseY, 0.001f, "BaseY should be initialized correctly");
        assertEquals(1.5f, star.speed, 0.001f, "Speed should be initialized correctly");
        assertEquals(0.0f, star.brightness, 0.001f, "Brightness should default to 0");
    }

    @Test
    void testStar_Constructor_WithZeroValues() {
        Star star = new Star(0.0f, 0.0f, 0.0f);
        
        assertEquals(0.0f, star.baseX, 0.001f, "BaseX should handle 0");
        assertEquals(0.0f, star.baseY, 0.001f, "BaseY should handle 0");
        assertEquals(0.0f, star.speed, 0.001f, "Speed should handle 0");
    }

    @Test
    void testStar_Constructor_WithNegativeValues() {
        Star star = new Star(-50.0f, -100.0f, -2.0f);
        
        assertEquals(-50.0f, star.baseX, 0.001f, "BaseX should handle negative values");
        assertEquals(-100.0f, star.baseY, 0.001f, "BaseY should handle negative values");
        assertEquals(-2.0f, star.speed, 0.001f, "Speed should handle negative values");
    }

    @Test
    void testStar_Constructor_WithLargeValues() {
        Star star = new Star(10000.0f, 20000.0f, 100.0f);
        
        assertEquals(10000.0f, star.baseX, 0.001f, "BaseX should handle large values");
        assertEquals(20000.0f, star.baseY, 0.001f, "BaseY should handle large values");
        assertEquals(100.0f, star.speed, 0.001f, "Speed should handle large values");
    }

    @Test
    void testStar_FloatingPointPrecision() {
        Star star = new Star(123.456789f, 987.654321f, 5.555555f);
        
        assertEquals(123.456789f, star.baseX, 0.0001f, "Should preserve floating point precision for baseX");
        assertEquals(987.654321f, star.baseY, 0.0001f, "Should preserve floating point precision for baseY");
        assertEquals(5.555555f, star.speed, 0.0001f, "Should preserve floating point precision for speed");
    }

    @Test
    void testStar_BrightnessOffset_RandomInitialization() {
        Star star = new Star(100.0f, 200.0f, 1.0f);
        
        assertTrue(star.brightnessOffset >= 0.0f, "Brightness offset should be non-negative");
        assertTrue(star.brightnessOffset <= Math.PI * 2, "Brightness offset should be within 2π");
    }

    @Test
    void testStar_BrightnessOffset_MultipleStars_DifferentValues() {
        Star star1 = new Star(100.0f, 100.0f, 1.0f);
        Star star2 = new Star(100.0f, 100.0f, 1.0f);
        Star star3 = new Star(100.0f, 100.0f, 1.0f);
        
        // With randomization, it's very unlikely all three will be equal
        boolean allDifferent = star1.brightnessOffset != star2.brightnessOffset 
            || star2.brightnessOffset != star3.brightnessOffset;
        
        assertTrue(allDifferent, "Multiple stars should likely have different brightness offsets");
    }

    @Test
    void testStar_FieldMutability() {
        Star star = new Star(100.0f, 200.0f, 1.0f);
        
        // Test that fields can be modified
        star.baseX = 300.0f;
        star.baseY = 400.0f;
        star.speed = 2.5f;
        star.brightness = 0.8f;
        
        assertEquals(300.0f, star.baseX, 0.001f, "BaseX should be mutable");
        assertEquals(400.0f, star.baseY, 0.001f, "BaseY should be mutable");
        assertEquals(2.5f, star.speed, 0.001f, "Speed should be mutable");
        assertEquals(0.8f, star.brightness, 0.001f, "Brightness should be mutable");
    }

    @Test
    void testShootingStar_Constructor_InitializesFields() {
        ShootingStar shootingStar = new ShootingStar(150.0f, 250.0f, 5.0f, 3.0f);
        
        assertEquals(150.0f, shootingStar.x, 0.001f, "X should be initialized correctly");
        assertEquals(250.0f, shootingStar.y, 0.001f, "Y should be initialized correctly");
        assertEquals(5.0f, shootingStar.speedX, 0.001f, "SpeedX should be initialized correctly");
        assertEquals(3.0f, shootingStar.speedY, 0.001f, "SpeedY should be initialized correctly");
    }

    @Test
    void testShootingStar_Constructor_WithZeroValues() {
        ShootingStar shootingStar = new ShootingStar(0.0f, 0.0f, 0.0f, 0.0f);
        
        assertEquals(0.0f, shootingStar.x, 0.001f, "X should handle 0");
        assertEquals(0.0f, shootingStar.y, 0.001f, "Y should handle 0");
        assertEquals(0.0f, shootingStar.speedX, 0.001f, "SpeedX should handle 0");
        assertEquals(0.0f, shootingStar.speedY, 0.001f, "SpeedY should handle 0");
    }

    @Test
    void testShootingStar_Constructor_WithNegativeValues() {
        ShootingStar shootingStar = new ShootingStar(-100.0f, -200.0f, -5.0f, -3.0f);
        
        assertEquals(-100.0f, shootingStar.x, 0.001f, "X should handle negative values");
        assertEquals(-200.0f, shootingStar.y, 0.001f, "Y should handle negative values");
        assertEquals(-5.0f, shootingStar.speedX, 0.001f, "SpeedX should handle negative values");
        assertEquals(-3.0f, shootingStar.speedY, 0.001f, "SpeedY should handle negative values");
    }

    @Test
    void testShootingStar_Constructor_WithLargeValues() {
        ShootingStar shootingStar = new ShootingStar(5000.0f, 10000.0f, 50.0f, 100.0f);
        
        assertEquals(5000.0f, shootingStar.x, 0.001f, "X should handle large values");
        assertEquals(10000.0f, shootingStar.y, 0.001f, "Y should handle large values");
        assertEquals(50.0f, shootingStar.speedX, 0.001f, "SpeedX should handle large values");
        assertEquals(100.0f, shootingStar.speedY, 0.001f, "SpeedY should handle large values");
    }

    @Test
    void testShootingStar_DifferentSpeeds() {
        ShootingStar fast = new ShootingStar(100.0f, 100.0f, 10.0f, 10.0f);
        ShootingStar slow = new ShootingStar(100.0f, 100.0f, 1.0f, 1.0f);
        
        assertTrue(fast.speedX > slow.speedX, "Fast shooting star should have higher speedX");
        assertTrue(fast.speedY > slow.speedY, "Fast shooting star should have higher speedY");
    }

    @Test
    void testShootingStar_DiagonalMovement() {
        ShootingStar diagonal = new ShootingStar(200.0f, 300.0f, 4.0f, 4.0f);
        
        assertEquals(diagonal.speedX, diagonal.speedY, 0.001f, 
            "Diagonal movement should have equal X and Y speeds");
    }

    @Test
    void testShootingStar_HorizontalMovement() {
        ShootingStar horizontal = new ShootingStar(100.0f, 200.0f, 5.0f, 0.0f);
        
        assertTrue(horizontal.speedX != 0.0f, "Horizontal movement should have non-zero X speed");
        assertEquals(0.0f, horizontal.speedY, 0.001f, "Horizontal movement should have zero Y speed");
    }

    @Test
    void testShootingStar_VerticalMovement() {
        ShootingStar vertical = new ShootingStar(100.0f, 200.0f, 0.0f, 5.0f);
        
        assertEquals(0.0f, vertical.speedX, 0.001f, "Vertical movement should have zero X speed");
        assertTrue(vertical.speedY != 0.0f, "Vertical movement should have non-zero Y speed");
    }

    @Test
    void testShootingStar_FieldMutability() {
        ShootingStar shootingStar = new ShootingStar(100.0f, 200.0f, 5.0f, 3.0f);
        
        // Test that fields can be modified (for animation updates)
        shootingStar.x = 150.0f;
        shootingStar.y = 250.0f;
        shootingStar.speedX = 6.0f;
        shootingStar.speedY = 4.0f;
        
        assertEquals(150.0f, shootingStar.x, 0.001f, "X should be mutable");
        assertEquals(250.0f, shootingStar.y, 0.001f, "Y should be mutable");
        assertEquals(6.0f, shootingStar.speedX, 0.001f, "SpeedX should be mutable");
        assertEquals(4.0f, shootingStar.speedY, 0.001f, "SpeedY should be mutable");
    }

    @Test
    void testShootingStar_SimulateMovement() {
        ShootingStar shootingStar = new ShootingStar(100.0f, 200.0f, 5.0f, 3.0f);
        
        // Simulate one frame of movement
        shootingStar.x += shootingStar.speedX;
        shootingStar.y += shootingStar.speedY;
        
        assertEquals(105.0f, shootingStar.x, 0.001f, "X should move by speedX");
        assertEquals(203.0f, shootingStar.y, 0.001f, "Y should move by speedY");
    }

    @Test
    void testShootingStar_MultipleFramesMovement() {
        ShootingStar shootingStar = new ShootingStar(0.0f, 0.0f, 2.0f, 1.5f);
        
        // Simulate 10 frames
        for (int i = 0; i < 10; i++) {
            shootingStar.x += shootingStar.speedX;
            shootingStar.y += shootingStar.speedY;
        }
        
        assertEquals(20.0f, shootingStar.x, 0.001f, "X should move correctly over multiple frames");
        assertEquals(15.0f, shootingStar.y, 0.001f, "Y should move correctly over multiple frames");
    }

    @Test
    void testStar_DifferentSpeedStars() {
        Star slowStar = new Star(100.0f, 100.0f, 0.5f);
        Star mediumStar = new Star(100.0f, 100.0f, 1.0f);
        Star fastStar = new Star(100.0f, 100.0f, 2.0f);
        
        assertTrue(slowStar.speed < mediumStar.speed, "Slow star should be slower than medium");
        assertTrue(mediumStar.speed < fastStar.speed, "Medium star should be slower than fast");
    }

    @Test
    void testStar_BrightnessRange() {
        Star star = new Star(100.0f, 200.0f, 1.0f);
        
        // Test setting various brightness values
        star.brightness = 0.0f;
        assertEquals(0.0f, star.brightness, 0.001f, "Should handle minimum brightness");
        
        star.brightness = 0.5f;
        assertEquals(0.5f, star.brightness, 0.001f, "Should handle medium brightness");
        
        star.brightness = 1.0f;
        assertEquals(1.0f, star.brightness, 0.001f, "Should handle maximum brightness");
    }

    @Test
    void testShootingStar_OppositeDirections() {
        ShootingStar leftUp = new ShootingStar(200.0f, 200.0f, -5.0f, -3.0f);
        ShootingStar rightDown = new ShootingStar(200.0f, 200.0f, 5.0f, 3.0f);
        
        assertTrue(leftUp.speedX < 0, "Left-up should have negative X speed");
        assertTrue(leftUp.speedY < 0, "Left-up should have negative Y speed");
        assertTrue(rightDown.speedX > 0, "Right-down should have positive X speed");
        assertTrue(rightDown.speedY > 0, "Right-down should have positive Y speed");
    }

    @Test
    void testShootingStar_FloatingPointPrecision() {
        ShootingStar shootingStar = new ShootingStar(123.456f, 789.012f, 3.141592f, 2.718281f);
        
        assertEquals(123.456f, shootingStar.x, 0.001f, "Should preserve X precision");
        assertEquals(789.012f, shootingStar.y, 0.001f, "Should preserve Y precision");
        assertEquals(3.141592f, shootingStar.speedX, 0.00001f, "Should preserve speedX precision");
        assertEquals(2.718281f, shootingStar.speedY, 0.00001f, "Should preserve speedY precision");
    }

    @Test
    void testStar_CreateMultipleStars_IndependentState() {
        Star star1 = new Star(100.0f, 100.0f, 1.0f);
        Star star2 = new Star(200.0f, 200.0f, 2.0f);
        
        star1.brightness = 0.5f;
        star2.brightness = 0.8f;
        
        assertEquals(0.5f, star1.brightness, 0.001f, "Star1 brightness should be independent");
        assertEquals(0.8f, star2.brightness, 0.001f, "Star2 brightness should be independent");
    }

    @Test
    void testShootingStar_CreateMultiple_IndependentState() {
        ShootingStar ss1 = new ShootingStar(0.0f, 0.0f, 1.0f, 1.0f);
        ShootingStar ss2 = new ShootingStar(100.0f, 100.0f, 2.0f, 2.0f);
        
        ss1.x += ss1.speedX;
        ss2.x += ss2.speedX;
        
        assertEquals(1.0f, ss1.x, 0.001f, "SS1 position should be independent");
        assertEquals(102.0f, ss2.x, 0.001f, "SS2 position should be independent");
    }
}