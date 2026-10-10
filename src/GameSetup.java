import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;

/**
 * Title-screen entry point for DEPTHS — Ocean Defence.
 *
 * This class extends {@link Application} and is the main launch class for
 * the game. It builds and animates a full-screen title scene using the
 * JavaFX Animation API before handing off to {@link Game} when the player
 * clicks Play.
 *
 * <p>Animations used (satisfying the Assignment Animation requirement):</p>
 * <ul>
 *   <li>{@link TranslateTransition} — bubbles rise from the ocean floor</li>
 *   <li>{@link FadeTransition} — bubbles pulse in opacity</li>
 *   <li>{@link ScaleTransition} — bubbles and the title text breathe in scale</li>
 *   <li>{@link TranslateTransition} — silhouette fish swim across the screen</li>
 *   <li>{@link TranslateTransition} — seaweed fronds sway side to side</li>
 * </ul>
 *
 * @author Fakhriyya Vazirova and Waice Lim
 * @version 2025.04
 */
public class GameSetup extends Application {

    /**
     * Application main entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Called by the JavaFX runtime after the application is initialised.
     * Delegates immediately to {@link #showTitleScreen(Stage)}.
     *
     * @param stage the primary window provided by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        showTitleScreen(stage);
    }

    /**
     * Builds the animated title screen and displays it on the given stage.
     *
     * The background is a dark ocean pane populated with:
     * rising/pulsing bubbles (TranslateTransition + FadeTransition + ScaleTransition),
     * silhouette fish swimming across (TranslateTransition), and
     * swaying seaweed dots at the bottom (TranslateTransition).
     *
     * The Play button transitions directly into {@link Game#start(Stage)},
     * reusing the same Stage to avoid creating a second window.
     *
     * @param stage the primary window to display the title screen on
     */
    private void showTitleScreen(Stage stage) {

        Pane bgPane = new Pane();
        bgPane.setStyle("-fx-background-color: #001025;");
        bgPane.setPrefSize(Game.WIDTH, Game.HEIGHT);

        Random random = new Random();

        
        for (int i = 0; i < 30; i++) {
            double size = 3 + random.nextDouble() * 8;
            Circle bubble = new Circle(
                random.nextInt(Game.WIDTH),
                Game.HEIGHT + random.nextInt(300),
                size
            );
            bubble.setFill(Color.rgb(120, 190, 240, 0.08 + random.nextDouble() * 0.12));
            bubble.setStroke(Color.rgb(150, 210, 250, 0.1));
            bubble.setStrokeWidth(0.5);

            // Rise upward
            TranslateTransition rise = new TranslateTransition(
                Duration.seconds(8 + random.nextDouble() * 12), bubble
            );
            rise.setFromY(0);
            rise.setToY(-(Game.HEIGHT + 400));
            rise.setCycleCount(Animation.INDEFINITE);
            rise.setInterpolator(Interpolator.LINEAR);
            rise.play();

            // Fade in and out
            FadeTransition fade = new FadeTransition(
                Duration.seconds(2 + random.nextDouble() * 4), bubble
            );
            fade.setFromValue(0.1);
            fade.setToValue(0.6);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

           
            ScaleTransition pulse = new ScaleTransition(
                Duration.seconds(3 + random.nextDouble() * 4), bubble
            );
            pulse.setFromX(0.8);
            pulse.setFromY(0.8);
            pulse.setToX(1.2);
            pulse.setToY(1.2);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();

            bgPane.getChildren().add(bubble);
        }

        // fish
        for (int i = 0; i < 4; i++) {
            Polygon fish = new Polygon(
                0, 0, -12, -5, -12, 5  
            );
            fish.setFill(Color.rgb(60, 100, 140, 0.15 + random.nextDouble() * 0.1));
            fish.setLayoutY(100 + random.nextInt(Game.HEIGHT - 200));

            TranslateTransition swim = new TranslateTransition(
                Duration.seconds(15 + random.nextDouble() * 20), fish
            );
            swim.setFromX(-30);
            swim.setToX(Game.WIDTH + 30);
            swim.setCycleCount(Animation.INDEFINITE);
            swim.setDelay(Duration.seconds(random.nextDouble() * 10));
            swim.setInterpolator(Interpolator.LINEAR);
            swim.play();

            bgPane.getChildren().add(fish);
        }

        // SEAWEED at bottom 
        for (int i = 0; i < 10; i++) {
            Circle seaweedDot = new Circle(
                40 + i * 75 + random.nextInt(20),
                Game.HEIGHT - 15 - random.nextInt(20),
                4 + random.nextDouble() * 5
            );
            seaweedDot.setFill(Color.rgb(20, 80, 40, 0.3));

            
            TranslateTransition sway = new TranslateTransition(
                Duration.seconds(3 + random.nextDouble() * 3), seaweedDot
            );
            sway.setFromX(-5);
            sway.setToX(5);
            sway.setCycleCount(Animation.INDEFINITE);
            sway.setAutoReverse(true);
            sway.setInterpolator(Interpolator.EASE_BOTH);
            sway.play();

            bgPane.getChildren().add(seaweedDot);
        }

        // ═══ TITLE ═══
        Label titleLabel = new Label(" DEPTHS");
        titleLabel.setStyle(
            "-fx-text-fill: #55ccff;" +
            "-fx-font-size: 56;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, #003366, 10, 0.5, 0, 2);"
        );

        // Title pulse animation — ScaleTransition
        ScaleTransition titlePulse = new ScaleTransition(Duration.seconds(2.5), titleLabel);
        titlePulse.setFromX(1.0);
        titlePulse.setFromY(1.0);
        titlePulse.setToX(1.06);
        titlePulse.setToY(1.06);
        titlePulse.setCycleCount(Animation.INDEFINITE);
        titlePulse.setAutoReverse(true);
        titlePulse.setInterpolator(Interpolator.EASE_BOTH);
        titlePulse.play();

        Label subtitleLabel = new Label("Ocean Defence");
        subtitleLabel.setStyle(
            "-fx-text-fill: #88bbdd;" +
            "-fx-font-size: 22;"
        );

        

        // ═══ BUTTONS ═══
        Button startBtn = new Button("\u25B6  Play!");
        startBtn.setStyle(
            "-fx-background-color: #006699;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 40;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        );
        startBtn.setOnAction(e -> {
            Game game = new Game();
            game.start(stage);
        });

        // Button hover effect via mouse events
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
            "-fx-background-color: #0088cc;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 40;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        ));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(
            "-fx-background-color: #006699;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 40;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        ));

        Button quitBtn = new Button("Surface (Quit)");
        quitBtn.setStyle(
            "-fx-background-color: #334455;" +
            "-fx-text-fill: #aabbcc;" +
            "-fx-font-size: 14;" +
            "-fx-padding: 8 25;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;"
        );
        quitBtn.setOnAction(e -> Platform.exit());

        // CONTROLS INFO
        Label controlsLabel = new Label(
            "\u2190 \u2192  Move    |    SPACE  Shoot    |    P  Pause"
        );
        controlsLabel.setStyle("-fx-text-fill: #556677; -fx-font-size: 12;");

        Label tipsLabel = new Label(
            "Collect power-ups  \u25B2 Spread Shot   \u25CF Depth Charge   \u25C6 Shield"
        );
        tipsLabel.setStyle("-fx-text-fill: #445566; -fx-font-size: 11;");

        // LAYOUT 
        VBox centerBox = new VBox(12);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Label(""),
            startBtn,
            quitBtn,
            new Label(""),
            controlsLabel,
            tipsLabel
        );

        StackPane root = new StackPane();
        root.getChildren().addAll(bgPane, centerBox);

        Scene scene = new Scene(root, Game.WIDTH, Game.HEIGHT);
        stage.setTitle(" DEPTHS ");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}