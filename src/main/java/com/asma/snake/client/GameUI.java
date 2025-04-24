package com.asma.snake.client;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

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
    private final ImageView player1Token = new ImageView();
    private final ImageView player2Token = new ImageView();
    private final Pane boardLayer = new Pane();
    private final Label player1PosLabel = new Label();
    private final Label player2PosLabel = new Label();
    private final ImageView diceImageView1 = new ImageView(); // Player 1 (right)
    private final ImageView diceImageView2 = new ImageView(); // Player 2 (left)

    private AudioClip moveSound;
    private AudioClip diceSound;
    private final Random random = new Random();
    private boolean isRolling = false;

    private int previousPlayer1Pos = 1;
    private int previousPlayer2Pos = 1;

    private static final String RED_DICE_PATH = "dice/red/dice";
    private static final String BLUE_DICE_PATH = "dice/blue/dice";

    public GameUI(Stage stage) {
        Player player1 = new Player("Player 1", "red");
        Player player2 = new Player("Player 2", "blue");
        gameManager = new GameManager(player1, player2);

        // Load default dice image
        // Set default dice images
        try {
            diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
            diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("⚠️ Error loading default dice images");
        }

        diceImageView1.setFitHeight(60);
        diceImageView1.setFitWidth(60);
        diceImageView2.setFitHeight(60);
        diceImageView2.setFitWidth(60);

        // Load movement sound
        try {
            moveSound = new AudioClip(getClass().getResource("/sounds/move.wav").toExternalForm());
        } catch (Exception e) {
            System.err.println("⚠️ move.wav not found");
        }

        // Load dice roll sound
        try {
            diceSound = new AudioClip(getClass().getResource("/sounds/Dice.wav").toExternalForm());
        } catch (Exception e) {
            System.err.println("⚠️ Dice.wav not found");
        }

        // Load board
        Image boardImage = new Image("board.png");
        ImageView boardView = new ImageView(boardImage);
        boardView.setFitWidth(600);
        boardView.setFitHeight(600);

        // Load player tokens
        Image tokenRed = new Image("tokens/Player_Red.png");
        Image tokenBlue = new Image("tokens/Player_Blue.png");

        player1Token.setImage(tokenRed);
        player1Token.setFitWidth(40);
        player1Token.setFitHeight(40);

        player2Token.setImage(tokenBlue);
        player2Token.setFitWidth(40);
        player2Token.setFitHeight(40);

        boardLayer.getChildren().addAll(boardView, player1Token, player2Token);

        // Player UI
        Label player1Label = new Label("🔴 " + player1.getName());
        player1Label.setTextFill(Color.RED);
        player1PosLabel.setText("Position: 1");
        Button player1Roll = new Button("Roll Dice");

        Label player2Label = new Label("🔵 " + player2.getName());
        player2Label.setTextFill(Color.BLUE);
        player2PosLabel.setText("Position: 1");
        Button player2Roll = new Button("Roll Dice");

        player1Roll.setDisable(false);
        player2Roll.setDisable(true);

        player1Roll.setOnAction(e -> handleRoll(player1, player1Roll, player2Roll));
        player2Roll.setOnAction(e -> handleRoll(player2, player2Roll, player1Roll));

        VBox player1Box = new VBox(5, player1Label, diceImageView1, player1PosLabel, player1Roll);
        player1Box.setAlignment(Pos.CENTER_RIGHT);

        VBox player2Box = new VBox(5, player2Label, diceImageView2, player2PosLabel, player2Roll);
        player2Box.setAlignment(Pos.CENTER_LEFT);

        HBox controlRow = new HBox(200, player2Box, player1Box);
        controlRow.setAlignment(Pos.CENTER);
        controlRow.setPrefHeight(100);

        VBox layout = new VBox(boardLayer, diceResult, statusLabel, controlRow);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 620, 740);
        scene.getStylesheets().add("style.css");

        stage.setTitle("Snake and Ladder");
        stage.setScene(scene);
        stage.show();

        updateTokens();
        updateTurnText();
    }

    private void handleRoll(Player player, Button currentRoll, Button otherRoll) {
        if (isRolling) return;
    
        isRolling = true;
        currentRoll.setDisable(true);
    
        String dicePath = player.getName().equals("Player 1") ? RED_DICE_PATH : BLUE_DICE_PATH;
        ImageView activeDiceImageView = player.getName().equals("Player 1") ? diceImageView1 : diceImageView2;
    
        if (diceSound != null) diceSound.play();
    
        Thread animationThread = new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    int tempRoll = random.nextInt(6) + 1;
                    String frame = "/" + dicePath + tempRoll + ".png";
    
                    Platform.runLater(() -> {
                        try {
                            activeDiceImageView.setImage(new Image(getClass().getResource(frame).toExternalForm()));
                        } catch (Exception e) {
                            System.err.println("⚠️ Missing frame: " + frame);
                        }
                    });
    
                    Thread.sleep(50);
                }
    
                int roll = gameManager.rollDice();
                String finalImage = "/" + dicePath + roll + ".png";
    
                Platform.runLater(() -> {
                    try {
                        activeDiceImageView.setImage(new Image(getClass().getResource(finalImage).toExternalForm()));
                    } catch (Exception e) {
                        System.err.println("⚠️ Missing final dice: " + finalImage);
                    }
    
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
                        } else {
                            currentRoll.setDisable(false);
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

        if (row % 2 == 1) {
            col = 9 - col;
        }

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
