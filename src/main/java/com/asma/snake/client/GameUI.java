package com.asma.snake.client;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class GameUI {

    private final GameManager gameManager;
    private final Label statusLabel = new Label();
    private final Label diceResult = new Label();
    private final Label gameTimerLabel = new Label("⏱ 30:00");
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
    private AudioClip snakeSound;
    private AudioClip ladderSound;
    private AudioClip winSound;
    private final Random random = new Random();
    private boolean isRolling = false;

    private int previousPlayer1Pos = 1;
    private int previousPlayer2Pos = 1;

    private Timeline gameTimer;
    private Timeline turnCountdown;
    private int remainingSeconds = 30 * 60;

    private static final String RED_DICE_PATH = "dice/red/dice";
    private static final String BLUE_DICE_PATH = "dice/blue/dice";

    private ParallelTransition celebrationAnimation;

    private Circle timerCircle1;
    private Circle timerCircle2;

    public GameUI(Stage stage) {
        Player player1 = new Player("Player 1", "red");
        Player player2 = new Player("Player 2", "blue");
        gameManager = new GameManager(player1, player2);

        // Load dice images
        diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
        diceImageView1.setFitHeight(70);
        diceImageView1.setFitWidth(70);
        diceImageView2.setFitHeight(70);
        diceImageView2.setFitWidth(70);
        diceImageView1.getStyleClass().add("dice-image");
        diceImageView2.getStyleClass().add("dice-image");

        // Load sounds
        moveSound = new AudioClip(getClass().getResource("/sounds/move.wav").toExternalForm());
        diceSound = new AudioClip(getClass().getResource("/sounds/Dice.wav").toExternalForm());
        snakeSound = new AudioClip(getClass().getResource("/sounds/snake.wav").toExternalForm());
        ladderSound = new AudioClip(getClass().getResource("/sounds/ladder.wav").toExternalForm());
        winSound = new AudioClip(getClass().getResource("/sounds/win.wav").toExternalForm());

        // Load board and tokens
        Image boardImage = new Image("board.png");
        ImageView boardView = new ImageView(boardImage);
        boardView.setFitWidth(600);
        boardView.setFitHeight(600);

        player1Token.setImage(new Image("tokens/Player_Red.png"));
        player2Token.setImage(new Image("tokens/Player_Blue.png"));
        player1Token.setFitWidth(45);
        player2Token.setFitWidth(45);
        player1Token.setFitHeight(45);
        player2Token.setFitHeight(45);
        player1Token.getStyleClass().add("token");
        player2Token.getStyleClass().add("token");

        // Add glow effect to active player
        Glow playerGlow = new Glow(0.8);
        player1Token.setEffect(playerGlow);
        player2Token.setEffect(playerGlow);
        player1Token.setOpacity(0.9);
        player2Token.setOpacity(0.9);

        boardLayer.getStyleClass().add("board-layer");
        boardLayer.getChildren().addAll(boardView, player1Token, player2Token);

        // Player labels
        Label player1Label = new Label("🔴 " + player1.getName());
        player1Label.getStyleClass().addAll("player-label", "player1-label");
        player1PosLabel.getStyleClass().add("position-label");
        player1PosLabel.setText("Position: 1");

        Label player2Label = new Label("🔵 " + player2.getName());
        player2Label.getStyleClass().addAll("player-label", "player2-label");
        player2PosLabel.getStyleClass().add("position-label");
        player2PosLabel.setText("Position: 1");

        // Roll buttons
        Button player1Roll = new Button("🎲 Roll Dice");
        Button player2Roll = new Button("🎲 Roll Dice");
        player1Roll.getStyleClass().add("roll-button");
        player2Roll.getStyleClass().add("roll-button");
        player1Roll.setDisable(false);
        player2Roll.setDisable(true);

        // Play again button
        Button playAgain = new Button("🔄 Play Again");
        playAgain.getStyleClass().add("play-again");
        playAgain.setOnAction(e -> {
            // Stop any ongoing animations
            if (celebrationAnimation != null) {
                celebrationAnimation.stop();
            }

            // Reset token transformations
            player1Token.setRotate(0);
            player1Token.setScaleX(1);
            player1Token.setScaleY(1);
            player2Token.setRotate(0);
            player2Token.setScaleX(1);
            player2Token.setScaleY(1);

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

            // Reset player effects
            player1Token.setEffect(playerGlow);
            player2Token.setEffect(null);
        });

        // Roll actions
        player1Roll.setOnAction(e -> handleRoll(player1, player1Roll, player2Roll));
        player2Roll.setOnAction(e -> handleRoll(player2, player2Roll, player1Roll));

        // Create timer circles for each player
this.timerCircle1 = createTimerCircle(diceImageView1);
this.timerCircle2 = createTimerCircle(diceImageView2);

        // Create stacks to put the circle behind the dice
        StackPane diceStack1 = new StackPane(timerCircle1, diceImageView1);
        StackPane diceStack2 = new StackPane(timerCircle2, diceImageView2);

        // Update player boxes to use the stacks
        VBox player1Box = new VBox(10, player1Label, diceStack1, turnTimerLabel1, player1PosLabel, player1Roll);
        VBox player2Box = new VBox(10, player2Label, diceStack2, turnTimerLabel2, player2PosLabel, player2Roll);
        player1Box.getStyleClass().add("player-box");
        player2Box.getStyleClass().add("player-box");
        player1Box.setAlignment(Pos.CENTER);
        player2Box.setAlignment(Pos.CENTER);
        player1Box.setMinWidth(180);
        player2Box.setMinWidth(180);

        // Main layout
        HBox controlRow = new HBox(20, player2Box, player1Box);
        controlRow.setAlignment(Pos.CENTER);

        // Add style classes
        statusLabel.getStyleClass().add("status-label");
        diceResult.getStyleClass().add("dice-result");
        gameTimerLabel.getStyleClass().add("timer-label");
        turnTimerLabel1.getStyleClass().add("turn-timer");
        turnTimerLabel2.getStyleClass().add("turn-timer");

        VBox layout = new VBox(15, gameTimerLabel, boardLayer, diceResult, statusLabel, controlRow, playAgain);
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(Background.EMPTY);

        Scene scene = new Scene(layout, 700, 800);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("style.css");

        stage.setTitle("🐍 Snake and Ladder 🪜");
        stage.setScene(scene);
        stage.show();

        updateTokens();
        updateTurnText();
        startGameClock();
        startTurnTimer(player1, player1Roll, player2Roll);

        // Initial player highlight
        highlightCurrentPlayer();
    }

    private void handleRoll(Player player, Button currentRoll, Button otherRoll) {
        if (isRolling)
            return;
        isRolling = true;
        currentRoll.setDisable(true);
        stopTurnTimer();

        String dicePath = player.getName().equals("Player 1") ? RED_DICE_PATH : BLUE_DICE_PATH;
        ImageView activeDice = player.getName().equals("Player 1") ? diceImageView1 : diceImageView2;

        if (diceSound != null)
            diceSound.play();

        Thread animationThread = new Thread(() -> {
            try {
                // Dice rolling animation
                for (int i = 0; i < 15; i++) {
                    int tempRoll = random.nextInt(6) + 1;
                    String frame = "/" + dicePath + tempRoll + ".png";
                    Platform.runLater(
                            () -> activeDice.setImage(new Image(getClass().getResource(frame).toExternalForm())));
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

                    int startPos = player.getPosition();
                    int finalPos = gameManager.movePlayer(roll);

                    // Only animate movement if player has started or just got their first 6
                    if (player.hasStarted() || roll == 6) {
                        animateMovement(player, startPos, finalPos, () -> {
                            updateTokens();

                            if (gameManager.checkWin()) {
                                statusLabel.setText(emoji + " " + player.getName() + " wins! 🎉");
                                currentRoll.setDisable(true);
                                otherRoll.setDisable(true);
                                if (winSound != null)
                                    winSound.play();
                                celebrateWin(player);
                            } else {
                                if (gameManager.shouldSwitchTurn(roll)) {
                                    gameManager.switchTurn();
                                    currentRoll.setDisable(true);
                                    otherRoll.setDisable(false);
                                    startTurnTimer(gameManager.getCurrentPlayer(), otherRoll, currentRoll);
                                    highlightCurrentPlayer();
                                } else {
                                    currentRoll.setDisable(false);
                                    startTurnTimer(gameManager.getCurrentPlayer(), currentRoll, otherRoll);
                                }
                            }
                            updateTurnText();
                            isRolling = false;
                        });
                    } else {
                        // Player didn't get a 6 to start, just update UI
                        updateTokens();
                        gameManager.switchTurn();
                        currentRoll.setDisable(true);
                        otherRoll.setDisable(false);
                        startTurnTimer(gameManager.getCurrentPlayer(), otherRoll, currentRoll);
                        highlightCurrentPlayer();
                        updateTurnText();
                        isRolling = false;
                    }
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

    private void animateMovement(Player player, int startPos, int finalPos, Runnable onFinished) {
        ImageView token = player.getName().equals("Player 1") ? player1Token : player2Token;
        boolean isPlayer1 = player.getName().equals("Player 1");

        SequentialTransition walkSteps = new SequentialTransition();

        // Step-by-step animation from startPos to finalPos
        for (int i = 1; i <= finalPos - startPos; i++) {
            int step = startPos + i;
            TranslateTransition stepAnim = createStepTransition(token, step, isPlayer1, i == finalPos - startPos);
            walkSteps.getChildren().add(stepAnim);
            if (i < finalPos - startPos) {
                walkSteps.getChildren().add(new PauseTransition(Duration.millis(100)));
            }
        }

        // After walking, check if landed on ladder base
        walkSteps.setOnFinished(event -> {
            Integer ladderTop = gameManager.getBoard().getLadders().get(finalPos);
            if (ladderTop != null) {
                // 🎯 FinalPos is a ladder base ➜ climb to top
                TranslateTransition climb = createStraightLineTransition(token, finalPos, ladderTop);
                climb.setOnFinished(e -> onFinished.run());
                climb.play();
            } else {
                onFinished.run();
            }
        });

        walkSteps.play();
    }

    private TranslateTransition createStepTransition(ImageView token, int position, boolean isPlayer1,
            boolean isLastStep) {
        int cellSize = 60;
        int col = (position - 1) % 10;
        int row = (position - 1) / 10;
        if (row % 2 == 1)
            col = 9 - col;

        double centerX = col * cellSize + cellSize / 2.0;
        double centerY = (9 - row) * cellSize + cellSize / 2.0;

        double offset = 15;
        double x = isPlayer1 ? centerX - offset : centerX + offset;
        double y = centerY;

        double targetX = x - token.getFitWidth() / 2;
        double targetY = y - token.getFitHeight() / 2;

        TranslateTransition transition = new TranslateTransition(Duration.millis(150), token);
        transition.setToX(targetX);
        transition.setToY(targetY);

        if (isLastStep) {
            // Bounce effect when landing
            transition.setOnFinished(e -> {
                ScaleTransition bounce = new ScaleTransition(Duration.millis(100), token);
                bounce.setToX(1.2);
                bounce.setToY(1.2);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.play();
            });
        }

        return transition;
    }

    private void animateSnakeBite(ImageView token, int startPos, int finalPos, Runnable onFinished) {
        if (snakeSound != null)
            snakeSound.play();

        // Create snake effect
        Circle snakeEffect = new Circle(30, Color.RED);
        snakeEffect.getStyleClass().add("snake-effect");
        snakeEffect.setOpacity(0);
        boardLayer.getChildren().add(snakeEffect);

        // Position the effect at the snake's head
        positionEffect(snakeEffect, startPos);

        // Animation sequence
        ParallelTransition snakeAnimation = new ParallelTransition();

        // Fade in effect
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), snakeEffect);
        fadeIn.setToValue(0.7);

        // Scale effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(500), snakeEffect);
        scale.setToX(1.5);
        scale.setToY(1.5);

        snakeAnimation.getChildren().addAll(fadeIn, scale);

        SequentialTransition sequence = new SequentialTransition(
                snakeAnimation,
                new PauseTransition(Duration.millis(300)),
                createStepTransition(token, finalPos, token == player1Token, true),
                new PauseTransition(Duration.millis(300)),
                new FadeTransition(Duration.millis(500), snakeEffect),
                new PauseTransition(Duration.millis(100)));

        sequence.setOnFinished(e -> {
            boardLayer.getChildren().remove(snakeEffect);
            onFinished.run();
        });

        sequence.play();
    }

    private void animateLadderClimb(ImageView token, int ladderBasePos, int ladderTopPos, Runnable onFinished) {
        if (ladderSound != null)
            ladderSound.play();

        Rectangle ladderEffect = new Rectangle(40, 40, Color.GREEN);
        ladderEffect.getStyleClass().add("ladder-effect");
        ladderEffect.setOpacity(0);
        boardLayer.getChildren().add(ladderEffect);
        positionEffect(ladderEffect, ladderBasePos);

        // Determine where the token currently is
        int currentPos = (token == player1Token) ? previousPlayer1Pos : previousPlayer2Pos;
        SequentialTransition fullAnimation = new SequentialTransition();

        // 1. Walk to the base of the ladder if not already there
        if (currentPos < ladderBasePos) {
            for (int i = currentPos + 1; i <= ladderBasePos; i++) {
                TranslateTransition step = createStepTransition(token, i, token == player1Token, i == ladderBasePos);
                fullAnimation.getChildren().add(step);
                if (i < ladderBasePos) {
                    fullAnimation.getChildren().add(new PauseTransition(Duration.millis(100)));
                }
            }
            fullAnimation.getChildren().add(new PauseTransition(Duration.millis(300))); // pause at base
        }

        // 2. Straight line climb from base to top
        TranslateTransition climb = createStraightLineTransition(token, ladderBasePos, ladderTopPos);
        fullAnimation.getChildren().add(climb);
        fullAnimation.getChildren().add(new PauseTransition(Duration.millis(300))); // pause at top

        fullAnimation.setOnFinished(e -> {
            boardLayer.getChildren().remove(ladderEffect);
            onFinished.run();
        });

        fullAnimation.play();
    }

    private TranslateTransition createStraightLineTransition(ImageView token, int fromPos, int toPos) {
        int cellSize = 60;

        // Calculate positions
        int fromCol = (fromPos - 1) % 10;
        int fromRow = (fromPos - 1) / 10;
        if (fromRow % 2 == 1)
            fromCol = 9 - fromCol;

        int toCol = (toPos - 1) % 10;
        int toRow = (toPos - 1) / 10;
        if (toRow % 2 == 1)
            toCol = 9 - toCol;

        // Calculate center points
        double fromX = fromCol * cellSize + cellSize / 2.0;
        double fromY = (9 - fromRow) * cellSize + cellSize / 2.0;
        double toX = toCol * cellSize + cellSize / 2.0;
        double toY = (9 - toRow) * cellSize + cellSize / 2.0;

        // Apply player offset
        double offset = 12;
        if (token == player1Token) {
            fromX -= offset;
            toX -= offset;
        } else {
            fromX += offset;
            toX += offset;
        }

        // Calculate target positions
        double targetFromX = fromX - token.getFitWidth() / 2;
        double targetFromY = fromY - token.getFitHeight() / 2;
        double targetToX = toX - token.getFitWidth() / 2;
        double targetToY = toY - token.getFitHeight() / 2;

        // Set starting position
        token.setTranslateX(targetFromX);
        token.setTranslateY(targetFromY);

        // Create straight line transition
        TranslateTransition climb = new TranslateTransition(Duration.millis(600), token);
        climb.setToX(targetToX);
        climb.setToY(targetToY);

        // Add slight scale effect during climb
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), token);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        return climb;
    }

    private void positionEffect(javafx.scene.Node effect, int position) {
        int cellSize = 60;
        int col = (position - 1) % 10;
        int row = (position - 1) / 10;
        if (row % 2 == 1)
            col = 9 - col;

        double centerX = col * cellSize + cellSize / 2.0;
        double centerY = (9 - row) * cellSize + cellSize / 2.0;

        effect.setLayoutX(centerX - 20);
        effect.setLayoutY(centerY - 20);
    }

    private void celebrateWin(Player player) {
        ImageView token = player.getName().equals("Player 1") ? player1Token : player2Token;

        // Stop any existing celebration
        if (celebrationAnimation != null) {
            celebrationAnimation.stop();
        }

        celebrationAnimation = new ParallelTransition();

        // Bounce animation
        ScaleTransition bounce = new ScaleTransition(Duration.millis(200), token);
        bounce.setToX(1.5);
        bounce.setToY(1.5);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Animation.INDEFINITE);

        // Rotation animation
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), token);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);

        celebrationAnimation.getChildren().addAll(bounce, rotate);
        celebrationAnimation.play();
    }

    private void highlightCurrentPlayer() {
        if (gameManager.getCurrentPlayer().getName().equals("Player 1")) {
            player1Token.setEffect(new Glow(0.8));
            player2Token.setEffect(null);
        } else {
            player2Token.setEffect(new Glow(0.8));
            player1Token.setEffect(null);
        }
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
        if (gameTimer != null)
            gameTimer.stop();
        remainingSeconds = 30 * 60;
        startGameClock();
    }

    private void startTurnTimer(Player player, Button currentRoll, Button otherRoll) {
        stopTurnTimer();
        final int[] countdown = {5};
        Label turnLabel = player.getName().equals("Player 1") ? turnTimerLabel1 : turnTimerLabel2;
        Circle timerCircle = player.getName().equals("Player 1") ? timerCircle1 : timerCircle2;
        
        // Reset timer circle
        timerCircle.setStroke(Color.DARKGOLDENROD);
        timerCircle.getStrokeDashArray().setAll(1000d, 0d); // Reset to full circle
        
        turnCountdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown[0]--;
            turnLabel.setText("⏳ " + countdown[0] + "s");
            
            // Update circle progress
            double progress = countdown[0] / 5.0;
            updateTimerCircle(timerCircle, progress);
            
            if (countdown[0] <= 0) {
                turnCountdown.stop();
                currentRoll.setDisable(true);
                otherRoll.setDisable(false);
                gameManager.switchTurn();
                updateTurnText();
                startTurnTimer(gameManager.getCurrentPlayer(), otherRoll, currentRoll);
            }
        }));
        
        // Initial setup
        turnLabel.setText("⏳ 5s");
        updateTimerCircle(timerCircle, 1.0);
        
        // Animate the circle continuously
        Timeline circleAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(timerCircle.strokeDashOffsetProperty(), 0)),
            new KeyFrame(Duration.seconds(5), new KeyValue(timerCircle.strokeDashOffsetProperty(), 1))
        );
        
        turnCountdown.setCycleCount(5);
        turnCountdown.play();
        circleAnimation.play();
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
        if (row % 2 == 1)
            col = 9 - col;

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

        // Remove sound playing from here since we handle it in animateMovement
        transition.play();
    }

    private Circle createTimerCircle(ImageView dice) {
        Circle timerCircle = new Circle();
        timerCircle.setRadius(40);
        timerCircle.setStroke(Color.DARKGOLDENROD);
        timerCircle.setStrokeWidth(4);
        timerCircle.setFill(Color.TRANSPARENT);
        timerCircle.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Initialize dash array
        ObservableList<Double> dashArray = timerCircle.getStrokeDashArray();
        dashArray.clear();
        dashArray.addAll(1000d, 0d); // Start with full circle
        
        // Position it around the dice
        timerCircle.setCenterX(dice.getFitWidth()/2);
        timerCircle.setCenterY(dice.getFitHeight()/2);
        
        return timerCircle;
    }

    private void updateTimerCircle(Circle timerCircle, double progress) {
        // Calculate dash array values
        double circumference = 2 * Math.PI * timerCircle.getRadius();
        double visibleLength = progress * circumference;
        double invisibleLength = circumference - visibleLength;
        
        // Update the stroke dash array
        ObservableList<Double> dashArray = timerCircle.getStrokeDashArray();
        dashArray.clear();
        dashArray.addAll(visibleLength, invisibleLength);
        
        // Change color as time runs out
        if (progress < 0.2) {
            timerCircle.setStroke(Color.RED);
        } else if (progress < 0.5) {
            timerCircle.setStroke(Color.ORANGE);
        } else {
            timerCircle.setStroke(Color.DARKGOLDENROD);
        }
    }
}