package com.asma.snake.utils;

import javafx.scene.media.AudioClip;

public class SoundManager {
    private final AudioClip moveSound;
    private final AudioClip diceSound;
    private final AudioClip snakeSound;
    private final AudioClip ladderSound;
    private final AudioClip winSound;

    public SoundManager() {
        moveSound = load("/sounds/move.wav");
        diceSound = load("/sounds/Dice.wav");
        snakeSound = load("/sounds/snake.wav");
        ladderSound = load("/sounds/ladder.wav");
        winSound = load("/sounds/win.wav");
    }

    private AudioClip load(String path) {
        return new AudioClip(getClass().getResource(path).toExternalForm());
    }

    public void playMove() { moveSound.play(); }
    public void playDice() { diceSound.play(); }
    public void playSnake() { snakeSound.play(); }
    public void playLadder() { ladderSound.play(); }
    public void playWin() { winSound.play(); }
}

