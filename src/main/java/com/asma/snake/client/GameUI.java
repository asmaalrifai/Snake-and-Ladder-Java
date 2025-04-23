package com.asma.snake.client;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class GameUI {

    private final GameManager gameManager;
    private final Label statusLabel = new Label();
    private final Label diceResult = new Label();
    private final Circle player1Token = new Circle(15);
    private final Circle player2Token = new Circle(15);
    private final Pane boardLayer = new Pane();
    private final Label player1PosLabel = new Label();
    private final Label player2PosLabel = new Label();

    public GameUI(Stage stage) {
        Player player1 = new Player("Player 1", "red");
        Player player2 = new Player("Player 2", "blue");
        gameManager = new GameManager(player1, player2);

        // Load the board image
        Image boardImage = new Image("board.png");
        ImageView boardView = new ImageView(boardImage);
        boardView.setFitWidth(600);
        boardView.setFitHeight(600);

        player1Token.setFill(Color.RED);
        player2Token.setFill(Color.BLUE);
        boardLayer.getChildren().addAll(boardView, player1Token, player2Token);

        // Player 1 controls
        Label player1Label = new Label("🔴 " + player1.getName());
        player1Label.setTextFill(Color.RED);
        player1PosLabel.setText("Position: 1");
        Button player1Roll = new Button("Roll Dice");
        player1Roll.setDisable(false);

        // Player 2 controls
        Label player2Label = new Label("🔵 " + player2.getName());
        player2Label.setTextFill(Color.BLUE);
        player2PosLabel.setText("Position: 1");
        Button player2Roll = new Button("Roll Dice");
        player2Roll.setDisable(true);

        player1Roll.setOnAction(e -> {
            handleRoll(player1, player1Roll, player2Roll);
        });

        player2Roll.setOnAction(e -> {
            handleRoll(player2, player2Roll, player1Roll);
        });

        VBox player1Box = new VBox(5, player1Label, player1PosLabel, player1Roll);
        player1Box.setAlignment(Pos.CENTER_RIGHT);

        VBox player2Box = new VBox(5, player2Label, player2PosLabel, player2Roll);
        player2Box.setAlignment(Pos.CENTER_LEFT);

        HBox controlRow = new HBox(200, player2Box, player1Box);
        controlRow.setAlignment(Pos.CENTER);
        controlRow.setPrefHeight(100);

        VBox layout = new VBox(boardLayer, diceResult, statusLabel, controlRow);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 620, 700);
        scene.getStylesheets().add("style.css");

        stage.setTitle("Snake and Ladder");
        stage.setScene(scene);
        stage.show();

        updateTokens();
        updateTurnText();
    }

    // ✅ MOVE THIS OUTSIDE THE CONSTRUCTOR
    private void handleRoll(Player player, Button currentRoll, Button otherRoll) {
        int roll = gameManager.rollDice();
        diceResult.setText(player.getName() + " rolled: " + roll);

        if (gameManager.getCurrentPlayer() != player) {
            return;
        }

        int finalPos = gameManager.movePlayer(roll);
        updateTokens();

        if (gameManager.checkWin()) {
            statusLabel.setText(player.getName() + " wins!");
            currentRoll.setDisable(true);
            otherRoll.setDisable(true);
            return;
        }

        if (gameManager.shouldSwitchTurn(roll)) {
            gameManager.switchTurn();
            currentRoll.setDisable(true);
            otherRoll.setDisable(false);
        }

        updateTurnText();
    }

    private void updateTurnText() {
        statusLabel.setText("Turn: " + gameManager.getCurrentPlayer().getName());
    }

    private void updateTokens() {
        updateTokenPosition(player1Token, gameManager.getPlayer1().getPosition());
        updateTokenPosition(player2Token, gameManager.getPlayer2().getPosition());
        player1PosLabel.setText("Position: " + gameManager.getPlayer1().getPosition());
        player2PosLabel.setText("Position: " + gameManager.getPlayer2().getPosition());

    }

    private void updateTokenPosition(Circle token, int position) {
        int cellSize = 60;
        int col = (position - 1) % 10;
        int row = (position - 1) / 10;

        if ((row % 2) == 1) {
            col = 9 - col;
        }

        double x = col * cellSize + cellSize / 2.0;
        double y = (9 - row) * cellSize + cellSize / 2.0;

        token.setLayoutX(x);
        token.setLayoutY(y);
    }
}
