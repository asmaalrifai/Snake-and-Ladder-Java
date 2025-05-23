package com.asma.snake.model;

import java.util.Random;

public class Dice {

    private final Random random; // Random number generator for dice roll

    public Dice() {
        this.random = new Random(); // Initialize the random generator
    }

    public int roll() {
        return random.nextInt(6) + 1; // Return a number between 1 and 6
    }
}
