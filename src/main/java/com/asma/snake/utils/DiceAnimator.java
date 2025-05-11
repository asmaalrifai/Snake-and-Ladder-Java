package com.asma.snake.utils;

import java.util.Random;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DiceAnimator {
    private final Random random = new Random();

    public void rollDiceAnimation(ImageView diceView, String dicePath, Consumer<Integer> onFinished) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    int tempRoll = random.nextInt(6) + 1;
                    String frame = "/" + dicePath + tempRoll + ".png";
                    Platform.runLater(() -> diceView.setImage(new Image(getClass().getResource(frame).toExternalForm())));
                    Thread.sleep(50);
                }
                int result = random.nextInt(6) + 1;
                String finalFrame = "/" + dicePath + result + ".png";
                Platform.runLater(() -> {
                    diceView.setImage(new Image(getClass().getResource(finalFrame).toExternalForm()));
                    onFinished.accept(result);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

