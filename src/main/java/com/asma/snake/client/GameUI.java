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
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.asma.snake.chat.ChatClient;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
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

    private AudioClip moveSound, diceSound, snakeSound, ladderSound, winSound;
    private final Random random = new Random();
    private boolean isRolling = false;

    private int previousPlayer1Pos = 1, previousPlayer2Pos = 1;

    private Timeline gameTimer, turnCountdown;
    private int remainingSeconds = 30 * 60;

    private static final String RED_DICE_PATH = "dice/red/dice";
    private static final String BLUE_DICE_PATH = "dice/blue/dice";

    private ParallelTransition celebrationAnimation;
    private Circle timerCircle1, timerCircle2;

    private final NetworkClient net;
    private String yourColor; // “red” or “blue”
    private final Button player1Roll;
    private final Button player2Roll;
    private ChatClient chatClient;
    private final TextArea chatArea = new TextArea();
    private final TextField chatInput = new TextField();

    public GameUI(Stage stage) {
        // 1) Game logic
        Player p1 = new Player("Player 1", "red");
        Player p2 = new Player("Player 2", "blue");
        gameManager = new GameManager(p1, p2);

        // 2) Roll buttons
        player1Roll = new Button("🎲 Roll Dice");
        player2Roll = new Button("🎲 Roll Dice");
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

        // 4) Sounds
        moveSound = new AudioClip(getClass().getResource("/sounds/move.wav").toExternalForm());
        diceSound = new AudioClip(getClass().getResource("/sounds/Dice.wav").toExternalForm());
        snakeSound = new AudioClip(getClass().getResource("/sounds/snake.wav").toExternalForm());
        ladderSound = new AudioClip(getClass().getResource("/sounds/ladder.wav").toExternalForm());
        winSound = new AudioClip(getClass().getResource("/sounds/win.wav").toExternalForm());

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
        Label lbl1 = new Label("🔴 " + p1.getName());
        lbl1.getStyleClass().addAll("player-label", "player1-label");
        player1PosLabel.getStyleClass().add("position-label");
        player1PosLabel.setText("Position: 1");

        Label lbl2 = new Label("🔵 " + p2.getName());
        lbl2.getStyleClass().addAll("player-label", "player2-label");
        player2PosLabel.getStyleClass().add("position-label");
        player2PosLabel.setText("Position: 1");

        // 7) Play Again
        Button playAgain = new Button("🔄 Play Again");
        playAgain.getStyleClass().add("play-again");
        playAgain.setOnAction(e -> resetUI());

        // 8) Turn timers
        timerCircle1 = createTimerCircle(diceImageView1);
        timerCircle2 = createTimerCircle(diceImageView2);

        StackPane ds1 = new StackPane(timerCircle1, diceImageView1);
        StackPane ds2 = new StackPane(timerCircle2, diceImageView2);

        VBox box1 = new VBox(10, lbl1, ds1, turnTimerLabel1, player1PosLabel, player1Roll);
        box1.setAlignment(Pos.CENTER);
        box1.getStyleClass().add("player-box");
        box1.setMinWidth(180);

        VBox box2 = new VBox(10, lbl2, ds2, turnTimerLabel2, player2PosLabel, player2Roll);
        box2.setAlignment(Pos.CENTER);
        box2.getStyleClass().add("player-box");
        box2.setMinWidth(180);

        HBox controls = new HBox(20, box2, box1);
        controls.setAlignment(Pos.CENTER);

        statusLabel.getStyleClass().add("status-label");
        diceResult.getStyleClass().add("dice-result");
        gameTimerLabel.getStyleClass().add("timer-label");
        turnTimerLabel1.getStyleClass().add("turn-timer");
        turnTimerLabel2.getStyleClass().add("turn-timer");

        VBox root = new VBox(15,
                gameTimerLabel,
                boardLayer,
                diceResult,
                statusLabel,
                controls,
                playAgain);
        root.setAlignment(Pos.CENTER);
        root.setBackground(Background.EMPTY);

        Scene scene = new Scene(root, 700, 800);
        scene.getStylesheets().add("style.css");
        stage.setTitle("🐍 Snake and Ladder 🪜");
        stage.setScene(scene);
        stage.show();

        // 9) Network
        net = new NetworkClient("localhost", 12345, this::handleServerMessage);

        // —— set up chat UI ——
        chatArea.setEditable(false);
        chatArea.setPrefRowCount(5);
        chatInput.setPromptText("Type a message…");
        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("chat-send");
        sendBtn.setOnAction(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                // prefix with yourColor so clients know who said it
                chatClient.send("CHAT:" + yourColor + ":" + text);
                chatInput.clear();
            }
        });

        // layout it below your game controls
        VBox chatBox = new VBox(5,
                new Label("Chat"),
                chatArea,
                new HBox(5, chatInput, sendBtn));
        chatBox.setMaxWidth(400);
        chatBox.setAlignment(Pos.CENTER);

        // assume your root VBox is called `root`:
        root.getChildren().add(chatBox);

        // —— connect the chat client ——
        try {
            chatClient = new ChatClient(
                    "localhost", // or your server’s IP
                    12346, // ChatServer port
                    msg -> Platform.runLater(() -> {
                        chatArea.appendText(msg + "\n");
                    }));
        } catch (IOException ex) {
            ex.printStackTrace();
            chatArea.appendText("Failed to connect chat\n");
        }

        // 10) Start clocks
        startGameClock();
    }

    private void resetUI() {
        if (celebrationAnimation != null)
            celebrationAnimation.stop();
        gameManager.resetGame();
        previousPlayer1Pos = previousPlayer2Pos = 1;
        player1PosLabel.setText("Position: 1");
        player2PosLabel.setText("Position: 1");
        diceResult.setText("");
        statusLabel.setText("");
        diceImageView1.setImage(new Image(
                getClass().getResource("/dice/red/dice1.png").toExternalForm()));
        diceImageView2.setImage(new Image(
                getClass().getResource("/dice/blue/dice1.png").toExternalForm()));
        player1Roll.setDisable(true);
        player2Roll.setDisable(true);
        resetGameClock();
    }

    private void handleRoll(Player player) {
        if (isRolling)
            return;
        isRolling = true;
        player1Roll.setDisable(true);
        player2Roll.setDisable(true);

        // play dice sound & pick face
        if (diceSound != null)
            diceSound.play();
        int simulatedRoll = random.nextInt(6) + 1;
        String path = player.getColor().equals("red") ? RED_DICE_PATH : BLUE_DICE_PATH;
        String frame = "/" + path + simulatedRoll + ".png";

        Platform.runLater(() -> {
            ImageView dv = player.getColor().equals("red") ? diceImageView1 : diceImageView2;
            dv.setImage(new Image(getClass().getResource(frame).toExternalForm()));
            diceResult.setText(
                    (player.getColor().equals("red") ? "🔴" : "🔵") +
                            " rolled: " + simulatedRoll);
        });

        net.send("ROLL:" + simulatedRoll);
    }

    private void handleServerMessage(String msg) {
        if (msg.startsWith("CHAT:")) {
            // we already broadcast raw "CHAT:color:message"
            // and chatClient does the UI update.
            return;
        }
        Platform.runLater(() -> {
            String[] parts = msg.split(":", 3);
            switch (parts[0]) {
                case "MATCHED":
                    yourColor = parts[1];
                    statusLabel.setText("Matched! You are: " + yourColor);
                    break;
                case "TURN":
                    boolean myTurn = yourColor.equals(parts[1]);
                    player1Roll.setDisable(!(myTurn && "red".equals(yourColor)));
                    player2Roll.setDisable(!(myTurn && "blue".equals(yourColor)));
                    statusLabel.setText("Turn: " + parts[1]);
                    isRolling = false;
                    break;
                case "MOVE":
                    String color = parts[1];
                    int newPos = Integer.parseInt(parts[2]);
                    // animate from previous to new
                    if (color.equals("red")) {
                        animateMovement(gameManager.getPlayer1(),
                                previousPlayer1Pos, newPos, () -> {
                                    previousPlayer1Pos = newPos;
                                    player1PosLabel.setText("Position: " + newPos);
                                });
                    } else {
                        animateMovement(gameManager.getPlayer2(),
                                previousPlayer2Pos, newPos, () -> {
                                    previousPlayer2Pos = newPos;
                                    player2PosLabel.setText("Position: " + newPos);
                                });
                    }
                    break;
                case "WIN":
                    statusLabel.setText(parts[1] + " wins!");
                    player1Roll.setDisable(true);
                    player2Roll.setDisable(true);
                    if (winSound != null)
                        winSound.play();
                    break;
            }
        });
    }

    // --- Animation helpers (same as before) ---

    private void animateMovement(Player player, int start, int end, Runnable onFinished) {
        ImageView token = player.getColor().equals("red")
                ? player1Token
                : player2Token;
        boolean isP1 = player.getColor().equals("red");
        SequentialTransition seq = new SequentialTransition();
        for (int i = 1; i <= end - start; i++) {
            int step = start + i;
            seq.getChildren().add(
                    createStepTransition(token, step, isP1, i == end - start));
            if (i < end - start)
                seq.getChildren().add(
                        new PauseTransition(Duration.millis(100)));
        }
        seq.setOnFinished(e -> {
            Integer top = gameManager.getBoard().getLadders().get(end);
            if (top != null) {
                TranslateTransition climb = createStraightLineTransition(token, end, top);
                climb.setOnFinished(ev -> onFinished.run());
                climb.play();
            } else {
                onFinished.run();
            }
        });
        seq.play();
        if (moveSound != null)
            moveSound.play();
    }

    private TranslateTransition createStepTransition(
            ImageView token, int pos, boolean isP1, boolean last) {
        int size = 60;
        int col = (pos - 1) % 10, row = (pos - 1) / 10;
        if (row % 2 == 1)
            col = 9 - col;
        double cx = col * size + size / 2.0, cy = (9 - row) * size + size / 2.0;
        double off = 15, x = (isP1 ? cx - off : cx + off), y = cy;
        double tx = x - token.getFitWidth() / 2, ty = y - token.getFitHeight() / 2;
        TranslateTransition t = new TranslateTransition(
                Duration.millis(150), token);
        t.setToX(tx);
        t.setToY(ty);
        if (last) {
            t.setOnFinished(evt -> {
                ScaleTransition b = new ScaleTransition(
                        Duration.millis(100), token);
                b.setToX(1.2);
                b.setToY(1.2);
                b.setAutoReverse(true);
                b.setCycleCount(2);
                b.play();
            });
        }
        return t;
    }

    private TranslateTransition createStraightLineTransition(
            ImageView token, int from, int to) {
        int size = 60;
        int fcol = (from - 1) % 10, frow = (from - 1) / 10;
        int tcol = (to - 1) % 10, trow = (to - 1) / 10;
        if (frow % 2 == 1)
            fcol = 9 - fcol;
        if (trow % 2 == 1)
            tcol = 9 - tcol;
        double fx = fcol * size + size / 2.0, fy = (9 - frow) * size + size / 2.0;
        double tx = tcol * size + size / 2.0, ty = (9 - trow) * size + size / 2.0;
        double off = 12;
        if (token == player1Token) {
            fx -= off;
            tx -= off;
        } else {
            fx += off;
            tx += off;
        }
        token.setTranslateX(fx - token.getFitWidth() / 2);
        token.setTranslateY(fy - token.getFitHeight() / 2);
        TranslateTransition climb = new TranslateTransition(
                Duration.millis(600), token);
        climb.setToX(tx - token.getFitWidth() / 2);
        climb.setToY(ty - token.getFitHeight() / 2);
        return climb;
    }

    private Circle createTimerCircle(ImageView dice) {
        Circle c = new Circle(40);
        c.setStroke(Color.DARKGOLDENROD);
        c.setStrokeWidth(4);
        c.setFill(Color.TRANSPARENT);
        c.setStrokeLineCap(StrokeLineCap.ROUND);
        ObservableList<Double> d = c.getStrokeDashArray();
        d.setAll(1000d, 0d);
        c.setCenterX(dice.getFitWidth() / 2);
        c.setCenterY(dice.getFitHeight() / 2);
        return c;
    }

    private void startGameClock() {
        gameTimer = new Timeline(new KeyFrame(
                Duration.seconds(1), e -> {
                    remainingSeconds--;
                    int m = remainingSeconds / 60, s = remainingSeconds % 60;
                    gameTimerLabel.setText(
                            "⏱ " + String.format("%02d:%02d", m, s));
                    if (remainingSeconds <= 0) {
                        gameTimer.stop();
                        statusLabel.setText("⏰ Time's up! Game Over.");
                    }
                }));
        gameTimer.setCycleCount(Animation.INDEFINITE);
        gameTimer.play();
    }

    private void resetGameClock() {
        if (gameTimer != null)
            gameTimer.stop();
        remainingSeconds = 30 * 60;
        startGameClock();
    }
}
