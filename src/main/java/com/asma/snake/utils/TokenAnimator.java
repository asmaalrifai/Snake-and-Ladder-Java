    package com.asma.snake.utils;

import com.asma.snake.logic.GameManager;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Animates token movement and logs each step for debugging.
 */
public class TokenAnimator {

    private final GameManager gameManager;

    public TokenAnimator(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void animateMovement(ImageView token,
                                int startPos,
                                int finalPos,
                                boolean isPlayer1,
                                Runnable onFinished) {
        System.out.println("[DEBUG] animateMovement from " + startPos + " to " + finalPos);
        int steps = finalPos - startPos;
        if (steps <= 0) {
            System.out.println("[DEBUG] No movement needed");
            onFinished.run();
            return;
        }

        SequentialTransition walkSteps = new SequentialTransition();

        for (int i = 1; i <= steps; i++) {
            int step = startPos + i;
            TranslateTransition stepAnim = createStepTransition(token, step, isPlayer1, i == steps);
            walkSteps.getChildren().add(stepAnim);
            if (i < steps) {
                walkSteps.getChildren().add(new PauseTransition(Duration.millis(100)));
            }
        }

        walkSteps.setOnFinished(evt -> {
            System.out.println("[DEBUG] Primary walkSteps finished at pos=" + finalPos);
            Integer ladderTop = gameManager.getBoard().getLadders().get(finalPos);
            if (ladderTop != null) {
                TranslateTransition climb = createStraightLineTransition(token, finalPos, ladderTop, isPlayer1);
                climb.setOnFinished(e -> onFinished.run());
                climb.play();
            } else {
                onFinished.run();
            }
        });

        walkSteps.play();
    }

    private TranslateTransition createStepTransition(ImageView token,
                                                      int position,
                                                      boolean isPlayer1,
                                                      boolean isLastStep) {
        // compute target coordinates
        int cellSize = 60;
        int col = (position - 1) % 10;
        int row = (position - 1) / 10;
        if (row % 2 == 1) col = 9 - col;

        double centerX = col * cellSize + cellSize / 2.0;
        double centerY = (9 - row) * cellSize + cellSize / 2.0;
        double offset = isPlayer1 ? -12 : +12;

        double targetX = centerX + offset - token.getFitWidth() / 2;
        double targetY = centerY      - token.getFitHeight() / 2;

        System.out.println("[DEBUG] StepTransition to (" + targetX + "," + targetY + ")");

        TranslateTransition transition = new TranslateTransition(Duration.millis(150), token);
        transition.setToX(targetX);
        transition.setToY(targetY);

        if (isLastStep) {
            transition.setOnFinished(e -> {
                System.out.println("[DEBUG] Last step reached at (" + token.getTranslateX() + "," + token.getTranslateY() + ")");
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

    private TranslateTransition createStraightLineTransition(ImageView token,
                                                             int fromPos,
                                                             int toPos,
                                                             boolean isPlayer1) {
        // similar logging can be added here if needed
        int cellSize = 60;
        int fromCol = (fromPos - 1) % 10;
        int fromRow = (fromPos - 1) / 10;
        if (fromRow % 2 == 1) fromCol = 9 - fromCol;

        int toCol = (toPos - 1) % 10;
        int toRow = (toPos - 1) / 10;
        if (toRow % 2 == 1) toCol = 9 - toCol;

        double offset = isPlayer1 ? -12 : +12;
        double fromX = fromCol * cellSize + cellSize / 2.0 + offset;
        double fromY = (9 - fromRow) * cellSize + cellSize / 2.0;
        double toX   = toCol   * cellSize + cellSize / 2.0 + offset;
        double toY   = (9 - toRow)   * cellSize + cellSize / 2.0;

        token.setTranslateX(fromX - token.getFitWidth()/2);
        token.setTranslateY(fromY - token.getFitHeight()/2);

        TranslateTransition climb = new TranslateTransition(Duration.millis(600), token);
        climb.setToX(toX - token.getFitWidth()/2);
        climb.setToY(toY - token.getFitHeight()/2);
        return climb;
    }
}
    
