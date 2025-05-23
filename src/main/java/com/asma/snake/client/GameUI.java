package com.asma.snake.client;

import com.asma.snake.logic.GameManager;
import com.asma.snake.model.Board;
import com.asma.snake.model.Player;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * This class represents the main game UI for the Snake and Ladder game.
 * It handles rendering the board, user interactions, and communication with the
 * server.
 */
public class GameUI {

    // Game logic controller (handles rules, moves, and turn switching)
    private final GameManager gameManager;

    // UI components to display game status and dice result
    private final Label statusLabel = new Label();
    private final Label diceResult = new Label();

    // Server address and port (modifiable from one place)
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 40000;

    // Player tokens and board display
    private final ImageView player1Token = new ImageView();
    private final ImageView player2Token = new ImageView();
    private final Pane boardLayer = new Pane(); // Holds board and tokens

    // Position labels for each player
    private final Label player1PosLabel = new Label();
    private final Label player2PosLabel = new Label();

    // Dice images for each player
    private final ImageView diceImageView1 = new ImageView();
    private final ImageView diceImageView2 = new ImageView();

    // Dice roll helper and rolling flag to prevent duplicate rolls
    private final Random random = new Random();
    private boolean isRolling = false;

    // Tracks last known position of each player for animation
    private int previousPlayer1Pos = 1, previousPlayer2Pos = 1;

    // File path templates for dice image resources
    private static final String RED_DICE_PATH = "dice/red/dice";
    private static final String BLUE_DICE_PATH = "dice/blue/dice";

    // Network connection to the server and the assigned color for this client
    private NetworkClient net;
    private String yourColor;

    // Buttons for rolling the dice for each player
    private final Button player1Roll = new Button("Roll Dice");
    private final Button player2Roll = new Button("Roll Dice");

    // Used to store server messages that arrive before the UI is ready
    private final Queue<String> earlyMessages = new LinkedList<>();
    private boolean isReady = false;

    // Button to request a replay after the game ends
    private final Button playAgainButton = new Button("Play Again");

    // for the ladder and snake jumps displays
    private final Label player1EffectLabel = new Label();
    private final Label player2EffectLabel = new Label();

    public GameUI(Stage stage) {
        Player p1 = new Player("Player 1", "red");
        Player p2 = new Player("Player 2", "blue");
        gameManager = new GameManager(p1, p2);

        // Initialize roll buttons
        initRollButtons(p1, p2);

        // Initialize dice images
        setupDiceImages();

        // Load board and tokens
        setupBoardAndTokens();

        // Labels for players
        setupPlayerLabels(p1, p2);

        // Control panel with dice and roll buttons
        HBox controls = createControlPanel();

        // Status and result labels
        statusLabel.setText("Waiting for\nanother player...");
        statusLabel.getStyleClass().add("status-label");
        diceResult.getStyleClass().add("dice-result");

        // Play Again and Exit buttons
        HBox buttonBox = setupBottomButtons();

        // Create player boxes
        VBox box1 = new VBox(10,
                new Label("Red Player"),
                new StackPane(diceImageView1),
                player1PosLabel,
                player1EffectLabel,
                player1Roll);
        box1.setAlignment(Pos.CENTER);
        box1.setMinWidth(180);
        box1.getStyleClass().add("player-box");

        VBox box2 = new VBox(10,
                new Label("Blue Player"),
                new StackPane(diceImageView2),
                player2PosLabel,
                player2EffectLabel,
                player2Roll);
        box2.setAlignment(Pos.CENTER);
        box2.setMinWidth(180);
        box2.getStyleClass().add("player-box");

        // Create middle message box (dice result + status)
        HBox controlsWithCenterLabel = new HBox(20, box2, centerMessageBox(), box1);
        controlsWithCenterLabel.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, boardLayer, controlsWithCenterLabel, buttonBox);

        root.setAlignment(Pos.CENTER);
        root.setBackground(Background.EMPTY);

        Scene scene = new Scene(root, 600, 885);
        scene.getStylesheets().add("style.css");

        stage.setTitle("Snake and Ladder");
        stage.setScene(scene);
        stage.show();

        // Token initial placement
        placeTokenAt(1, player1Token, true);
        placeTokenAt(1, player2Token, false);

        // Networking
        net = new NetworkClient(SERVER_HOST, SERVER_PORT, this::handleServerMessage);
        net.send("READY");

        isReady = true;
        while (!earlyMessages.isEmpty()) {
            processServerMessage(earlyMessages.poll());
        }

        stage.setOnCloseRequest(e -> {
            e.consume();
            net.send("EXIT");
            net.close();
            Platform.exit();
        });
    }

    // Configure and disable roll buttons initially. Attach action handlers.
    private void initRollButtons(Player p1, Player p2) {
        // Disable buttons at start and apply CSS class
        player1Roll.setDisable(true);
        player2Roll.setDisable(true);
        player1Roll.getStyleClass().add("roll-button");
        player2Roll.getStyleClass().add("roll-button");

        // Bind each button to call handleRoll() with the correct player
        player1Roll.setOnAction(e -> handleRoll(p1));
        player2Roll.setOnAction(e -> handleRoll(p2));

    }

    // Load initial dice face images and apply styles.
    private void setupDiceImages() {
        // Load default dice images (value 1) for red and blue
        diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));

        // Set size and style for both dice
        for (ImageView dice : new ImageView[] { diceImageView1, diceImageView2 }) {
            dice.setFitWidth(70);
            dice.setFitHeight(70);
            dice.getStyleClass().add("dice-image");
        }

    }

    // Load the board image and player tokens. Apply layout and styles.
    private void setupBoardAndTokens() {
        // Load the board background image and set its size
        ImageView boardView = new ImageView(new Image(getClass().getResource("/board.png").toExternalForm()));
        boardView.setFitWidth(600);
        boardView.setFitHeight(600);

        // Load each player's token image
        player1Token.setImage(new Image(getClass().getResource("/tokens/Player_Red.png").toExternalForm()));
        player2Token.setImage(new Image(getClass().getResource("/tokens/Player_Blue.png").toExternalForm()));

        // Set size and style for tokens
        for (ImageView token : new ImageView[] { player1Token, player2Token }) {
            token.setFitWidth(45);
            token.setFitHeight(45);
            token.getStyleClass().add("token");
        }

        // Add board and tokens to the game layer
        boardLayer.getStyleClass().add("board-layer");
        boardLayer.getChildren().addAll(boardView, player1Token, player2Token);

    }

    // Create name and position labels for both players.
    private void setupPlayerLabels(Player p1, Player p2) {
        // Create and style labels for player names and positions
        Label lbl1 = new Label("Red " + p1.getName());
        lbl1.getStyleClass().addAll("player-label", "player1-label");
        player1PosLabel.getStyleClass().add("position-label");
        player1PosLabel.setText("Position: 1");

        Label lbl2 = new Label("Blue " + p2.getName());
        lbl2.getStyleClass().addAll("player-label", "player2-label");
        player2PosLabel.getStyleClass().add("position-label");
        player2PosLabel.setText("Position: 1");

        // print thw snales and ladders jumps
        player1EffectLabel.getStyleClass().add("effect-label");
        player2EffectLabel.getStyleClass().add("effect-label");
        player1EffectLabel.setText("waiting...");
        player2EffectLabel.setText("waiting...");

    }

    // Create layout containing dice images, position labels, and roll buttons.
    private HBox createControlPanel() {
        // Create player boxes with dice, position, and roll button
        VBox box1 = new VBox(10,
                new Label("Red Player"),
                new StackPane(diceImageView1),
                player1PosLabel,
                player1EffectLabel,
                player1Roll);

        VBox box2 = new VBox(10,
                new Label("Blue Player"),
                new StackPane(diceImageView2),
                player2PosLabel,
                player2EffectLabel,
                player2Roll);

        // Place boxes side by side
        HBox hbox = new HBox(20, box2, box1);
        hbox.setAlignment(Pos.CENTER);
        return hbox;
    }

    // Setup Play Again and Exit buttons with networking behavior.
    private HBox setupBottomButtons() {
        // Create Exit button with server disconnect logic
        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("exit-button");
        exitButton.setOnAction(e -> {
            net.send("EXIT");
            net.close();
            Platform.exit();
        });

        // Configure Play Again button to rejoin server and disable itself
        playAgainButton.getStyleClass().add("play-again-button");
        playAgainButton.setDisable(true);
        playAgainButton.setOnAction(e -> {
            playAgainButton.setDisable(true);
            net.close(); // close old socket
            net = new NetworkClient(SERVER_HOST, SERVER_PORT, this::handleServerMessage); // reconnect
            net.send("READY");
            statusLabel.setText("Waiting for\nopponent\nto replay...");
        });

        // Group and return the buttons in a layout
        HBox box = new HBox(20, playAgainButton, exitButton);
        box.setAlignment(Pos.CENTER);
        return box;

    }

    // Simulate a dice roll, update the image and notify the server.
    private void handleRoll(Player player) {
        // Prevent duplicate roll actions
        if (isRolling)
            return;
        isRolling = true;

        // Disable both roll buttons after click
        player1Roll.setDisable(true);
        player2Roll.setDisable(true);

        // Generate a random number between 1–6
        int value = random.nextInt(6) + 1;

        // Construct path to corresponding dice image
        String path = (player.getColor().equals("red") ? RED_DICE_PATH : BLUE_DICE_PATH) + value + ".png";

        // Update dice image and display result text
        ImageView view = player.getColor().equals("red") ? diceImageView1 : diceImageView2;
        view.setImage(new Image(getClass().getResource("/" + path).toExternalForm()));

        diceResult.setText((player.getColor().equals("red") ? "Red" : "Blue") + " rolled: " + value);

        // Send result to server
        net.send("ROLL:" + value);
    }

    // If not ready, queue message. Otherwise, process it immediately.
    private void handleServerMessage(String msg) {
        if (!isReady) {
            earlyMessages.add(msg);
        } else {
            processServerMessage(msg);
        }
    }

    // Handle different server messages (MATCHED, TURN, ROLL, MOVE, WIN, EXIT, etc.)
    private void processServerMessage(String msg) {
        Platform.runLater(() -> {
            String[] parts = msg.split(":", 3); // Split message into prefix and data

            switch (parts[0]) {

                case "MATCHED" -> {
                    // Assign color from server and start game
                    yourColor = parts[1];
                    statusLabel.setText("Matched! You are: " + yourColor);
                    startGame();
                    playAgainButton.setDisable(true); // disable play again during match

                    // Optional message if player is blue (red always starts)
                    if ("blue".equals(yourColor)) {
                        statusLabel.setText("You are: blue\nOpponent's turn...");
                    }
                }

                case "TURN" -> {
                    // Enable roll button only for the current player
                    boolean myTurn = yourColor.equals(parts[1]);
                    player1Roll.setDisable(!(myTurn && "red".equals(yourColor)));
                    player2Roll.setDisable(!(myTurn && "blue".equals(yourColor)));
                    statusLabel.setText("You're: " + parts[1]);
                    isRolling = false; // allow next roll
                }

                case "ROLL" -> {
                    // Update dice image and result text based on the server's roll info
                    String color = parts[1];
                    int val = Integer.parseInt(parts[2]);
                    ImageView dice = "red".equals(color) ? diceImageView1 : diceImageView2;
                    dice.setImage(new Image(getClass()
                            .getResource("/" + (color.equals("red") ? RED_DICE_PATH : BLUE_DICE_PATH) + val + ".png")
                            .toExternalForm()));
                    diceResult.setText((color.equals("red") ? "Red" : "Blue") + " rolled: " + val);
                }

                case "MOVE" -> {
                    String color = parts[1];
                    int newPos = Integer.parseInt(parts[2]);
                    Board board = gameManager.getBoard();
                    int displayFrom, displayTo;

                    if (color.equals("red")) {
                        displayFrom = previousPlayer1Pos;
                        displayTo = newPos;
                        animateMovement(gameManager.getPlayer1(), displayFrom, displayTo, () -> {
                            previousPlayer1Pos = displayTo;
                            player1PosLabel.setText("Position: " + displayTo);
                            updateEffectLabel(player1EffectLabel, displayFrom, displayTo, board);

                        });
                    } else {
                        displayFrom = previousPlayer2Pos;
                        displayTo = newPos;
                        animateMovement(gameManager.getPlayer2(), displayFrom, displayTo, () -> {
                            previousPlayer2Pos = displayTo;
                            player2PosLabel.setText("Position: " + displayTo);
                            updateEffectLabel(player2EffectLabel, displayFrom, displayTo, board);

                        });
                    }

                }

                case "WIN" -> {
                    // Show winner and enable play again
                    statusLabel.setText(parts[1] + " wins!");
                    statusLabel.getStyleClass().removeAll("exit-message"); // remove old styles
                    statusLabel.getStyleClass().add("win-message");
                    player1Roll.setDisable(true);
                    player2Roll.setDisable(true);
                    playAgainButton.setDisable(false); // Enable replay
                }

                case "EXIT" -> {
                    // Handle opponent disconnection
                    statusLabel.setText("Your opponent\nhas exited.\nGame over.");
                    statusLabel.getStyleClass().add("exit-message");
                    player1Roll.setDisable(true);
                    player2Roll.setDisable(true);
                    playAgainButton.setDisable(false); // Enable replay
                }
            }
        });
    }

    // Reset game UI and state for a new match.
    private void startGame() {
        // Reset position tracking
        previousPlayer1Pos = previousPlayer2Pos = 1;

        // Update labels to starting positions
        player1PosLabel.setText("Position: 1");
        player2PosLabel.setText("Position: 1");

        // Reset token visuals to starting tile
        placeTokenAt(1, player1Token, true);
        placeTokenAt(1, player2Token, false);

        // Reset dice visuals
        diceImageView1.setImage(new Image(getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(getClass().getResource("/dice/blue/dice1.png").toExternalForm()));

        // Clear any text shown in result/status
        diceResult.setText("");
        statusLabel.setText("");

        // Reset logic state in the GameManager
        gameManager.resetGame();

        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("win-message", "exit-message"); // clear win/loss styles

    }

    // Move token to new board position visually. Then run a callback.
    private void animateMovement(Player player, int start, int end, Runnable onFinished) {
        // Choose which token to move and place it at the destination
        placeTokenAt(end, player.getColor().equals("red") ? player1Token : player2Token,
                "red".equals(player.getColor()));
        onFinished.run(); // Perform post-move actions (e.g., update label)
    }

    // Calculate grid coordinates and place the token on the board.
    private void placeTokenAt(int position, ImageView token, boolean isPlayer1) {
        int size = 60; // tile width/height

        int col = (position - 1) % 10;
        int row = (position - 1) / 10;

        // Handle zigzag pattern of Snake & Ladder board
        if (row % 2 == 1)
            col = 9 - col;

        // Calculate center of tile
        double cx = col * size + size / 2.0;
        double cy = (9 - row) * size + size / 2.0;

        // Offset slightly left/right to avoid overlap
        double off = 15;
        double x = isPlayer1 ? cx - off : cx + off;
        double y = cy;

        // Apply translation to token
        token.setTranslateX(x - token.getFitWidth() / 2);
        token.setTranslateY(y - token.getFitHeight() / 2);
    }

    private VBox centerMessageBox() {
        VBox messageBox = new VBox(8, diceResult, statusLabel);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setMinWidth(160);
        return messageBox;
    }

    // show if the player hit on a snake or a ladder
    private void updateEffectLabel(Label label, int from, int to, Board board) {
        // No movement occurred
        if (from == to) {
            label.setText("waiting...");
        }
        // Direct ladder climb from 'from' to 'to'
        else if (board.getLadders().containsKey(from) && board.getLadders().get(from) == to) {
            label.setText("Ladder: " + from + " → " + to);
        }
        // Direct snake slide from 'from' to 'to'
        else if (board.getSnakes().containsKey(from) && board.getSnakes().get(from) == to) {
            label.setText("Snake: " + from + " → " + to);
        } else {
            // Reverse lookup in case 'to' is only provided (no intermediate jump shown)
            if (board.getLadders().containsValue(to)) {
                for (var entry : board.getLadders().entrySet()) {
                    if (entry.getValue() == to) {
                        label.setText("Ladder: " + entry.getKey() + " → " + to);
                        return;
                    }
                }
            } else if (board.getSnakes().containsValue(to)) {
                for (var entry : board.getSnakes().entrySet()) {
                    if (entry.getValue() == to) {
                        label.setText("Snake: " + entry.getKey() + " → " + to);
                        return;
                    }
                }
            } else {
                // No snake or ladder effect
                label.setText("normal move");
            }
        }
    }

}