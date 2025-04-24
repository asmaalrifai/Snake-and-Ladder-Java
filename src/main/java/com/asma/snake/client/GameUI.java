package com.asma.snake.client;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class GameUI {

    private final GameManager gameManager;
    private final Label statusLabel = new Label();
    private final Label diceResult = new Label();
    private final Label gameTimerLabel = new Label("⏱ 30:00"); // global timer
    private final Label turnTimerLabel1 = new Label("⏳ 5s");
    private final Label turnTimerLabel2 = new Label("⏳ 5s");

    private final ImageView player1Token = new ImageView();
    private final ImageView player2Token = new ImageView();
    private final Pane boardLayer = new Pane();
    private final Label player1PosLabel = new Label();
    private final Label player2PosLabel = new Label();
    private final ImageView diceImageView1 = new ImageView();
    private final ImageView diceImageView2 = new ImageView();

    private AudioClip moveSound;
    private AudioClip diceSound;
    private final Random random = new Random();
    private boolean isRolling = false;

    private int previousPlayer1Pos = 1;
    private int previousPlayer2Pos = 1;

    private Timeline gameTimer;
    private Timeline turnCountdown;
    private int remainingSeconds = 30 * 60;

    private static final String RED_DICE_PATH = "dice/red/dice";
    private static final String BLUE_DICE_PATH = "dice/blue/dice";

    public GameUI(Stage stage) {
        Player player1 = new Player("Player 1", "red");
        Player player2 = new Player("Player 2", "blue");
        gameManager = new GameManager(player1, player2);

        // Load dice images
        diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
        diceImageView1.setFitHeight(60);
        diceImageView1.setFitWidth(60);
        diceImageView2.setFitHeight(60);
        diceImageView2.setFitWidth(60);

        // Load sounds
        moveSound = new AudioClip(getClass().getResource("/sounds/move.wav").toExternalForm());
        diceSound = new AudioClip(getClass().getResource("/sounds/Dice.wav").toExternalForm());

        // Load board and tokens
        Image boardImage = new Image("board.png");
        ImageView boardView = new ImageView(boardImage);
        boardView.setFitWidth(600);
        boardView.setFitHeight(600);
        player1Token.setImage(new Image("tokens/Player_Red.png"));
        player2Token.setImage(new Image("tokens/Player_Blue.png"));
        player1Token.setFitWidth(40);
        player2Token.setFitWidth(40);
        player1Token.setFitHeight(40);
        player2Token.setFitHeight(40);
        boardLayer.getChildren().addAll(boardView, player1Token, player2Token);

        // Player labels
        Label player1Label = new Label("🔴 " + player1.getName());
        player1Label.setTextFill(Color.RED);
        player1PosLabel.setText("Position: 1");

        Label player2Label = new Label("🔵 " + player2.getName());
        player2Label.setTextFill(Color.BLUE);
        player2PosLabel.setText("Position: 1");

        // Roll buttons
        Button player1Roll = new Button("Roll Dice");
        Button player2Roll = new Button("Roll Dice");
        player1Roll.setDisable(false);
        player2Roll.setDisable(true);

        // Play again button
        Button playAgain = new Button("🔁 Play Again");
        playAgain.setOnAction(e -> {
            gameManager.resetGame();
            updateTokens();
            updateTurnText();
            resetGameClock();
            diceResult.setText("");
            statusLabel.setText("");
            diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
            diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
            player1Roll.setDisable(false);
            player2Roll.setDisable(true);
        });

        // Roll actions
        player1Roll.setOnAction(e -> handleRoll(player1, player1Roll, player2Roll));
        player2Roll.setOnAction(e -> handleRoll(player2, player2Roll, player1Roll));

        // Player UI blocks
        VBox player1Box = new VBox(5, player1Label, diceImageView1, turnTimerLabel1, player1PosLabel, player1Roll);
        VBox player2Box = new VBox(5, player2Label, diceImageView2, turnTimerLabel2, player2PosLabel, player2Roll);
        player1Box.setAlignment(Pos.CENTER_RIGHT);
        player2Box.setAlignment(Pos.CENTER_LEFT);

        // Main layout
        HBox controlRow = new HBox(200, player2Box, player1Box);
        controlRow.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, gameTimerLabel, boardLayer, diceResult, statusLabel, controlRow, playAgain);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 640, 780);
        scene.getStylesheets().add("style.css");

        stage.setTitle("Snake and Ladder");
        stage.setScene(scene);
        stage.show();

        updateTokens();
        updateTurnText();
        startGameClock();
        startTurnTimer(player1, player1Roll, player2Roll);
    }

    private void handleRoll(Player player, Button currentRoll, Button otherRoll) {
        if (isRolling) return;
        isRolling = true;
        currentRoll.setDisable(true);
        stopTurnTimer();

        String dicePath = player.getName().equals("Player 1") ? RED_DICE_PATH : BLUE_DICE_PATH;
        ImageView activeDice = player.getName().equals("Player 1") ? diceImageView1 : diceImageView2;

        if (diceSound != null) diceSound.play();

        Thread animationThread = new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    int tempRoll = random.nextInt(6) + 1;
                    String frame = "/" + dicePath + tempRoll + ".png";
                    Platform.runLater(() -> activeDice.setImage(new Image(getClass().getResource(frame).toExternalForm())));
                    Thread.sleep(50);
                }

                int roll = gameManager.rollDice();
                String finalFrame = "/" + dicePath + roll + ".png";

                Platform.runLater(() -> {
                    activeDice.setImage(new Image(getClass().getResource(finalFrame).toExternalForm()));
                    String emoji = player.getName().equals("Player 1") ? "🔴" : "🔵";
                    diceResult.setText(emoji + " " + player.getName() + " rolled: " + roll);

                    if (gameManager.getCurrentPlayer() != player) {
                        currentRoll.setDisable(false);
                        isRolling = false;
                        return;
                    }

                    int finalPos = gameManager.movePlayer(roll);
                    updateTokens();

                    if (gameManager.checkWin()) {
                        statusLabel.setText(emoji + " " + player.getName() + " wins! 🎉");
                        currentRoll.setDisable(true);
                        otherRoll.setDisable(true);
                    } else {
                        if (gameManager.shouldSwitchTurn(roll)) {
                            gameManager.switchTurn();
                            currentRoll.setDisable(true);
                            otherRoll.setDisable(false);
                            startTurnTimer(gameManager.getCurrentPlayer(), otherRoll, currentRoll);
                        } else {
                            currentRoll.setDisable(false);
                            startTurnTimer(gameManager.getCurrentPlayer(), currentRoll, otherRoll);
                        }
                    }

                    updateTurnText();
                    isRolling = false;
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    currentRoll.setDisable(false);
                    isRolling = false;
                });
            }
        });

        animationThread.start();
    }

    private void startGameClock() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            gameTimerLabel.setText("⏱ " + String.format("%02d:%02d", minutes, seconds));

            if (remainingSeconds <= 0) {
                gameTimer.stop();
                statusLabel.setText("⏰ Time's up! Game Over.");
                diceResult.setText("");
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void resetGameClock() {
        if (gameTimer != null) gameTimer.stop();
        remainingSeconds = 30 * 60;
        startGameClock();
    }

    private void startTurnTimer(Player player, Button currentRoll, Button otherRoll) {
        stopTurnTimer();
        final int[] countdown = {5};
        Label turnLabel = player.getName().equals("Player 1") ? turnTimerLabel1 : turnTimerLabel2;

        turnCountdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown[0]--;
            turnLabel.setText("⏳ " + countdown[0] + "s");
            if (countdown[0] <= 0) {
                turnCountdown.stop();
                currentRoll.setDisable(true);
                otherRoll.setDisable(false);
                gameManager.switchTurn();
                updateTurnText();
                startTurnTimer(gameManager.getCurrentPlayer(), otherRoll, currentRoll);
            }
        }));
        turnLabel.setText("⏳ 5s");
        turnCountdown.setCycleCount(5);
        turnCountdown.play();
    }

    private void stopTurnTimer() {
        if (turnCountdown != null) {
            turnCountdown.stop();
        }
    }

    private void updateTurnText() {
        String emoji = gameManager.getCurrentPlayer().getName().equals("Player 1") ? "🔴" : "🔵";
        statusLabel.setText("Turn: " + emoji + " " + gameManager.getCurrentPlayer().getName());
    }

    private void updateTokens() {
        int p1 = gameManager.getPlayer1().getPosition();
        int p2 = gameManager.getPlayer2().getPosition();

        animateTokenTo(player1Token, p1, true, p1 != previousPlayer1Pos);
        animateTokenTo(player2Token, p2, false, p2 != previousPlayer2Pos);

        previousPlayer1Pos = p1;
        previousPlayer2Pos = p2;

        player1PosLabel.setText("Position: " + p1);
        player2PosLabel.setText("Position: " + p2);
    }

    private void animateTokenTo(ImageView token, int position, boolean shiftLeft, boolean moved) {
        int cellSize = 60;
        int col = (position - 1) % 10;
        int row = (position - 1) / 10;
        if (row % 2 == 1) col = 9 - col;

        double centerX = col * cellSize + cellSize / 2.0;
        double centerY = (9 - row) * cellSize + cellSize / 2.0;

        double offset = 12;
        double x = shiftLeft ? centerX - offset : centerX + offset;
        double y = centerY;

        double targetX = x - token.getFitWidth() / 2;
        double targetY = y - token.getFitHeight() / 2;

        TranslateTransition transition = new TranslateTransition(Duration.millis(400), token);
        transition.setToX(targetX);
        transition.setToY(targetY);
        transition.play();

        if (moveSound != null && moved && position > 1) {
            moveSound.play();
        }
    }
}
