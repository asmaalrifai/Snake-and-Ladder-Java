package com.asma.snake.model;

public class Player {
    private final String name;
    private final String color;
    private int position;

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.position = 1;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getPosition() {
        return position;
    }

    public void moveTo(int newPosition) {
        this.position = newPosition;
    }

    public void moveForward(int steps) {
        this.position += steps;
    }

    public boolean hasWon() {
        return this.position == 100;
    }
}
