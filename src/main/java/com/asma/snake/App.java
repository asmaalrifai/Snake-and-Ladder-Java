package com.asma.snake;

import com.asma.snake.client.GameUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        new GameUI(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
