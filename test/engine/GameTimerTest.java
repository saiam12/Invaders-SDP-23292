package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for GameTimer class.
 * Tests timing functionality, pausing, and elapsed time calculations.
 */
class GameTimerTest {

    private GameTimer timer;

    @BeforeEach
    void setUp() {
        timer = new GameTimer();
    }

    @Test
    void testConstructor_InitializesTimer() {
        assertNotNull(timer, "GameTimer should be initialized");
    }

    @Test
    void testGetElapsedSeconds_InitiallyZero() {
        assertEquals(0, timer.getElapsedSeconds(), 
            "Elapsed seconds should be 0 initially");
    }

    @Test
    void testStart_InitializesStartTime() {
        timer.start();
        // No exception should be thrown
        assertTrue(true, "Start should not throw exception");
    }

    @Test
    void testGetElapsedSeconds_AfterStart_ReturnsPositive() throws InterruptedException {
        timer.start();
        Thread.sleep(1100); // Sleep for slightly over 1 second
        
        int elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 1, "Should have at least 1 second elapsed");
    }

    @Test
    void testGetElapsedSeconds_WithoutStart_ReturnsZero() {
        int elapsed = timer.getElapsedSeconds();
        assertEquals(0, elapsed, "Should return 0 if not started");
    }

    @Test
    void testPause_StopsTimer() throws InterruptedException {
        timer.start();
        Thread.sleep(500);
        timer.pause();
        
        int elapsedBeforeSleep = timer.getElapsedSeconds();
        Thread.sleep(1000); // Sleep while paused
        int elapsedAfterSleep = timer.getElapsedSeconds();
        
        assertEquals(elapsedBeforeSleep, elapsedAfterSleep, 
            "Elapsed time should not change while paused");
    }

    @Test
    void testResume_ContinuesTimer() throws InterruptedException {
        timer.start();
        Thread.sleep(500);
        timer.pause();
        
        int elapsedAtPause = timer.getElapsedSeconds();
        
        Thread.sleep(500);
        timer.resume();
        Thread.sleep(1100);
        
        int elapsedAfterResume = timer.getElapsedSeconds();
        assertTrue(elapsedAfterResume >= elapsedAtPause + 1, 
            "Timer should continue after resume");
    }

    @Test
    void testMultiplePauseResumeCycles() throws InterruptedException {
        timer.start();
        
        // Cycle 1
        Thread.sleep(300);
        timer.pause();
        Thread.sleep(200);
        timer.resume();
        
        // Cycle 2
        Thread.sleep(300);
        timer.pause();
        Thread.sleep(200);
        timer.resume();
        
        // Cycle 3
        Thread.sleep(400);
        
        int elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 1, "Should accumulate time across pause/resume cycles");
    }

    @Test
    void testPause_WithoutStart_DoesNotThrow() {
        assertDoesNotThrow(() -> timer.pause(), 
            "Pause without start should not throw exception");
    }

    @Test
    void testResume_WithoutPause_DoesNotThrow() {
        timer.start();
        assertDoesNotThrow(() -> timer.resume(), 
            "Resume without pause should not throw exception");
    }

    @Test
    void testResume_WithoutStart_DoesNotThrow() {
        assertDoesNotThrow(() -> timer.resume(), 
            "Resume without start should not throw exception");
    }

    @Test
    void testDoublePause_DoesNotThrow() {
        timer.start();
        timer.pause();
        assertDoesNotThrow(() -> timer.pause(), 
            "Double pause should not throw exception");
    }

    @Test
    void testDoubleResume_DoesNotThrow() {
        timer.start();
        timer.pause();
        timer.resume();
        assertDoesNotThrow(() -> timer.resume(), 
            "Double resume should not throw exception");
    }

    @Test
    void testDoubleStart_ResetsTimer() throws InterruptedException {
        timer.start();
        Thread.sleep(1100);
        int elapsed1 = timer.getElapsedSeconds();
        
        timer.start(); // Restart
        Thread.sleep(100);
        int elapsed2 = timer.getElapsedSeconds();
        
        assertTrue(elapsed2 < elapsed1, "Second start should reset the timer");
    }

    @Test
    void testGetElapsedSeconds_RoundsDown() throws InterruptedException {
        timer.start();
        Thread.sleep(1500); // 1.5 seconds
        
        int elapsed = timer.getElapsedSeconds();
        assertEquals(1, elapsed, "Should round down to 1 second");
    }

    @Test
    void testGetElapsedSeconds_MultipleSeconds() throws InterruptedException {
        timer.start();
        Thread.sleep(2100); // Just over 2 seconds
        
        int elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 2, "Should show at least 2 seconds");
    }

    @Test
    void testPauseImmediatelyAfterStart() throws InterruptedException {
        timer.start();
        timer.pause();
        
        int elapsed = timer.getElapsedSeconds();
        assertEquals(0, elapsed, "Should be 0 if paused immediately");
    }

    @Test
    void testResumeImmediatelyAfterPause() throws InterruptedException {
        timer.start();
        Thread.sleep(500);
        timer.pause();
        timer.resume();
        Thread.sleep(600);
        
        int elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 1, "Should accumulate time correctly");
    }

    @Test
    void testLongRunningTimer() throws InterruptedException {
        timer.start();
        Thread.sleep(3100); // Just over 3 seconds
        
        int elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 3, "Should handle longer durations");
    }

    @Test
    void testTimerAccuracy() throws InterruptedException {
        timer.start();
        Thread.sleep(5100); // Just over 5 seconds
        
        int elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 5 && elapsed <= 6, 
            "Timer should be reasonably accurate (5-6 seconds)");
    }

    @Test
    void testPausePreservesElapsedTime() throws InterruptedException {
        timer.start();
        Thread.sleep(1100);
        int beforePause = timer.getElapsedSeconds();
        
        timer.pause();
        Thread.sleep(2000); // Wait 2 seconds while paused
        int afterPause = timer.getElapsedSeconds();
        
        assertEquals(beforePause, afterPause, 
            "Elapsed time should be preserved during pause");
    }

    @Test
    void testResumeAddsToElapsedTime() throws InterruptedException {
        timer.start();
        Thread.sleep(1000);
        timer.pause();
        int elapsedAtPause = timer.getElapsedSeconds();
        
        timer.resume();
        Thread.sleep(1100);
        int elapsedAfterResume = timer.getElapsedSeconds();
        
        assertTrue(elapsedAfterResume > elapsedAtPause, 
            "Resume should add to elapsed time");
    }

    @Test
    void testSequentialTimers() throws InterruptedException {
        // First timer run
        timer.start();
        Thread.sleep(1100);
        int elapsed1 = timer.getElapsedSeconds();
        
        // Second timer run (restart)
        timer.start();
        Thread.sleep(1100);
        int elapsed2 = timer.getElapsedSeconds();
        
        assertTrue(elapsed1 >= 1, "First run should have elapsed time");
        assertTrue(elapsed2 >= 1, "Second run should have elapsed time");
    }

    @Test
    void testGetElapsedSeconds_WhilePaused_Consistent() throws InterruptedException {
        timer.start();
        Thread.sleep(500);
        timer.pause();
        
        int elapsed1 = timer.getElapsedSeconds();
        Thread.sleep(100);
        int elapsed2 = timer.getElapsedSeconds();
        Thread.sleep(100);
        int elapsed3 = timer.getElapsedSeconds();
        
        assertEquals(elapsed1, elapsed2, "Elapsed should be consistent while paused (1)");
        assertEquals(elapsed2, elapsed3, "Elapsed should be consistent while paused (2)");
    }

    @Test
    void testRapidPauseResume() throws InterruptedException {
        timer.start();
        
        for (int i = 0; i < 5; i++) {
            Thread.sleep(100);
            timer.pause();
            Thread.sleep(50);
            timer.resume();
        }
        
        Thread.sleep(600);
        int elapsed = timer.getElapsedSeconds();
        
        assertTrue(elapsed >= 1, "Should handle rapid pause/resume cycles");
    }
}