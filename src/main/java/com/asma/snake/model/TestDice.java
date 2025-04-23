package com.asma.snake.model;

public class TestDice {
    private final int[] predefinedRolls = {6, 3, 6, 6, 6, 6, 6, 6, 3};
    private int index = 0;

    public int roll() {
        if (index < predefinedRolls.length) {
            return predefinedRolls[index++];
        } else {
            return 1; // Default roll after test sequence
        }
    }
}
