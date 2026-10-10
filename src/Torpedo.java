import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a torpedo projectile fired in the DEPTHS game.
 *
 * Torpedoes are the primary projectile used by both the player's submarine
 * (friendly) and sea-creature enemies (hostile). A friendly torpedo travels
 * upward and is drawn in yellow; an enemy torpedo travels downward and is
 * drawn as a red circle.
 *
 * <p>Instances are created by:</p>
 * <ul>
 *   <li>The player (via the shoot mechanic in Game) with {@code friendly = true}</li>
 *   <li>Enemy creatures (via {@code SeaCreature.canShoot()}) with {@code friendly = false}</li>
 * </ul>
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public class Torpedo {

    /** Current horizontal position. */
    private double x;

    /** Current vertical position (changes each frame). */
    private double y;

    /** Vertical velocity: negative (upward) for friendly, positive (downward) for enemy. */
    private double dy;

    /** Damage this torpedo deals on contact. */
    private int damage;

    /** Whether this torpedo was fired by the player ({@code true}) or an enemy ({@code false}). */
    private boolean friendly;

    /** Colour used when drawing: yellow for player torpedoes, red for enemy shots. */
    private Color color;

    /** Collision detection radius in pixels. */
    private static final double RADIUS = 5;

    /**
     * Creates a torpedo at the given position.
     *
     * @param x        initial horizontal centre position
     * @param y        initial vertical centre position
     * @param friendly {@code true} if fired by the player, {@code false} if fired by an enemy
     */
    public Torpedo(double x, double y, boolean friendly) {
        this.x = x;
        this.y = y;
        this.friendly = friendly;

        if (friendly) {
            dy = -8;               // player torpedoes travel upward
            color = Color.YELLOW;
            damage = 1;
        } else {
            dy = 4;                // enemy shots travel downward
            color = Color.RED;
            damage = 1;
        }
    }

    /**
     * Advances the torpedo's position by one frame.
     * Called once per game-loop iteration.
     */
    public void update() {
        y += dy;
    }

    /**
     * Draws the torpedo on the canvas.
     * Friendly torpedoes are drawn as elongated oval "shells"; enemy shots
     * are drawn as filled circles.
     *
     * @param gc the GraphicsContext to draw on
     */
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        if (friendly) {
            gc.fillOval(x - 3, y - 6, 6, 12);   // tall oval for player shot
        } else {
            gc.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        }
    }

    /**
     * Returns whether this torpedo is still within the visible canvas area.
     * Torpedoes that leave the screen should be removed from the game.
     *
     * @return {@code true} if the torpedo is on screen
     */
    public boolean onScreen() {
        return y > -10 && y < Game.HEIGHT + 10;
    }

    /**
     * Checks whether this torpedo overlaps a circular target.
     * Uses a simple distance-based check between the torpedo's centre
     * and the target's centre.
     *
     * @param ox     target centre x
     * @param oy     target centre y
     * @param radius target collision radius
     * @return {@code true} if the torpedo is within (radius + RADIUS) pixels of the target
     */
    public boolean inContact(double ox, double oy, double radius) {
        double dx = x - ox;
        double dy = y - oy;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < radius + RADIUS;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    /** @return {@code true} if this torpedo was fired by the player */
    public boolean isFriendly() { return friendly; }

    /** @return current horizontal position */
    public double getX() { return x; }

    /** @return current vertical position */
    public double getY() { return y; }

    /** @return damage dealt on contact */
    public int getDamage() { return damage; }

    /** @return collision radius in pixels */
    public double getRadius() { return RADIUS; }
}