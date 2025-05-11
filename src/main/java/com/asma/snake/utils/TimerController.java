package com.asma.snake.utils;

import com.asma.snake.model.Player;
import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class TimerController {
    private Timeline gameTimer;
    private Timeline turnTimer;
    private int remainingSeconds = 1800;

    public void startGameTimer(Label label, Runnable onTimeout) {
        stopGameTimer();
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            label.setText("⏱ " + String.format("%02d:%02d", minutes, seconds));

            if (remainingSeconds <= 0) {
                gameTimer.stop();
                onTimeout.run();
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    public void stopGameTimer() {
        if (gameTimer != null) gameTimer.stop();
    }

    public void startTurnTimer(Player player, Label label, Circle circle, Runnable onTimeout) {
        stopTurnTimer();
        final int[] countdown = {5};

        // Reset visuals
        label.setText("⏳ 5s");
        updateTimerCircle(circle, 1.0);

        turnTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown[0]--;
            label.setText("⏳ " + countdown[0] + "s");
            updateTimerCircle(circle, countdown[0] / 5.0);

            if (countdown[0] <= 0) {
                stopTurnTimer();
                onTimeout.run();
            }
        }));
        turnTimer.setCycleCount(5);
        turnTimer.play();
    }

    public void stopTurnTimer() {
        if (turnTimer != null) turnTimer.stop();
    }

    private void updateTimerCircle(Circle circle, double progress) {
        double circumference = 2 * Math.PI * circle.getRadius();
        double visible = progress * circumference;
        double invisible = circumference - visible;

        circle.getStrokeDashArray().setAll(visible, invisible);

        if (progress < 0.2) {
            circle.setStroke(Color.RED);
        } else if (progress < 0.5) {
            circle.setStroke(Color.ORANGE);
        } else {
            circle.setStroke(Color.DARKGOLDENROD);
        }
    }
}
