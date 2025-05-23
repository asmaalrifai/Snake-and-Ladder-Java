package com.asma.snake.model;

/**
 * Represents a player in the Snake and Ladder game.
 * Stores the player's name, color, and current position.
 */
public class Player {

    private final String name;
    private final String color;
    private int position;

    /**
     * Constructs a new player with a name and color.
     * All players start at position 1.
     *
     * @param name  the name of the player
     * @param color the color of the player (e.g., "red" or "blue")
     */
    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.position = 1;
    }

    /**
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the player's color
     */
    public String getColor() {
        return color;
    }

    /**
     * @return the player's current board position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Moves the player directly to a specified position.
     *
     * @param newPosition the target position
     */
    public void moveTo(int newPosition) {
        this.position = newPosition;
    }

    /**
     * Moves the player forward by a number of steps.
     * (Note: This method is unused in current logic, but can be useful for manual advancement.)
     *
     * @param steps number of steps to move forward
     */
    public void moveForward(int steps) {
        this.position += steps;
    }

    /**
     * Checks whether the player has reached the final square.
     *
     * @return true if position is exactly 100
     */
    public boolean hasWon() {
        return this.position == 100;
    }
}
