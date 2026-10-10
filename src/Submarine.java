import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents the player-controlled submarine in the DEPTHS game.
 *
 * The submarine can move left and right across the bottom of the screen,
 * fire torpedoes upward, and carry power-up effects (spread shot, shield,
 * depth charge). A brief invincibility window is granted after each hit
 * to prevent instant consecutive damage.
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public class Submarine {

    /** Current horizontal and vertical position of the submarine centre. */
    private double x, y;

    /** Horizontal movement speed in pixels per frame. */
    private double speed = 4;

    /** True while the shield power-up is active. */
    private boolean shielded = false;

    /** True while the spread-shot power-up is active. */
    private boolean spreadShot = false;

    /** Remaining frames for the spread-shot effect. */
    private int spreadShotTimer = 0;

    /** Remaining frames of post-hit invincibility. */
    private int invincibleTimer = 0;

    /** True while the submarine is invincible (cannot take damage). */
    private boolean invincible = false;

    /**
     * True when a Depth Charge power-up has been collected and is ready
     * to be detonated by the game loop. The game loop reads this flag,
     * detonates the charge, then calls {@link #consumeDepthCharge()} to
     * clear it — keeping the activation logic properly separated between
     * the PowerUp class and the Game class.
     */
    private boolean depthChargePending = false;

    /** Half-width used for boundary clamping and collision radius. */
    private static final double WIDTH = 30;

    /** Half-height (cosmetic reference). */
    private static final double HEIGHT = 20;

    /**
     * Creates a new submarine centred at the given coordinates.
     *
     * @param x initial horizontal centre position
     * @param y initial vertical centre position
     */
    public Submarine(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Moves the submarine one step to the left, clamped to the left edge.
     */
    public void moveLeft() {
        x -= speed;
        if (x < WIDTH) {
            x = WIDTH;
        }
    }

    /**
     * Moves the submarine one step to the right, clamped to the right edge.
     */
    public void moveRight() {
        x += speed;
        if (x > Game.WIDTH - WIDTH) {
            x = Game.WIDTH - WIDTH;
        }
    }

    /**
     * Updates per-frame timers for spread shot and invincibility.
     * Called once per game loop iteration.
     */
    public void update() {
        // Count down spread-shot duration
        if (spreadShotTimer > 0) {
            spreadShotTimer--;
        } else {
            spreadShot = false;
        }

        // Count down post-hit invincibility
        if (invincibleTimer > 0) {
            invincibleTimer--;
        } else {
            invincible = false;
        }
    }

    /**
     * Draws the submarine on the canvas.
     * While invincible the submarine flickers (drawn every other 10-frame
     * block) to give visual feedback to the player.
     *
     * @param gc the GraphicsContext to draw on
     */
    public void draw(GraphicsContext gc) {
        // Flicker effect during invincibility
        if (invincible && invincibleTimer % 10 < 5) {
            return;
        }

        // Main hull
        gc.setFill(Color.YELLOW);
        gc.fillOval(x - 15, y - 10, 30, 20);

        // Conning tower
        gc.setFill(Color.GOLD);
        gc.fillRect(x - 5, y - 20, 10, 10);

        // Periscope tip
        gc.setFill(Color.DARKGRAY);
        gc.fillOval(x - 2, y - 25, 4, 4);

        // Shield ring (shown when shield power-up is active)
        if (shielded) {
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2);
            gc.strokeOval(x - 25, y - 25, 50, 50);
        }
    }

    /**
     * Processes a hit on the submarine.
     *
     * <ul>
     *   <li>If already invincible, the hit is ignored.</li>
     *   <li>If the shield is active, it absorbs the hit and grants a brief
     *       invincibility window without losing a life.</li>
     *   <li>Otherwise a life is lost and an invincibility window is started.</li>
     * </ul>
     *
     * @return {@code true} if the player should lose a life, {@code false}
     *         if the hit was absorbed or ignored
     */
    public boolean hit() {
        if (invincible) return false;

        if (shielded) {
            shielded = false;
            invincible = true;
            invincibleTimer = 60; // ~1 second at 60 fps
            return false;
        }

        invincible = true;
        invincibleTimer = 60;
        return true;
    }

    // ─── Power-up activators ────────────────────────────────────────────────

    /**
     * Activates the shield power-up, blocking the next incoming hit.
     */
    public void activateShield() {
        shielded = true;
    }

    /**
     * Activates spread-shot mode for approximately 10 seconds (600 frames).
     */
    public void activateSpreadShot() {
        spreadShot = true;
        spreadShotTimer = 600;
    }

    /**
     * Arms the depth-charge power-up.
     * The game loop detects this flag via {@link #isDepthChargePending()},
     * applies the area-of-effect damage to all enemies, and then calls
     * {@link #consumeDepthCharge()} to clear the flag.
     */
    public void armDepthCharge() {
        depthChargePending = true;
    }

    /**
     * Returns whether a depth charge is primed and waiting to detonate.
     *
     * @return {@code true} if a depth charge should be detonated this frame
     */
    public boolean isDepthChargePending() {
        return depthChargePending;
    }

    /**
     * Clears the depth-charge flag after the game loop has processed the
     * detonation. Must be called by the game loop immediately after handling
     * the depth-charge effect.
     */
    public void consumeDepthCharge() {
        depthChargePending = false;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    /** @return the horizontal centre position */
    public double getX() { return x; }

    /** @return the vertical centre position */
    public double getY() { return y; }

    /** @return {@code true} if spread-shot mode is currently active */
    public boolean isSpreadShot() { return spreadShot; }

    /**
     * Returns the collision radius used for hit detection.
     *
     * @return radius in pixels
     */
    public double getRadius() { return 15; }
}