import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Represents a destructible coral barrier in the DEPTHS game.
 *
 * Coral reefs are placed between the enemy formation and the submarine,
 * providing the player with temporary cover. Both friendly torpedoes and
 * enemy shots are blocked by intact coral, but each hit reduces the coral's
 * health. The visual appearance degrades as health decreases — healthy coral
 * is vibrant with many branches and polyp decorations, while damaged coral
 * loses branches and shows cracks.
 *
 * <p>The branch geometry is generated once in the constructor using a seeded
 * {@link Random} based on position, ensuring coral always looks the same on
 * each wave.</p>
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public class Coral {

    /** Horizontal position of the coral's base. */
    private double x;

    /** Vertical position of the coral's base. */
    private double y;

    /** Current hit points. */
    private int health;

    /** Maximum hit points (12). */
    private int maxHealth;

    /** Visual bounding width of the coral cluster. */
    private double width;

    /** Visual bounding height of the coral cluster. */
    private double height;

    /**
     * Pre-computed branch geometry: each branch is {@code {startX, startY, endX, endY}}
     * relative to the coral's base position. Generated once from a seeded random
     * so the layout is deterministic per (x, y) position.
     */
    private double[][] branches;

    /**
     * Creates a coral barrier at the given position with 12 hit points
     * and 12 randomly generated branches.
     *
     * @param x horizontal base position
     * @param y vertical base position
     */
    public Coral(double x, double y) {
        this.x = x;
        this.y = y;
        this.health = 12;
        this.maxHealth = 12;
        this.width = 60;
        this.height = 45;

        // Seeded by position so coral always looks the same at the same spot
        Random r = new Random((long)(x * 100 + y));
        branches = new double[12][4];
        for (int i = 0; i < 12; i++) {
            double bx = r.nextDouble() * width - 5;
            double by = r.nextDouble() * height * 0.7;
            double ex = bx + (r.nextDouble() - 0.5) * 15;
            double ey = by - 8 - r.nextDouble() * 15;
            branches[i] = new double[]{bx, by, ex, ey};
        }
    }

    /**
     * Draws the coral on the canvas.
     *
     * The visual representation changes with health:
     * <ul>
     *   <li>Above 70 % health: vibrant colours, polyp decorations, many branches</li>
     *   <li>40–70 % health: muted colours, crack lines visible, fewer branches</li>
     *   <li>Below 40 %: dark, mostly bare, only a few branches remain</li>
     * </ul>
     *
     * @param gc the GraphicsContext to draw on
     */
    public void draw(GraphicsContext gc) {
        double ratio = (double) health / maxHealth;

        // Number of branches shown scales with health ratio
        int branchCount = (int)(branches.length * ratio);

        // Base colour interpolates from brownish-grey (damaged) to vibrant red (healthy)
        int baseR = (int)(255 * ratio + 80 * (1 - ratio));
        int baseG = (int)(110 * ratio + 60 * (1 - ratio));
        int baseB = (int)(90 * ratio + 50 * (1 - ratio));

        // Rocky base mound
        gc.setFill(Color.rgb(
            Math.min(255, baseR - 40),
            Math.min(255, Math.max(0, baseG - 20)),
            Math.min(255, Math.max(0, baseB - 10))
        ));
        gc.fillOval(x - 5, y + 15, width + 10, 18);
        gc.fillOval(x + 5, y + 10, width - 10, 15);

        // Draw branches (fewer as health decreases)
        for (int i = 0; i < branchCount; i++) {
            double bx = x + branches[i][0];
            double by = y + branches[i][1];
            double ex = x + branches[i][2];
            double ey = y + branches[i][3];

            // Slight colour variation per branch for visual richness
            int cr = Math.min(255, baseR + (i % 3) * 15 - 20);
            int cg = Math.min(255, Math.max(0, baseG + (i % 2) * 10));
            int cb = Math.min(255, Math.max(0, baseB + (i % 4) * 8));

            // Branch stem
            gc.setStroke(Color.rgb(cr, cg, cb));
            gc.setLineWidth(3 + (i % 3));
            gc.strokeLine(bx, by + 15, ex, ey + 10);

            // Polyp tip
            gc.setFill(Color.rgb(
                Math.min(255, cr + 30),
                Math.min(255, cg + 20),
                Math.min(255, cb + 15)
            ));
            gc.fillOval(ex - 3, ey + 8, 6, 6);

            // Side buds on every third branch (healthy coral only)
            if (i % 3 == 0 && ratio > 0.5) {
                double mx = (bx + ex) / 2;
                double my = (by + ey) / 2 + 12;
                gc.setStroke(Color.rgb(cr, cg, cb, 0.7));
                gc.setLineWidth(2);
                gc.strokeLine(mx, my, mx + 8, my - 6);
                gc.strokeLine(mx, my, mx - 7, my - 5);
            }
        }

        // Polyp dots visible at high health (>70 %)
        if (ratio > 0.7) {
            gc.setFill(Color.rgb(255, 180, 200, 0.5));
            gc.fillOval(x + 10, y + 20, 5, 5);
            gc.fillOval(x + 35, y + 18, 4, 4);
            gc.fillOval(x + 22, y + 22, 4, 4);
        }

        // Crack lines visible at medium damage (40–70 %)
        if (ratio > 0.4 && ratio <= 0.7) {
            gc.setStroke(Color.rgb(40, 30, 20, 0.4));
            gc.setLineWidth(1);
            gc.strokeLine(x + 15, y + 18, x + 25, y + 25);
            gc.strokeLine(x + 30, y + 15, x + 35, y + 22);
        }
    }

    /**
     * Applies damage to this coral.
     *
     * @param d hit points to subtract
     * @return {@code true} if the coral has been fully destroyed
     */
    public boolean takeDamage(int d) {
        health -= d;
        return health <= 0;
    }

    /**
     * Returns whether this coral has been completely destroyed.
     *
     * @return {@code true} if health has reached zero
     */
    public boolean isDestroyed() { return health <= 0; }

    /**
     * Returns whether the given point is within this coral's hit region.
     * Used by the game loop to detect torpedo–coral collisions.
     *
     * @param px probe point x
     * @param py probe point y
     * @return {@code true} if (px, py) is inside the coral's bounding rectangle
     */
    public boolean containsPoint(double px, double py) {
        return px >= x - 8 && px <= x + width + 8 &&
               py >= y - 5 && py <= y + height + 5;
    }

    /** @return horizontal base position */
    public double getX() { return x; }

    /** @return vertical base position */
    public double getY() { return y; }
}