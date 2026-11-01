package com.example;

import java.util.HashSet;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * A single-player Pong game
 * 
 * Gameplay is updated through the AnimationTimer
 * which updates the game state (Main) by updating the 
 * board, which updates the player and the ball
 */
public class Main extends Application {

    private final int SCENE_WIDTH = 820;
    private final int SCENE_HEIGHT = 850;

    private final Set<KeyCode> keysDown = new HashSet<>();
    private final Board board = new Board();
    private final Canvas court = new Canvas();
    private final Label rallyScore = new Label("00");
    private final Label bestScore = new Label("00");
    private final Label stateLabel = new Label("READY TO PLAY");
    private final Label instructionLabel = new Label("PRESS START TO SERVE");
    private final Button playButton = new Button("START GAME");

    private GameState gameState = GameState.READY;
    private double serveCountdown;
    private long previousFrame;
    private int bestRally;

    @Override
    public void start(Stage stage) {
        Label eyebrow = new Label("Example JavaFX with Graphics");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("PONG");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Keep the rally alive. How many returns can you make?");
        subtitle.getStyleClass().add("subtitle");
        VBox heading = new VBox(7, eyebrow, title, subtitle);
        heading.getStyleClass().add("heading");

        HBox scoreboard = createScoreboard();
        instructionLabel.getStyleClass().add("instruction");

        Pane courtPane = new Pane(court);
        courtPane.setMinSize(0, 0);
                
        StackPane courtFrame = new StackPane(courtPane);

        courtFrame.getStyleClass().add("court-frame");
        court.widthProperty().bind(courtFrame.widthProperty());
        court.heightProperty().bind(courtFrame.heightProperty());
        courtFrame.setMinHeight(180);
        courtFrame.setPrefHeight(450);
        VBox.setVgrow(courtFrame, Priority.ALWAYS);

        Label controls = new Label("MOVE WITH W / S OR ↑ / ↓");
        controls.getStyleClass().add("control-hint");
        Label pauseHint = new Label("SPACE TO PAUSE");
        pauseHint.getStyleClass().add("control-hint");
        HBox controlHints = new HBox(30, controls, pauseHint);
        controlHints.setAlignment(Pos.CENTER);

        playButton.getStyleClass().add("play-button");
        playButton.setFocusTraversable(false);
        playButton.setOnAction(event -> handlePlayAction());

        Button resetButton = new Button("RESET");
        resetButton.getStyleClass().add("reset-button");
        resetButton.setFocusTraversable(false);
        resetButton.setOnAction(event -> resetGame());
        
        HBox actions = new HBox(12, playButton, resetButton);
        actions.setAlignment(Pos.CENTER);
        VBox footer = new VBox(18, controlHints, actions);
        footer.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        VBox content = new VBox(16, heading, scoreboard, instructionLabel, courtFrame, footer);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(24, 40, 24, 40));
        root.setCenter(content);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        scene.getStylesheets().add(getClass().getResource("/pong.css").toExternalForm());

        // keep track of keys which are pressed
        // Using keysDown ensures smooth movement of the paddle
        scene.setOnKeyPressed(event -> {
            boolean newlyPressed = keysDown.add(event.getCode());
            if (newlyPressed && (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER)) {
                handlePlayAction();
                event.consume();
            }          
        });
        // remove a key from memory if released
        scene.setOnKeyReleased(event -> keysDown.remove(event.getCode()));
 
        stage.setTitle("Example Pong Game");        
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        board.reset(court.getWidth(), court.getHeight());
    
    // Create and start a timer. The handle method is executed every frame
    // (roughly 60 times a second)
    new AnimationTimer() {
            // handle is timestamp in nanoseconds
            @Override
            public void handle(long now) {
                // convert to seconds, starting at 0
                double elapsed = previousFrame == 0 ? 0
                        : Math.min((now - previousFrame) / 1_000_000_000.0, 0.035);
                previousFrame = now;
                update(elapsed);
                render();
            }
        }.start();

        scene.getRoot().requestFocus();
    }

    private HBox createScoreboard() {
        VBox player = playerCard("YOUR RALLY", "MINT", rallyScore, true);
        VBox record = playerCard("BEST RALLY", "CORAL", bestScore, false);
        stateLabel.getStyleClass().add("game-state");
        Label mode = new Label("SINGLE PLAYER");
        mode.getStyleClass().add("versus");
        VBox center = new VBox(10, stateLabel, mode);
        center.setAlignment(Pos.CENTER);
        HBox.setHgrow(player, Priority.ALWAYS);
        HBox.setHgrow(record, Priority.ALWAYS);
        player.setMaxWidth(Double.MAX_VALUE);
        record.setMaxWidth(Double.MAX_VALUE);
        HBox scoreboard = new HBox(18, player, center, record);
        scoreboard.setAlignment(Pos.CENTER);
        return scoreboard;
    }

    private VBox playerCard(String name, String colorClass, Label score, boolean left) {
        Label playerName = new Label(name);
        playerName.getStyleClass().addAll("player-name", colorClass.toLowerCase());
        score.getStyleClass().addAll("score", colorClass.toLowerCase());
        VBox card = new VBox(3, playerName, score);
        card.getStyleClass().add("player-card");
        card.setAlignment(left ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return card;
    }

    /**
     * Updates the game state (called from the timer)
     * @param elapsed
     */
    private void update(double elapsed) {
        if (gameState != GameState.RUNNING || elapsed == 0) {
            return;
        }
        if (serveCountdown > 0) {
            serveCountdown = Math.max(0, serveCountdown - elapsed);
            return;
        }

        // update the board
        board.update(elapsed, court.getWidth(), court.getHeight(), keysDown);

        // get bestRally
        bestRally = Math.max(bestRally, board.getRally());

        // update the score labels
        updateScoreLabels();

        // handle game over
        if (board.isGameOver()) {
            gameState = GameState.GAME_OVER;
            stateLabel.setText("RALLY OVER");
            playButton.setText("TRY AGAIN");
            instructionLabel.setText("RALLY OVER — PRESS START TO TRY AGAIN");
        }
    }

    private void render() {
        double width = court.getWidth();
        double height = court.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        GraphicsContext graphics = court.getGraphicsContext2D();
        board.render(graphics, width, height);
    }

    private void handlePlayAction() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
            stateLabel.setText("GAME PAUSED");
            instructionLabel.setText("PAUSED — PRESS SPACE TO RESUME");
            playButton.setText("RESUME GAME");
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
            stateLabel.setText("RALLY IN PROGRESS");
            instructionLabel.setText("MOVE WITH W / S OR ↑ / ↓ TO RETURN THE BALL");
            playButton.setText("PAUSE GAME");
        } else {
            startGame();
        }
    }

    private void startGame() {
        board.reset(court.getWidth(), court.getHeight());
        serveCountdown = 0.65;
        gameState = GameState.RUNNING;
        stateLabel.setText("RALLY IN PROGRESS");
        instructionLabel.setText("MOVE WITH W / S OR ↑ / ↓ TO RETURN THE BALL");
        playButton.setText("PAUSE GAME");
        updateScoreLabels();
    }

    private void resetGame() {
        board.reset(court.getWidth(), court.getHeight());
        serveCountdown = 0;
        gameState = GameState.READY;
        stateLabel.setText("READY TO PLAY");
        instructionLabel.setText("PRESS START TO SERVE");
        playButton.setText("START GAME");
        updateScoreLabels();
    }

    private void updateScoreLabels() {
        rallyScore.setText(String.format("%02d", board.getRally()));
        bestScore.setText(String.format("%02d", bestRally));
    }

    private enum GameState {
        READY,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Exercises
     * 
     * 1. Change the radius of the ball to 30 (and then change it back!)
     * 2. Modify the code so that the wall moves toward the paddle (up to a point)
     * 3. Add a "cheat code" so that if the user types Shift-9 then the
     *      paddle covers the whole screen. Note: the KEY_PRESSED event
     *      has an isShiftDown method.
     */
}
