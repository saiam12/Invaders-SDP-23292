package entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import engine.DrawManager;
import engine.Core;

/**
 * Manages enemies for Infinite Mode.
 * Unlike EnemyShipFormation, this doesn't use a grid layout.
 * Manages a free-floating list of InfiniteEnemyShip objects.
 */
public class InfiniteEnemyFormation implements Iterable<InfiniteEnemyShip> {

    /** List of active enemies */
    private List<InfiniteEnemyShip> enemies;

    /** DrawManager for rendering */
    private DrawManager drawManager;

    /** Count of destroyed enemies */
    private int destroyedCount;

    /**
     * Constructor.
     */
    public InfiniteEnemyFormation() {
        this.enemies = new ArrayList<>();
        this.drawManager = Core.getDrawManager();
        this.destroyedCount = 0;
    }

    /**
     * Adds an enemy to the formation.
     *
     * @param enemy Enemy to add
     */
    public void addEnemy(final InfiniteEnemyShip enemy) {
        this.enemies.add(enemy);
    }

    /**
     * Updates all enemies and removes dead/off-screen ones.
     */
    public void update() {
        Iterator<InfiniteEnemyShip> iterator = enemies.iterator();

        while (iterator.hasNext()) {
            InfiniteEnemyShip enemy = iterator.next();

            if (!enemy.isDestroyed()) {
                enemy.update();
            }

            // Remove if off-screen
            if (enemy.shouldDespawn()) {
                iterator.remove();
                continue;
            }

            // Remove if explosion finished
            if (enemy.isExplosionFinished()) {
                iterator.remove();
                this.destroyedCount++;
            }
        }
    }

    /**
     * Draws all enemies.
     */
    public void draw() {
        for (InfiniteEnemyShip enemy : enemies) {
            drawManager.drawEntity(enemy, enemy.getPositionX(), enemy.getPositionY());
        }
    }

    /**
     * Destroys a specific enemy.
     *
     * @param enemy Enemy to destroy
     */
    public void destroy(final InfiniteEnemyShip enemy) {
        if (enemy != null && !enemy.isDestroyed()) {
            enemy.destroy();
        }
    }

    /**
     * Shoots bullets from enemies (optional feature).
     *
     * @param bullets Set to add bullets to
     */
    public void shoot(final Set<Bullet> bullets) {
        // Optional: Implement enemy shooting
        // For now, infinite mode enemies don't shoot
    }

    /**
     * Returns iterator over enemies.
     *
     * @return Iterator
     */
    @Override
    public Iterator<InfiniteEnemyShip> iterator() {
        return enemies.iterator();
    }

    /**
     * Checks if formation is empty.
     *
     * @return True if no enemies
     */
    public boolean isEmpty() {
        return enemies.isEmpty();
    }

    /**
     * Gets current enemy count.
     *
     * @return Number of active enemies
     */
    public int getEnemyCount() {
        return enemies.size();
    }

    /**
     * Gets total destroyed count.
     *
     * @return Number of destroyed enemies
     */
    public int getDestroyedCount() {
        return destroyedCount;
    }

    /**
     * Clears all enemies.
     */
    public void clear() {
        enemies.clear();
    }

    /**
     * Gets the list of enemies (for collision checking).
     *
     * @return List of enemies
     */
    public List<InfiniteEnemyShip> getEnemies() {
        return enemies;
    }
}