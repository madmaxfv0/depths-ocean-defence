import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all collectible power-ups in DEPTHS.
 *
 * Power-ups fall downward from the position they were spawned (typically
 * where an enemy was destroyed). The player's submarine collects them by
 * making contact, triggering the power-up's specific effect via
 * {@link #activate(Submarine)}.
 *
 * Concrete subclasses must provide:
 * <ul>
 *   <li>{@link #draw(GraphicsContext)} — how the power-up looks on screen</li>
 *   <li>{@link #activate(Submarine)} — what effect it applies to the submarine</li>
 * </ul>
 *
 * This design demonstrates OOP abstraction and polymorphism: the game loop
 * calls {@code powerUp.activate(sub)} without needing to know the concrete
 * type, and each subclass applies a different effect.
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public abstract class PowerUp {

    /** Current horizontal position. */
    protected double x;

    /** Current vertical position (increases each frame as it falls). */
    protected double y;

    /** Fall speed in pixels per frame. */
    protected double fallSpeed = 2;

    /**
     * Creates a power-up at the given position.
     *
     * @param x initial horizontal position
     * @param y initial vertical position
     */
    public PowerUp(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Moves the power-up downward by {@code fallSpeed} pixels.
     * Called once per game-loop frame.
     */
    public void update() {
        y += fallSpeed;
    }

    /**
     * Draws the power-up on the canvas.
     * Each subclass provides a distinct visual so the player can identify
     * the effect before collecting it.
     *
     * @param gc the GraphicsContext to draw on
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Applies this power-up's effect to the given submarine.
     * Called by the game loop when the submarine touches this power-up.
     *
     * @param sub the player's submarine
     */
    public abstract void activate(Submarine sub);

    /**
     * Checks whether this power-up has made contact with the submarine.
     * Uses a circular distance check with a generous 20-pixel radius so
     * collection feels responsive to the player.
     *
     * @param sx submarine centre x
     * @param sy submarine centre y
     * @return {@code true} if the power-up overlaps the submarine
     */
    public boolean inContact(double sx, double sy) {
        double dx = x - sx;
        double dy = y - sy;
        return Math.sqrt(dx * dx + dy * dy) < 20;
    }

    /**
     * Returns whether this power-up is still within the visible canvas.
     *
     * @return {@code true} if the power-up has not fallen off the bottom
     */
    public boolean isOnScreen() {
        return y < Game.HEIGHT + 20;
    }

    /** @return current horizontal position */
    public double getX() { return x; }

    /** @return current vertical position */
    public double getY() { return y; }
}


// ─── Concrete power-up types ────────────────────────────────────────────────

/**
 * Spread Shot power-up — collected as a green upward-pointing triangle.
 *
 * When activated, the submarine fires three torpedoes simultaneously
 * (left, centre, right) instead of a single shot, for approximately
 * 10 seconds.
 */
class SpreadShot extends PowerUp {

    /**
     * Creates a Spread Shot power-up at the given position.
     *
     * @param x horizontal spawn position
     * @param y vertical spawn position
     */
    public SpreadShot(double x, double y) {
        super(x, y);
    }

    /**
     * Draws a lime-green upward triangle to indicate the spread-shot effect.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.LIMEGREEN);
        double[] xs = {x, x - 8, x + 8};
        double[] ys = {y + 8, y - 8, y - 8};
        gc.fillPolygon(xs, ys, 3);
    }

    /**
     * Activates spread-shot mode on the submarine.
     *
     * @param sub the player's submarine
     */
    @Override
    public void activate(Submarine sub) {
        sub.activateSpreadShot();
    }
}


/**
 * Shield power-up — collected as a cyan diamond shape.
 *
 * When activated, the submarine gains a protective shield that absorbs
 * the next incoming enemy hit without costing a life.
 */
class Shield extends PowerUp {

    /**
     * Creates a Shield power-up at the given position.
     *
     * @param x horizontal spawn position
     * @param y vertical spawn position
     */
    public Shield(double x, double y) {
        super(x, y);
    }

    /**
     * Draws a cyan diamond to indicate the shield effect.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.CYAN);
        double[] xs = {x, x - 8, x, x + 8};
        double[] ys = {y - 10, y, y + 10, y};
        gc.fillPolygon(xs, ys, 4);
    }

    /**
     * Activates the shield on the submarine.
     *
     * @param sub the player's submarine
     */
    @Override
    public void activate(Submarine sub) {
        sub.activateShield();
    }
}


/**
 * Depth Charge power-up — collected as a red circle with a "!" label.
 *
 * When activated, the depth charge is armed on the submarine via
 * {@link Submarine#armDepthCharge()}. The game loop detects the pending
 * flag and applies area-of-effect damage to every living enemy on screen,
 * awarding bonus points for each kill.
 *
 * Separating the "arming" (here) from the "detonation" (in Game) keeps
 * the PowerUp class independent of the enemy list, preserving clean
 * encapsulation while still providing a meaningful activate() implementation.
 */
class DepthCharge extends PowerUp {

    /**
     * Creates a Depth Charge power-up at the given position.
     *
     * @param x horizontal spawn position
     * @param y vertical spawn position
     */
    public DepthCharge(double x, double y) {
        super(x, y);
    }

    /**
     * Draws a red circle with an exclamation mark to signal danger/explosion.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillOval(x - 6, y - 6, 12, 12);
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.fillText("!", x - 2, y + 4);
    }

    /**
     * Arms the depth charge on the submarine.
     *
     * The submarine stores a {@code depthChargePending} flag which the game
     * loop checks each frame. When detected, the game loop detonates the
     * charge (damaging all enemies) and then clears the flag by calling
     * {@link Submarine#consumeDepthCharge()}.
     *
     * @param sub the player's submarine
     */
    @Override
    public void activate(Submarine sub) {
        sub.armDepthCharge();
    }
}