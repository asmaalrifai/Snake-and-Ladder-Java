package com.asma.snake.model;

import java.util.Random;

/**
 * Represents a 6-sided dice used in the Snake and Ladder game.
 * Encapsulates random dice rolling logic.
 */
public class Dice {

    private final Random random;

    /**
     * Initializes the dice with a random number generator.
     */
    public Dice() {
        this.random = new Random();
    }

    /**
     * Rolls the dice to generate a random number between 1 and 6.
     *
     * @return a number between 1 and 6 (inclusive)
     */
    public int roll() {
        return random.nextInt(6) + 1;
    }
}
