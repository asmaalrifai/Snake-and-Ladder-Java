package com.asma.snake;

import com.asma.snake.client.GameUI;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for launching the Snake and Ladder client UI.
 */
public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        new GameUI(primaryStage); // Launch the UI
    }

    public static void main(String[] args) {
        launch(args);
    }
}
