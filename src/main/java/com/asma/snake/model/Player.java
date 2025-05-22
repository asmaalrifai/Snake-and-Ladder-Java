package com.asma.snake.model;

public class Player {
    private final String name;
    private final String color;
    private int position;
    private boolean hasStarted; // <--- this is the missing part

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.position = 1;
        this.hasStarted = false; // not started until rolling a 6
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

    // ✅ ADD THESE:

    public boolean hasStarted() {
        return hasStarted;
    }

    public void setStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
    }
}
