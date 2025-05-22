package com.asma.snake.client;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Player;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
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
    private final ImageView diceImageView1 = new ImageView();
    private final ImageView diceImageView2 = new ImageView();

    private final Random random = new Random();
    private boolean isRolling = false;

    private int previousPlayer1Pos = 1, previousPlayer2Pos = 1;

    private static final String RED_DICE_PATH = "dice/red/dice";
    private static final String BLUE_DICE_PATH = "dice/blue/dice";

    private NetworkClient net;
    private String yourColor;
    private final Button player1Roll;
    private final Button player2Roll;

    private final Queue<String> earlyMessages = new LinkedList<>();
    private boolean isReady = false;

    public GameUI(Stage stage) {
        // 1) Game logic
        Player p1 = new Player("Player 1", "red");
        Player p2 = new Player("Player 2", "blue");
        gameManager = new GameManager(p1, p2);

        // 2) Roll buttons
        player1Roll = new Button("Roll Dice");
        player2Roll = new Button("Roll Dice");
        player1Roll.getStyleClass().add("roll-button");
        player2Roll.getStyleClass().add("roll-button");
        // disable until server MATCHED & TURN
        player1Roll.setDisable(true);
        player2Roll.setDisable(true);
        player1Roll.setOnAction(e -> handleRoll(p1));
        player2Roll.setOnAction(e -> handleRoll(p2));

        // 3) Load dice images
        diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
        diceImageView1.setFitWidth(70);
        diceImageView1.setFitHeight(70);
        diceImageView2.setFitWidth(70);
        diceImageView2.setFitHeight(70);
        diceImageView1.getStyleClass().add("dice-image");
        diceImageView2.getStyleClass().add("dice-image");

        // 5) Board + tokens
        ImageView boardView = new ImageView(
                new Image(getClass().getResource("/board.png").toExternalForm()));
        boardView.setFitWidth(600);
        boardView.setFitHeight(600);

        player1Token.setImage(new Image(getClass().getResource("/tokens/Player_Red.png").toExternalForm()));
        player2Token.setImage(new Image(getClass().getResource("/tokens/Player_Blue.png").toExternalForm()));
        player1Token.setFitWidth(45);
        player1Token.setFitHeight(45);
        player2Token.setFitWidth(45);
        player2Token.setFitHeight(45);
        player1Token.getStyleClass().add("token");
        player2Token.getStyleClass().add("token");

        Glow glow = new Glow(0.8);
        player1Token.setEffect(glow);
        player2Token.setEffect(glow);

        boardLayer.getStyleClass().add("board-layer");
        boardLayer.getChildren().addAll(boardView, player1Token, player2Token);

        // 6) Labels
        Label lbl1 = new Label("Red " + p1.getName());
        lbl1.getStyleClass().addAll("player-label", "player1-label");
        player1PosLabel.getStyleClass().add("position-label");
        player1PosLabel.setText("Position: 1");

        Label lbl2 = new Label("Blue " + p2.getName());
        lbl2.getStyleClass().addAll("player-label", "player2-label");
        player2PosLabel.getStyleClass().add("position-label");
        player2PosLabel.setText("Position: 1");

        // 7) Turn timers

        StackPane ds1 = new StackPane(diceImageView1);
        StackPane ds2 = new StackPane(diceImageView2);

        VBox box1 = new VBox(10, lbl1, ds1, player1PosLabel, player1Roll);
        box1.setAlignment(Pos.CENTER);
        box1.getStyleClass().add("player-box");
        box1.setMinWidth(180);

        VBox box2 = new VBox(10, lbl2, ds2, player2PosLabel, player2Roll);
        box2.setAlignment(Pos.CENTER);
        box2.getStyleClass().add("player-box");
        box2.setMinWidth(180);

        HBox controls = new HBox(20, box2, box1);
        controls.setAlignment(Pos.CENTER);

        statusLabel.setText("Waiting for another player...");

        statusLabel.getStyleClass().add("status-label");
        diceResult.getStyleClass().add("dice-result");

        // Exit button
        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("exit-button");
        exitButton.setOnAction(e -> {
            net.send("EXIT"); // Notify the server
            net.close(); // Close client socket
            Platform.exit(); // Close window
        });

        VBox root = new VBox(15,
                boardLayer,
                diceResult,
                statusLabel,
                controls,
                exitButton);

        root.setAlignment(Pos.CENTER);
        root.setBackground(Background.EMPTY);

        Scene scene = new Scene(root, 600, 875);
        scene.getStylesheets().add("style.css");
        stage.setTitle("🐍 Snake and Ladder 🪜");
        stage.setScene(scene);
        isReady = true;
        while (!earlyMessages.isEmpty()) {
            processServerMessage(earlyMessages.poll());
        }

        stage.show();
        // Place tokens on tile 1 at game start
        placeTokenAt(1, player1Token, true); // red
        placeTokenAt(1, player2Token, false); // blue

        // 8) Network
        System.out.println("Connecting to game server...");
        // net = new NetworkClient("13.53.129.233", 40000, this::handleServerMessage);
        net = new NetworkClient("localhost", 40000, this::handleServerMessage);
        System.out.println("Sending READY...");
        net.send("READY");

        // if a player exit the game
        stage.setOnCloseRequest(e -> {
            net.close();
        });

    }

    private void handleRoll(Player player) {
        if (isRolling)
            return;
        isRolling = true;
        player1Roll.setDisable(true);
        player2Roll.setDisable(true);

        int simulatedRoll = random.nextInt(6) + 1;
        String path = player.getColor().equals("red") ? RED_DICE_PATH : BLUE_DICE_PATH;
        String frame = "/" + path + simulatedRoll + ".png";

        Platform.runLater(() -> {
            ImageView dv = player.getColor().equals("red") ? diceImageView1 : diceImageView2;
            dv.setImage(new Image(getClass().getResource(frame).toExternalForm()));
            diceResult.setText(
                    (player.getColor().equals("red") ? "Red" : "Blue") +
                            " rolled: " + simulatedRoll);
        });

        net.send("ROLL:" + simulatedRoll);
    }

    private void handleServerMessage(String msg) {
        System.out.println("Server said: " + msg);
        if (!isReady) {
            earlyMessages.add(msg); // queue until setup complete
            return;
        }

        processServerMessage(msg);
    }

    private void processServerMessage(String msg) {

        Platform.runLater(() -> {
            String[] parts = msg.split(":", 3);
            switch (parts[0]) {
                case "MATCHED":
                    yourColor = parts[1];
                    statusLabel.setText("Matched! You are: " + yourColor);
                    startGame();
                    if ("blue".equals(yourColor)) {
                        statusLabel.setText("You are: blue, Opponent's turn...");
                    }
                    break;

                case "TURN":
                    boolean myTurn = yourColor.equals(parts[1]);
                    player1Roll.setDisable(!(myTurn && "red".equals(yourColor)));
                    player2Roll.setDisable(!(myTurn && "blue".equals(yourColor)));
                    statusLabel.setText("You are: " + parts[1]);
                    isRolling = false;
                    break;
                case "ROLL":
                    String rollColor = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    ImageView targetDiceView = rollColor.equals("red") ? diceImageView1 : diceImageView2;
                    String imagePath = (rollColor.equals("red") ? RED_DICE_PATH : BLUE_DICE_PATH) + value + ".png";
                    targetDiceView.setImage(new Image(getClass().getResource("/" + imagePath).toExternalForm()));
                    diceResult.setText((rollColor.equals("red") ? "Red" : "Blue") + " rolled: " + value);
                    break;

                case "MOVE":
                    String color = parts[1];
                    int newPos = Integer.parseInt(parts[2]);
                    if (color.equals("red")) {
                        animateMovement(gameManager.getPlayer1(), previousPlayer1Pos, newPos, () -> {
                            previousPlayer1Pos = newPos;
                            player1PosLabel.setText("Position: " + newPos);
                        });
                    } else {
                        animateMovement(gameManager.getPlayer2(), previousPlayer2Pos, newPos, () -> {
                            previousPlayer2Pos = newPos;
                            player2PosLabel.setText("Position: " + newPos);
                        });
                    }
                    break;
                case "WIN":
                    statusLabel.setText(parts[1] + " wins!");
                    player1Roll.setDisable(true);
                    player2Roll.setDisable(true);
                    break;

                case "GAME_OVER":
                    String winner = parts[1];
                    statusLabel.setText(winner + " wins!");

                    break;
                case "EXIT":
                    statusLabel.setText("Your opponent has exited. Game over.");
                    statusLabel.getStyleClass().add("exit-message");
                    player1Roll.setDisable(true);
                    player2Roll.setDisable(true);
                    break;

            }
        });
    }

    private void startGame() {
        // Reset player positions
        previousPlayer1Pos = 1;
        previousPlayer2Pos = 1;
        player1PosLabel.setText("Position: 1");
        player2PosLabel.setText("Position: 1");

        // Reset tokens to position 1
        placeTokenAt(1, player1Token, true);
        placeTokenAt(1, player2Token, false);

        // Reset dice
        diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
        diceResult.setText("");
        statusLabel.setText("");

        gameManager.resetGame();
    }

    private void animateMovement(Player player, int start, int end, Runnable onFinished) {
        ImageView token = player.getColor().equals("red") ? player1Token : player2Token;
        boolean isP1 = player.getColor().equals("red");
        int size = 60;

        int col = (end - 1) % 10;
        int row = (end - 1) / 10;
        if (row % 2 == 1)
            col = 9 - col;

        double cx = col * size + size / 2.0;
        double cy = (9 - row) * size + size / 2.0;
        double off = 15;
        double x = (isP1 ? cx - off : cx + off);
        double y = cy;

        token.setTranslateX(x - token.getFitWidth() / 2);
        token.setTranslateY(y - token.getFitHeight() / 2);

        onFinished.run();
    }

    private void placeTokenAt(int position, ImageView token, boolean isPlayer1) {
        int size = 60;
        int col = (position - 1) % 10;
        int row = (position - 1) / 10;
        if (row % 2 == 1)
            col = 9 - col;

        double cx = col * size + size / 2.0;
        double cy = (9 - row) * size + size / 2.0;
        double off = 15;
        double x = (isPlayer1 ? cx - off : cx + off);
        double y = cy;

        token.setTranslateX(x - token.getFitWidth() / 2);
        token.setTranslateY(y - token.getFitHeight() / 2);
    }

}
