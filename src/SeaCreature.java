import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Abstract base class for all sea-creature enemies in DEPTHS.
 *
 * SeaCreature defines the common state and behaviour shared by every enemy
 * type — position, health, movement direction, frame counter — and declares
 * the abstract methods that each concrete creature must implement differently.
 *
 * The class also provides static factory methods ({@link #createJellyfish},
 * {@link #createSquid}, {@link #createAnglerfish}, {@link #createKraken})
 * so the game can spawn enemies without depending on concrete subclass names.
 *
 * <p>OOP principles demonstrated:</p>
 * <ul>
 *   <li><b>Abstraction</b>: {@code draw()} and {@code getPoints()} are abstract;
 *       each subclass provides its own implementation.</li>
 *   <li><b>Polymorphism</b>: the game loop calls {@code c.draw(gc)} and
 *       {@code c.getPoints()} on a {@code SeaCreature} reference, and the
 *       correct subclass method runs at runtime.</li>
 *   <li><b>Encapsulation</b>: fields are {@code protected} and only exposed
 *       where needed; no public mutable field access.</li>
 *   <li><b>Inheritance</b>: Jellyfish, Squid, Anglerfish, and Kraken all
 *       extend this class, inheriting movement and damage logic.</li>
 * </ul>
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public abstract class SeaCreature {

    /** Horizontal centre position. */
    protected double x;

    /** Vertical centre position. */
    protected double y;

    /** Current hit points. */
    protected int health;

    /** Maximum hit points (used for health-bar scaling). */
    protected int maxHealth;

    /** Movement speed in pixels per frame. */
    protected double speed;

    /** Whether this creature is still alive. */
    protected boolean alive;

    /** Movement direction: +1 (right) or -1 (left). */
    protected int direction;

    /** Frame counter, incremented in {@link #update()} and used by draw methods for animations. */
    protected long frameCount;

    /** Shared random number generator — one instance reused across all creatures. */
    private static final Random random = new Random();

    /**
     * Creates a sea creature with the given initial properties.
     *
     * @param x       initial horizontal position
     * @param y       initial vertical position
     * @param health  starting hit points
     * @param speed   movement speed in pixels per frame
     */
    public SeaCreature(double x, double y, int health, double speed) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.alive = true;
        this.direction = 1;
        this.frameCount = 0;
    }

    /**
     * Draws this creature on the canvas.
     * Each concrete subclass provides its own vector-art implementation.
     *
     * @param gc the GraphicsContext to draw on
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Returns the point value awarded when this creature is destroyed.
     * Different enemy types are worth different amounts to encourage
     * players to prioritise higher-value targets.
     *
     * @return points awarded on kill
     */
    public abstract int getPoints();

    /**
     * Advances this creature's position by one frame.
     * The creature moves horizontally according to its speed and direction,
     * and the frame counter is incremented for animation purposes.
     */
    public void update() {
        x += speed * direction;
        frameCount++;
    }

    /**
     * Moves this creature downward by a fixed step when the formation
     * reaches a screen edge — identical to the classic Space Invaders mechanic.
     */
    public void stepDown() { y += 14; }

    /**
     * Reverses the horizontal movement direction.
     * Called by the game loop when any creature in the formation touches
     * a screen edge, so the whole grid turns together.
     */
    public void reverseDirection() { direction *= -1; }

    /**
     * Applies damage to this creature.
     * If health drops to zero or below the creature is marked dead.
     *
     * @param d amount of damage to apply (typically the torpedo's damage value)
     */
    public void takeDamage(int d) {
        health -= d;
        if (health <= 0) alive = false;
    }

    /**
     * Randomly decides whether this creature fires a shot this frame.
     * Uses a low probability so shots are spread unpredictably across
     * many creatures and many frames.
     *
     * @return {@code true} if the creature fires this frame
     */
    public boolean canShoot() {
        return alive && random.nextInt(800) < 2;
    }

    /**
     * Returns the current position as a two-element array [x, y].
     * Used by the game loop for collision detection and boundary checks.
     *
     * @return {@code double[]{x, y}}
     */
    public double[] getPos() { return new double[]{x, y}; }

    /**
     * Returns whether this creature is still alive.
     *
     * @return {@code true} if the creature has not been destroyed
     */
    public boolean isAlive() { return alive; }

    /**
     * Draws a small health bar above the creature.
     * Only drawn for creatures with more than one max hit point, so
     * single-hit enemies do not clutter the screen.
     *
     * @param gc      the GraphicsContext to draw on
     * @param offsetY vertical offset from the creature's centre to the bar
     */
    protected void drawHealthBar(GraphicsContext gc, double offsetY) {
        if (maxHealth > 1 && alive) {
            double w = 26;
            gc.setFill(Color.rgb(80, 0, 0));
            gc.fillRect(x - w / 2, y + offsetY, w, 3);
            gc.setFill(Color.rgb(0, 220, 80));
            gc.fillRect(x - w / 2, y + offsetY, w * ((double) health / maxHealth), 3);
        }
    }

    // ─── Static factory methods ──────────────────────────────────────────────

    /**
     * Creates a new Jellyfish enemy (1 HP, 10 pts).
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed movement speed
     * @return a new {@link Jellyfish} instance
     */
    public static SeaCreature createJellyfish(double x, double y, double speed) {
        return new Jellyfish(x, y, speed);
    }

    /**
     * Creates a new Squid enemy (1 HP, 20 pts).
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed movement speed
     * @return a new {@link Squid} instance
     */
    public static SeaCreature createSquid(double x, double y, double speed) {
        return new Squid(x, y, speed);
    }

    /**
     * Creates a new Anglerfish enemy (2 HP, 30 pts, shoots more frequently).
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed movement speed
     * @return a new {@link Anglerfish} instance
     */
    public static SeaCreature createAnglerfish(double x, double y, double speed) {
        return new Anglerfish(x, y, speed);
    }

    /**
     * Creates a new Kraken boss enemy (high HP, 100 pts, very frequent shooting).
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed movement speed
     * @param wave  current wave number, used to scale the Kraken's hit points
     * @return a new {@link Kraken} instance
     */
    public static SeaCreature createKraken(double x, double y, double speed, int wave) {
        return new Kraken(x, y, speed, wave);
    }
}


// ─── Concrete enemy subclasses ───────────────────────────────────────────────

/**
 * Jellyfish — the weakest enemy type.
 *
 * 1 HP, 10 points. Drawn as a translucent dome with animated trailing tentacles.
 * The tentacle animation uses a sine-wave offset driven by the inherited
 * {@code frameCount} field.
 */
class Jellyfish extends SeaCreature {

    /**
     * Creates a Jellyfish at the given position.
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed movement speed
     */
    public Jellyfish(double x, double y, double speed) {
        super(x, y, 1, speed);
    }

    /**
     * Draws the jellyfish as a layered translucent dome with animated tentacles.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Outer glow halo
        gc.setFill(Color.rgb(100, 180, 255, 0.12));
        gc.fillOval(x - 18, y - 18, 36, 30);

        // Main bell
        gc.setFill(Color.rgb(120, 190, 255, 0.6));
        gc.fillOval(x - 13, y - 13, 26, 20);

        // Inner highlight
        gc.setFill(Color.rgb(180, 220, 255, 0.4));
        gc.fillOval(x - 8, y - 11, 16, 12);

        // Bell outline
        gc.setStroke(Color.rgb(100, 170, 240, 0.5));
        gc.setLineWidth(1);
        gc.strokeOval(x - 13, y - 13, 26, 20);

        // Animated tentacles — sine wave based on frameCount
        gc.setStroke(Color.rgb(130, 190, 255, 0.45));
        gc.setLineWidth(1.5);
        for (int i = -2; i <= 2; i++) {
            double tx = x + i * 5;
            double prevX = tx;
            double prevY = y + 5;
            for (int j = 1; j <= 4; j++) {
                double wave = Math.sin(frameCount * 0.08 + i + j * 0.5) * (3 + j);
                double nextX = tx + wave;
                double nextY = y + 5 + j * 5;
                gc.strokeLine(prevX, prevY, nextX, nextY);
                prevX = nextX;
                prevY = nextY;
            }
        }

        // Bioluminescent spots
        gc.setFill(Color.rgb(200, 130, 255, 0.5));
        gc.fillOval(x - 4, y - 3, 3, 4);
        gc.fillOval(x + 1, y - 3, 3, 4);
    }

    /** @return 10 — the point value for destroying a jellyfish */
    @Override
    public int getPoints() { return 10; }
}


/**
 * Squid — medium-difficulty enemy.
 *
 * 1 HP, 20 points. Drawn as an orange body with side fins and animated
 * tentacle fringe at the bottom.
 */
class Squid extends SeaCreature {

    /**
     * Creates a Squid at the given position.
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed movement speed
     */
    public Squid(double x, double y, double speed) {
        super(x, y, 1, speed);
    }

    /**
     * Draws the squid with a tapered body, side fins, and waving tentacles.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Main body
        gc.setFill(Color.rgb(240, 110, 70));
        double[] bx = {x, x - 11, x - 8, x + 8, x + 11};
        double[] by = {y - 14, y + 6, y + 10, y + 10, y + 6};
        gc.fillPolygon(bx, by, 5);

        // Mantle highlight
        gc.setFill(Color.rgb(255, 140, 100, 0.5));
        gc.fillOval(x - 6, y - 6, 12, 10);

        // Side fins
        gc.setFill(Color.rgb(220, 90, 60, 0.7));
        gc.fillPolygon(
            new double[]{x - 11, x - 18, x - 9},
            new double[]{y, y + 2, y + 6}, 3
        );
        gc.fillPolygon(
            new double[]{x + 11, x + 18, x + 9},
            new double[]{y, y + 2, y + 6}, 3
        );

        // Animated tentacle fringe
        gc.setStroke(Color.rgb(240, 120, 80, 0.6));
        gc.setLineWidth(1.5);
        for (int i = -3; i <= 3; i++) {
            double tx = x + i * 3;
            double wave = Math.sin(frameCount * 0.1 + i) * 2;
            gc.strokeLine(tx, y + 10, tx + wave, y + 20 + Math.abs(i));
        }

        // Eyes
        gc.setFill(Color.WHITE);
        gc.fillOval(x - 7, y - 7, 7, 7);
        gc.fillOval(x + 0, y - 7, 7, 7);
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 5, y - 5, 3, 4);
        gc.fillOval(x + 2, y - 5, 3, 4);
    }

    /** @return 20 — the point value for destroying a squid */
    @Override
    public int getPoints() { return 20; }
}


/**
 * Anglerfish — the toughest regular enemy.
 *
 * 2 HP, 30 points, shoots more frequently than other enemies. Drawn with a
 * deep-sea body, animated lure, red eyes, and a health bar.
 */
class Anglerfish extends SeaCreature {

    /** Shared random generator reused from the superclass level — no new instance needed. */
    private static final Random rng = new Random();

    /**
     * Creates an Anglerfish at the given position.
     * Speed is reduced slightly (×0.8) compared to other enemies to compensate
     * for its higher health and more aggressive shooting.
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed base movement speed (will be scaled down)
     */
    public Anglerfish(double x, double y, double speed) {
        super(x, y, 2, speed * 0.8);
    }

    /**
     * Draws the anglerfish with animated lure glow and full detail.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Body
        gc.setFill(Color.rgb(50, 35, 70));
        gc.fillOval(x - 15, y - 11, 30, 22);

        // Tail
        gc.setFill(Color.rgb(60, 40, 80));
        gc.fillPolygon(
            new double[]{x - 14, x - 22, x - 20, x - 14},
            new double[]{y - 4, y - 8, y + 8, y + 4}, 4
        );

        // Dorsal fin
        gc.setFill(Color.rgb(70, 45, 90));
        gc.fillPolygon(
            new double[]{x - 5, x, x + 5},
            new double[]{y - 10, y - 17, y - 10}, 3
        );

        // Jaw
        gc.setFill(Color.rgb(40, 25, 50));
        gc.fillOval(x + 2, y + 2, 16, 10);

        // Teeth
        gc.setStroke(Color.rgb(220, 220, 200));
        gc.setLineWidth(1);
        double mx = x + 3;
        for (int i = 0; i < 5; i++) {
            double ty = (i % 2 == 0) ? y + 3 : y + 8;
            gc.strokeLine(mx + i * 3, y + 5, mx + (i + 1) * 3, ty);
        }

        // Animated lure stalk
        gc.setStroke(Color.rgb(80, 60, 100));
        gc.setLineWidth(1);
        double lureSwing = Math.sin(frameCount * 0.06) * 4;
        gc.strokeLine(x, y - 11, x + lureSwing, y - 24);

        // Lure glow (pulses in intensity)
        double glow = 0.4 + Math.sin(frameCount * 0.12) * 0.35;
        gc.setFill(Color.rgb(255, 255, 100, glow));
        gc.fillOval(x + lureSwing - 4, y - 28, 8, 8);
        gc.setFill(Color.rgb(255, 255, 100, glow * 0.3));
        gc.fillOval(x + lureSwing - 8, y - 32, 16, 16);

        // Eyes
        gc.setFill(Color.rgb(255, 40, 40));
        gc.fillOval(x + 5, y - 5, 5, 5);
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 6, y - 4, 3, 3);

        drawHealthBar(gc, 14);
    }

    /** @return 30 — the point value for destroying an anglerfish */
    @Override
    public int getPoints() { return 30; }

    /**
     * Anglerfish shoot roughly twice as often as regular enemies.
     * Uses the class-level {@code rng} field rather than creating a
     * new {@code Random} instance each call.
     *
     * @return {@code true} if the anglerfish fires this frame
     */
    @Override
    public boolean canShoot() {
        return alive && rng.nextInt(600) < 2;
    }
}


/**
 * Kraken — the wave boss.
 *
 * Appears every 5 waves. Has high hit points (scaled with wave number),
 * moves independently across the screen (not in the main formation), and
 * shoots very frequently. Drawn as a large tentacled creature with glowing
 * eyes.
 */
class Kraken extends SeaCreature {

    /** Internal movement direction, managed independently of the formation. */
    private int moveDir = 1;

    /** Shared random generator — one instance per class, not per call. */
    private static final Random rng = new Random();

    /**
     * Creates a Kraken boss at the given position.
     * Hit points scale with the wave number so later bosses are tougher.
     *
     * @param x     horizontal spawn position
     * @param y     vertical spawn position
     * @param speed base speed (scaled down for the boss)
     * @param wave  current wave number for scaling hit points
     */
    public Kraken(double x, double y, double speed, int wave) {
        super(x, y, 18 + wave * 3, speed * 0.4);
    }

    /**
     * Moves the Kraken independently, bouncing between the screen edges.
     * Overrides the default formation-movement logic from the superclass.
     */
    @Override
    public void update() {
        x += speed * moveDir;
        if (x > Game.WIDTH - 50) moveDir = -1;
        if (x < 50) moveDir = 1;
        frameCount++;
    }

    /**
     * The Kraken does not participate in the grid formation's edge-reversal.
     * This override intentionally does nothing.
     */
    @Override
    public void reverseDirection() { }

    /**
     * The Kraken does not step down with the formation.
     * This override intentionally does nothing.
     */
    @Override
    public void stepDown() { }

    /**
     * The Kraken fires far more frequently than regular enemies — roughly
     * once per 60 frames (~1 shot per second at 60 fps).
     * Uses the class-level {@code rng} field rather than creating a new
     * {@code Random} instance per call.
     *
     * @return {@code true} if the Kraken fires this frame
     */
    @Override
    public boolean canShoot() {
        return alive && rng.nextInt(60) < 1;
    }

    /**
     * Draws the Kraken as a large tentacled creature with animated arms,
     * glowing eyes, and a health bar.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Animated tentacle arms
        gc.setStroke(Color.rgb(130, 40, 70));
        gc.setLineWidth(4);
        for (int i = 0; i < 8; i++) {
            double angle = (360.0 / 8) * i;
            double rad = Math.toRadians(angle);
            double bx = x + Math.cos(rad) * 30;
            double by = y + Math.sin(rad) * 22;
            double wave = Math.sin(frameCount * 0.06 + i) * 10;
            double ex = x + Math.cos(rad) * (55 + wave);
            double ey = y + Math.sin(rad) * (42 + wave);
            gc.strokeLine(bx, by, ex, ey);

            // Suction cup dots along tentacle
            gc.setFill(Color.rgb(160, 60, 90, 0.5));
            double midX = (bx + ex) / 2;
            double midY = (by + ey) / 2;
            gc.fillOval(midX - 2, midY - 2, 4, 4);
        }

        // Central body
        gc.setFill(Color.rgb(110, 30, 60));
        gc.fillOval(x - 35, y - 26, 70, 52);

        // Body texture markings
        gc.setFill(Color.rgb(90, 20, 50));
        gc.fillOval(x - 15, y - 10, 12, 10);
        gc.fillOval(x + 8, y + 2, 10, 8);
        gc.fillOval(x - 5, y + 8, 8, 6);

        // Head spike
        gc.setFill(Color.rgb(130, 40, 70));
        gc.fillPolygon(
            new double[]{x - 15, x, x + 15},
            new double[]{y - 24, y - 36, y - 24}, 3
        );

        // Eyes
        gc.setFill(Color.rgb(255, 255, 200));
        gc.fillOval(x - 16, y - 12, 14, 14);
        gc.fillOval(x + 2, y - 12, 14, 14);
        gc.setFill(Color.rgb(200, 50, 50));
        gc.fillOval(x - 12, y - 8, 8, 8);
        gc.fillOval(x + 6, y - 8, 8, 8);
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 10, y - 6, 4, 5);
        gc.fillOval(x + 8, y - 6, 4, 5);

        // Mouth
        gc.setStroke(Color.rgb(60, 15, 30));
        gc.setLineWidth(2);
        gc.strokeLine(x - 10, y + 12, x + 10, y + 12);
        gc.strokeLine(x - 8, y + 12, x - 12, y + 16);
        gc.strokeLine(x + 8, y + 12, x + 12, y + 16);

        drawHealthBar(gc, 28);
    }

    /** @return 100 — the point value for defeating the Kraken boss */
    @Override
    public int getPoints() { return 100; }
}