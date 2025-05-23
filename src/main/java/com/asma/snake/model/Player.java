package com.asma.snake.model;

public class Player {

    private final String name; // Player's name
    private final String color; // Player's color (red or blue)
    private int position; // Current position on the board

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.position = 1; // Start at position 1
    }

    public String getName() {
        return name; // Get player name
    }

    public String getColor() {
        return color; // Get player color
    }

    public int getPosition() {
        return position; // Get current position
    }

    public void moveTo(int newPosition) {
        this.position = newPosition; // Move to specific position
    }

    public void moveForward(int steps) {
        this.position += steps; // Move forward by steps (currently unused)
    }

    public boolean hasWon() {
        return this.position == 100; // Win condition check
    }
}
