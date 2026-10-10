import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Main game class for DEPTHS — Ocean Defence.
 *
 * Owns the central game loop (via {@link AnimationTimer}) and coordinates
 * all three per-frame tasks: input processing, state update, and rendering.
 * Every game object draws itself via its own {@code draw(GraphicsContext)}
 * method, demonstrating polymorphism across {@link SeaCreature},
 * {@link Torpedo}, {@link PowerUp}, and {@link Coral}.
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public class Game extends Application {

    public static final int WIDTH = 700;
    public static final int HEIGHT = 600;

   
    private Submarine sub;
    private ArrayList<Torpedo> torpedoes = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();

    private boolean left, right, shooting;

    private int score = 0;
    private int lives = 3;

    private Label scoreLabel;
    private Label livesLabel;

    private int shootCooldown = 0;

   //enemy system
    private ArrayList<SeaCreature> creatures = new ArrayList<>();
    private ArrayList<Torpedo> enemyShots = new ArrayList<>();  
    private ArrayList<Coral> corals = new ArrayList<>();        

    //  Wave system — game gets harder each wave
    private int wave = 0;
    private Label waveLabel;

    //  Track high score across restarts
    private int highScore = 0;

    //Pause and game over states so we can freeze/restart
    private boolean gameOver = false;
    private boolean paused = false;

    //  Combo system — kill enemies quickly for bonus points
    private int comboCount = 0;
    private int comboTimer = 0;
    private String comboText = "";
    private int comboTextTimer = 0;
    private Label comboLabel;

    
    private String waveText = "";
    private int waveTextTimer = 0;

    
    private ArrayList<double[]> particles = new ArrayList<>();

  
    private ArrayList<double[]> bgBubbles = new ArrayList<>();

    
    private double waveOffset = 0;

    
    private Random random = new Random();

   
    private long frameCount = 0;


    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        //MENU BAR
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle(                                       
            "-fx-background-color: #002244;" +
            "-fx-border-color: #004488;" +
            "-fx-border-width: 0 0 2 0;"
        );

        
        Menu fileMenu = new Menu("File");
        MenuItem restartItem = new MenuItem("New Dive");       
        restartItem.setOnAction(e -> restartGame());
        MenuItem quitItem = new MenuItem("Quit");         
        quitItem.setOnAction(e -> Platform.exit());              
        fileMenu.getItems().addAll(restartItem, new SeparatorMenuItem(), quitItem);

        //  Controls menu so player can pause from menu too
        Menu controlsMenu = new Menu("Controls");
        MenuItem pauseItem = new MenuItem("Pause/Resume (P)");
        pauseItem.setOnAction(e -> paused = !paused);
        controlsMenu.getItems().add(pauseItem);

        //  How to Play
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");             
        aboutItem.setOnAction(e -> {                          
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("DEPTHS — Ocean Defence");
            alert.setContentText(
                "Defend the ocean depths from waves of sea creatures!\n\n" +
                "Controls:\n" +
                "  LEFT/RIGHT or A/D — Move submarine\n" +
                "  SPACE — Fire torpedo\n" +
                "  P — Pause\n" +
                "  R — Restart (when game over)\n\n" +
                "Power-ups:\n" +
                "  Green = Spread Shot\n" +
                "  Red = Depth Charge\n" +
                "  Blue = Shield\n\n" +
                "Created by: Fakhriyya Vazirova and Waice Lim"
            );
            alert.showAndWait();
        });
        MenuItem howToItem = new MenuItem("How to Play");       //extra help option
        howToItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("How to Play");
            alert.setHeaderText("Survive the Depths!");
            alert.setContentText(
                "Sea creatures descend toward you in waves.\n" +
                "Shoot them before they reach you!\n\n" +
                "Coral reefs block shots from both sides.\n" +
                "Boss Kraken appears every 5 waves.\n" +
                "Kill enemies quickly for combo bonuses!\n" +
                "Each wave gets harder and the ocean gets darker..."
            );
            alert.showAndWait();
        });
        helpMenu.getItems().addAll(howToItem, new SeparatorMenuItem(), aboutItem);

        menuBar.getMenus().addAll(fileMenu, controlsMenu, helpMenu);

        // ═══ UI LABELS ═══
        
        scoreLabel = new Label("Score: 0");                     
        livesLabel = new Label("Lives: 3");                      
        waveLabel = new Label("Wave: 0");                        
        comboLabel = new Label("");                               
        
        scoreLabel.setStyle("-fx-text-fill: #ffdd44; -fx-font-size: 14; -fx-font-weight: bold;");
        livesLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 14; -fx-font-weight: bold;");
        waveLabel.setStyle("-fx-text-fill: #66ccff; -fx-font-size: 14; -fx-font-weight: bold;");
        comboLabel.setStyle("-fx-text-fill: #ff9900; -fx-font-size: 14; -fx-font-weight: bold;");

        
        HBox infoBar = new HBox(25, scoreLabel, livesLabel, waveLabel, comboLabel);
        infoBar.setStyle("-fx-background-color: #001a33; -fx-padding: 8 15;");

        
        VBox topBox = new VBox(menuBar, infoBar);
        root.setTop(topBox);

        
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.setCenter(canvas);

        Scene scene = new Scene(root);

        
        scene.setOnKeyPressed(this::keyPressed);
        scene.setOnKeyReleased(this::keyReleased);

        
        canvas.setOnMousePressed(e -> {
            if (!gameOver && !paused) shooting = true;
        });
        canvas.setOnMouseReleased(e -> shooting = false);

        
        sub = new Submarine(WIDTH / 2, HEIGHT - 50);

        
        for (int i = 0; i < 20; i++) {
            bgBubbles.add(new double[]{
                random.nextInt(WIDTH),       
                random.nextInt(HEIGHT),     
                0.2 + random.nextDouble() * 0.6,  
                2 + random.nextDouble() * 4,       
                random.nextDouble()                 
            });
        }

        startWave(1);

     
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frameCount++;                               
                if (!gameOver && !paused) {                
                    update();
                    checkCollisions();
                }
                draw(gc);
            }
        };
        timer.start();

        stage.setTitle("DEPTHS — Ocean Defence");
        stage.setScene(scene);
        stage.setResizable(false);                          
        stage.show();
    }


    /**
     * Advances all game objects by one frame.
     * Handles submarine movement, torpedo movement and removal, enemy
     * formation movement and shooting, power-up falling, particle decay,
     * background animations, combo/wave timers, and wave completion detection.
     * Not called while the game is paused or in the game-over state.
     */
    private void update() {

        
        if (left) sub.moveLeft();
        if (right) sub.moveRight();
        sub.update();

        
        if (shootCooldown > 0) shootCooldown--;
        if (shooting && shootCooldown == 0) {
            shoot();
            shootCooldown = 12;                                  
        }

        
        for (Torpedo t : torpedoes) {
            t.update();
        }
        torpedoes.removeIf(t -> !t.onScreen());

        
        for (Torpedo t : enemyShots) {
            t.update();
        }
        enemyShots.removeIf(t -> !t.onScreen());

       
        for (PowerUp p : powerUps) {
            p.update();
        }
        powerUps.removeIf(p -> !p.isOnScreen());

        // enemy formation movement
        
        boolean hitEdge = false;
        for (SeaCreature c : creatures) {
            if (c.isAlive()) {
                double[] pos = c.getPos();
                if (pos[0] <= 15 || pos[0] >= WIDTH - 15) {
                    hitEdge = true;
                    break;
                }
            }
        }
        if (hitEdge) {
            for (SeaCreature c : creatures) {
                c.reverseDirection();
                c.stepDown();
            }
        }

        for (SeaCreature c : creatures) {
            if (c.isAlive()) {
                c.update();
                if (c.canShoot()) {
                    double[] pos = c.getPos();
                    
                    enemyShots.add(new Torpedo(pos[0], pos[1] + 10, false));
                }
            }
        }

        
        updateParticles();
        updateBgBubbles();

        
        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer <= 0) {
                comboCount = 0;
                comboLabel.setText("");
            }
        }
        if (comboTextTimer > 0) comboTextTimer--;
        if (waveTextTimer > 0) waveTextTimer--;

        
        waveOffset += 0.03;

        
        boolean allDead = true;
        for (SeaCreature c : creatures) {
            if (c.isAlive()) { allDead = false; break; }
        }
        if (allDead && creatures.size() > 0) {
            startWave(wave + 1);
        }
    }


    /**
     * Fires one or three torpedoes from the submarine's current position.
     * When spread-shot is active, three torpedoes are fired simultaneously
     * (left, centre, right) for wider coverage.
     */
    private void shoot() {
        if (sub.isSpreadShot()) {
         
            torpedoes.add(new Torpedo(sub.getX(), sub.getY() - 10, true));
            torpedoes.add(new Torpedo(sub.getX() - 10, sub.getY() - 10, true));
            torpedoes.add(new Torpedo(sub.getX() + 10, sub.getY() - 10, true));
        } else {
            torpedoes.add(new Torpedo(sub.getX(), sub.getY() - 10, true));
        }
    }


    /**
     * Performs all collision detection for the current frame:
     * <ul>
     *   <li>Friendly torpedoes vs coral barriers (blocks and damages coral)</li>
     *   <li>Friendly torpedoes vs sea creatures (damages/kills enemies, scores points)</li>
     *   <li>Enemy shots vs coral barriers</li>
     *   <li>Enemy shots vs submarine (loses a life, triggers game over)</li>
     *   <li>Sea creatures reaching the submarine's row (loses a life)</li>
     *   <li>Power-ups vs submarine (activates effect via polymorphic activate() call)</li>
     *   <li>Depth-charge detonation if {@link Submarine#isDepthChargePending()}</li>
     * </ul>
     */
    private void checkCollisions() {

        
        Iterator<Torpedo> tIt = torpedoes.iterator();
        while (tIt.hasNext()) {
            Torpedo t = tIt.next();
            if (!t.isFriendly()) continue;  // skip enemy shots

            // ADDED: check if torpedo hits a coral barrier first
            boolean blocked = false;
            for (Coral coral : corals) {
                if (!coral.isDestroyed() && coral.containsPoint(t.getX(), t.getY())) {
                    coral.takeDamage(1);
                    spawnParticles(t.getX(), t.getY(), Color.ORANGE, 3);
                    tIt.remove();
                    blocked = true;
                    break;
                }
            }
            if (blocked) continue;

            // Check if torpedo hits any creature
            for (SeaCreature c : creatures) {
                if (!c.isAlive()) continue;
                double[] pos = c.getPos();
                if (t.inContact(pos[0], pos[1], 16)) {
                    c.takeDamage(t.getDamage());
                    spawnParticles(pos[0], pos[1], Color.rgb(100, 200, 255), 6);

                    if (!c.isAlive()) {
                        int pts = c.getPoints();    // polymorphism: each type returns different points

                        // Combo system — kill quickly for multiplier
                        comboCount++;
                        comboTimer = 90;  // 1.5 seconds to keep combo
                        int multiplier = Math.min(comboCount, 5);
                        int totalPts = pts * multiplier;
                        score += totalPts;

                        if (comboCount >= 2) {
                            comboText = "x" + multiplier + " COMBO! +" + totalPts;
                            comboTextTimer = 60;
                            comboLabel.setText(comboText);
                        }

                        scoreLabel.setText("Score: " + score);

                        // 15% chance to drop a power-up
                        if (random.nextInt(100) < 15) {
                            spawnPowerUp(pos[0], pos[1]);
                        }

                        // Particle burst on kill
                        spawnParticles(pos[0], pos[1], Color.WHITE, 10);
                    }

                    tIt.remove();
                    break;
                }
            }
        }

        
        Iterator<Torpedo> eIt = enemyShots.iterator();
        while (eIt.hasNext()) {
            Torpedo t = eIt.next();

            // Check coral blocking for enemy shots too
            boolean blocked = false;
            for (Coral coral : corals) {
                if (!coral.isDestroyed() && coral.containsPoint(t.getX(), t.getY())) {
                    coral.takeDamage(1);
                    spawnParticles(t.getX(), t.getY(), Color.ORANGE, 3);
                    eIt.remove();
                    blocked = true;
                    break;
                }
            }
            if (blocked) continue;

            
            if (t.inContact(sub.getX(), sub.getY(), sub.getRadius())) {
                eIt.remove();
                if (sub.hit()) {  
                    lives--;
                    livesLabel.setText("Lives: " + lives);
                    spawnParticles(sub.getX(), sub.getY(), Color.RED, 15);
                    if (lives <= 0) {
                        gameOver = true;
                        if (score > highScore) highScore = score;
                    }
                }
            }
        }

        
        for (SeaCreature c : creatures) {
            if (c.isAlive() && c.getPos()[1] >= HEIGHT - 65) {
                lives--;
                livesLabel.setText("Lives: " + lives);
                c.takeDamage(999);   // kill it immediately
                spawnParticles(c.getPos()[0], c.getPos()[1], Color.RED, 8);
                if (lives <= 0) {
                    gameOver = true;
                    if (score > highScore) highScore = score;
                }
            }
        }

        
        powerUps.removeIf(p -> {
            if (p.inContact(sub.getX(), sub.getY())) {
                // activate() applies the effect to the submarine (polymorphic call).
                // For DepthCharge, activate() arms sub.depthChargePending.
                // The pending flag is checked AFTER this block so every power-up
                // type is handled uniformly here.
                p.activate(sub);

                // Particle burst on collection
                spawnParticles(p.getX(), p.getY(), Color.YELLOW, 12);
                return true;
            }
            return false;
        });

        // Depth-charge detonation: DepthCharge.activate() arms this flag on the
        // submarine rather than directly damaging enemies (which would require
        // the PowerUp to hold a reference to the enemy list). The game loop
        // owns the enemy list and handles the effect here, then clears the flag.
        if (sub.isDepthChargePending()) {
            sub.consumeDepthCharge();
            score += 50;
            for (SeaCreature c : creatures) {
                if (c.isAlive()) {
                    c.takeDamage(1);
                    if (!c.isAlive()) {
                        score += c.getPoints();
                        spawnParticles(c.getPos()[0], c.getPos()[1], Color.rgb(255, 100, 100), 6);
                    }
                }
            }
            scoreLabel.setText("Score: " + score);
        }

        //  clean up destroyed corals
        corals.removeIf(c -> c.isDestroyed());
    }


    /**
     * Renders the complete game scene to the canvas each frame.
     * Drawing order (back to front):
     * ocean gradient background → animated surface waves → background bubbles
     * → ocean floor with seaweed → coral barriers → sea creatures
     * → torpedoes → power-ups → particles → submarine → HUD overlays
     * (combo text, wave announcement, pause screen, game-over screen).
     *
     * @param gc the GraphicsContext of the main canvas
     */
    private void draw(GraphicsContext gc) {

        
        int depth = Math.min(wave, 15);
        for (int i = 0; i < HEIGHT; i += 4) {
            double ratio = (double) i / HEIGHT;
            int g = (int)(Math.max(5, 40 - depth * 2) * (1 - ratio * 0.6));
            int b = (int)(Math.max(20, 100 - depth * 4) * (1 - ratio * 0.3));
            gc.setFill(Color.rgb(0, Math.max(0, g), Math.max(0, b)));
            gc.fillRect(0, i, WIDTH, 4);
        }

        
        gc.setStroke(Color.rgb(60, 140, 200, 0.3));
        gc.setLineWidth(2);
        for (int i = 0; i < 3; i++) {
            double yy = 8 + i * 6;
            for (int x = 0; x < WIDTH; x += 5) {
                double y1 = yy + Math.sin((x + frameCount * 2) * 0.02 + i) * 3;
                double y2 = yy + Math.sin(((x + 5) + frameCount * 2) * 0.02 + i) * 3;
                gc.strokeLine(x, y1, x + 5, y2);
            }
        }

        // background bubbles
        for (double[] bb : bgBubbles) {
            double opacity = 0.08 + Math.sin(frameCount * 0.02 + bb[4] * 10) * 0.04;
            gc.setFill(Color.rgb(150, 210, 255, Math.max(0.02, opacity)));
            gc.fillOval(bb[0], bb[1], bb[3], bb[3]);
            gc.setStroke(Color.rgb(180, 220, 255, Math.max(0.02, opacity * 0.8)));
            gc.setLineWidth(0.5);
            gc.strokeOval(bb[0], bb[1], bb[3], bb[3]);
        }

        // ocean floor with animated seaweed
        gc.setFill(Color.rgb(20, 40, 30));
        for (int x = 0; x < WIDTH; x += 3) {
            double floorY = HEIGHT - 12 + Math.sin(x * 0.05 + waveOffset) * 4;
            gc.fillRect(x, floorY, 3, HEIGHT - floorY);
        }
        gc.setStroke(Color.rgb(30, 100, 50));
        gc.setLineWidth(2);
        for (int i = 0; i < 10; i++) {
            double sx = 30 + i * 72;
            double sway = Math.sin(frameCount * 0.03 + i * 2) * 8;
            gc.strokeLine(sx, HEIGHT - 10, sx + sway, HEIGHT - 30 - (i % 3) * 5);
            gc.strokeLine(sx + sway, HEIGHT - 30 - (i % 3) * 5, sx + sway * 1.5, HEIGHT - 48);
        }

        // ADDED: draw coral barriers
        for (Coral coral : corals) {
            if (!coral.isDestroyed()) coral.draw(gc);
        }

        
        for (SeaCreature c : creatures) {
            if (c.isAlive()) c.draw(gc);
        }

       
        for (Torpedo t : torpedoes) {
            t.draw(gc);
        }
        for (Torpedo t : enemyShots) {             
            t.draw(gc);
        }

        
        for (PowerUp p : powerUps) {
            p.draw(gc);
        }

      
        drawParticles(gc);

        
        sub.draw(gc);

      
        if (comboTextTimer > 0 && comboCount >= 2) {
            gc.setFill(Color.rgb(255, 180, 0, (double) comboTextTimer / 60));
            gc.setFont(Font.font("Arial", 22));
            gc.fillText(comboText, WIDTH / 2 - 60, HEIGHT / 2);
        }

       
        if (waveTextTimer > 0) {
            double alpha = Math.min(1.0, (double) waveTextTimer / 30);
            gc.setFill(Color.rgb(100, 200, 255, alpha));
            gc.setFont(Font.font("Arial", 32));
            gc.fillText(waveText, WIDTH / 2 - 80, HEIGHT / 2 - 60);
        }

       
        if (paused && !gameOver) {
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
            gc.setFill(Color.rgb(100, 200, 255));
            gc.setFont(Font.font("Arial", 40));
            gc.fillText("PAUSED", WIDTH / 2 - 75, HEIGHT / 2);
            gc.setFont(Font.font("Arial", 16));
            gc.setFill(Color.rgb(150, 200, 230));
            gc.fillText("Press P to resume", WIDTH / 2 - 70, HEIGHT / 2 + 35);
        }

        
        if (gameOver) {
            gc.setFill(Color.rgb(0, 0, 20, 0.75));
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            gc.setFill(Color.rgb(255, 80, 80));
            gc.setFont(Font.font("Arial", 50));
            gc.fillText("GAME OVER", WIDTH / 2 - 145, HEIGHT / 2 - 40);

            gc.setFill(Color.rgb(200, 220, 240));
            gc.setFont(Font.font("Arial", 20));
            gc.fillText("Score: " + score + "   |   Wave: " + wave,
                         WIDTH / 2 - 110, HEIGHT / 2 + 10);
            gc.fillText("High Score: " + highScore, WIDTH / 2 - 70, HEIGHT / 2 + 40);

            gc.setFill(Color.rgb(100, 200, 255));
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("Press R to dive again", WIDTH / 2 - 80, HEIGHT / 2 + 80);
        }
    }


    
    /**
     * Starts a new wave by populating the enemy formation and resetting the
     * coral barriers. Enemy speed and formation width increase each wave.
     * Every fifth wave spawns a Kraken boss in addition to the regular grid.
     *
     * @param waveNum the wave number to start (1-based)
     */
    private void startWave(int waveNum) {
        wave = waveNum;
        waveLabel.setText("Wave: " + wave);
        creatures.clear();
        enemyShots.clear();

        waveText = "WAVE " + wave;
        waveTextTimer = 90;

        double baseSpeed = 0.8 + wave * 0.12;     //so it gets faster each wave
        int cols = Math.min(8, 5 + wave / 4);       // more columns as waves progress

        // Top row: Anglerfish (hardest, 2HP, 30pts)
        for (int i = 0; i < cols; i++) {
            creatures.add(SeaCreature.createAnglerfish(80 + i * 72, 55, baseSpeed));
        }
        // Middle rows: Squid (medium, 1HP, 20pts)
        for (int row = 0; row < 2; row++) {
            for (int i = 0; i < cols; i++) {
                creatures.add(SeaCreature.createSquid(80 + i * 72, 100 + row * 42, baseSpeed));
            }
        }
        // Bottom rows: Jellyfish (easiest, 1HP, 10pts)
        for (int row = 0; row < 2; row++) {
            for (int i = 0; i < cols; i++) {
                creatures.add(SeaCreature.createJellyfish(80 + i * 72, 184 + row * 42, baseSpeed));
            }
        }

 
        if (wave % 5 == 0) {
            creatures.add(SeaCreature.createKraken(WIDTH / 2, 45, baseSpeed, wave));
        }

       
        corals.clear();
        corals.add(new Coral(100, HEIGHT - 155));
        corals.add(new Coral(250, HEIGHT - 155));
        corals.add(new Coral(400, HEIGHT - 155));
        corals.add(new Coral(550, HEIGHT - 155));
    }


    /**
     * Spawns a random power-up at the given position.
     * Each of the three types (Spread Shot, Depth Charge, Shield) has an
     * equal probability of appearing.
     *
     * @param x horizontal spawn position (typically where an enemy was killed)
     * @param y vertical spawn position
     */
    private void spawnPowerUp(double x, double y) {
        int type = random.nextInt(3);
        if (type == 0) powerUps.add(new SpreadShot(x, y));     
        else if (type == 1) powerUps.add(new DepthCharge(x, y));
        else powerUps.add(new Shield(x, y));                    
    }


    /**
     * Resets all game state to initial values and starts wave 1.
     * Called when the player presses R after game over, or selects
     * File → New Dive from the menu bar.
     */
    private void restartGame() {
        score = 0;
        lives = 3;
        wave = 0;
        gameOver = false;
        paused = false;
        comboCount = 0;
        scoreLabel.setText("Score: 0");
        livesLabel.setText("Lives: 3");
        comboLabel.setText("");
        sub = new Submarine(WIDTH / 2, HEIGHT - 50);
        torpedoes.clear();
        enemyShots.clear();
        powerUps.clear();
        particles.clear();
        startWave(1);
    }


    /**
     * Spawns a burst of particle effects at the given position.
     * Each particle stores position, velocity, size, remaining life, and
     * colour as a {@code double[]} for lightweight representation.
     * Particles fade out as their life counter decreases.
     *
     * @param x     burst origin x
     * @param y     burst origin y
     * @param color base colour of the particles
     * @param count number of particles to spawn
     */
    private void spawnParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 360;
            double speed = 1 + random.nextDouble() * 3;
            double dx = Math.cos(Math.toRadians(angle)) * speed;
            double dy = Math.sin(Math.toRadians(angle)) * speed;
            double size = 2 + random.nextDouble() * 3;
            double life = 20 + random.nextInt(25);
            particles.add(new double[]{
                x, y, dx, dy, size, life, life,
                color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255
            });
        }
    }

    private void updateParticles() {
        Iterator<double[]> it = particles.iterator();
        while (it.hasNext()) {
            double[] p = it.next();
            p[0] += p[2];   
            p[1] += p[3];   
            p[5] -= 1;    
            if (p[5] <= 0) it.remove();
        }
    }

    private void drawParticles(GraphicsContext gc) {
        for (double[] p : particles) {
            double alpha = p[5] / p[6];   // remaining life / max life = fading opacity
            gc.setFill(Color.rgb((int) p[7], (int) p[8], (int) p[9], alpha));
            double size = p[4] * alpha;
            gc.fillOval(p[0] - size / 2, p[1] - size / 2, size, size);
        }
    }

    // ADDED: background bubbles rise and wobble for atmosphere
    private void updateBgBubbles() {
        for (double[] b : bgBubbles) {
            b[1] -= b[2];                                              // rise upward
            b[0] += Math.sin(frameCount * 0.02 + b[4] * 10) * 0.3;   // side wobble
            if (b[1] < -10) {                                          // reset when off top
                b[0] = random.nextInt(WIDTH);
                b[1] = HEIGHT + 10;
                b[3] = 2 + random.nextDouble() * 4;
            }
        }
    }


    /**
     * Handles key-press events. Sets movement flags, toggles pause,
     * fires torpedoes, and restarts the game after game over.
     *
     * @param e the key event
     */
    private void keyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) left = true;           
        if (e.getCode() == KeyCode.RIGHT) right = true;         
        if (e.getCode() == KeyCode.SPACE) shooting = true;       
        if (e.getCode() == KeyCode.A) left = true;              
        if (e.getCode() == KeyCode.D) right = true;              
        if (e.getCode() == KeyCode.P) paused = !paused;         
        if (e.getCode() == KeyCode.R && gameOver) restartGame();  
    }

    /**
     * Handles key-release events. Clears movement and shooting flags.
     *
     * @param e the key event
     */
    private void keyReleased(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) left = false;          
        if (e.getCode() == KeyCode.RIGHT) right = false;         
        if (e.getCode() == KeyCode.SPACE) shooting = false;      
        if (e.getCode() == KeyCode.A) left = false;              
        if (e.getCode() == KeyCode.D) right = false;            
    }

    // Partner's original main method — unchanged
    public static void main(String[] args) {
        launch();
    }
}