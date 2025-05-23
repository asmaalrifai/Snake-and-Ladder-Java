package com.asma.snake.model;

import java.util.HashMap;
import java.util.Map;

public class Board {

    private final Map<Integer, Integer> snakes; // Stores snake head → tail
    private final Map<Integer, Integer> ladders; // Stores ladder bottom → top

    public Board() {
        snakes = new HashMap<>();
        ladders = new HashMap<>();
        setupBoard(); // Initialize snake and ladder positions
    }

    private void setupBoard() {
        // Snakes: head → tail
        snakes.put(46, 14);
        snakes.put(51, 29);
        snakes.put(60, 22);
        snakes.put(74, 49);
        snakes.put(97, 78);

        // Ladders: bottom → top
        ladders.put(4, 38);
        ladders.put(12, 32);
        ladders.put(36, 75);
        ladders.put(59, 63);
        ladders.put(69, 88);
    }

    public int checkPosition(int position) {
        if (snakes.containsKey(position)) {
            return snakes.get(position); // Landed on a snake
        }
        if (ladders.containsKey(position)) {
            return ladders.get(position); // Landed on a ladder
        }
        return position; // No snake or ladder
    }

    public Map<Integer, Integer> getSnakes() {
        return snakes; // Return snake map
    }

    public Map<Integer, Integer> getLadders() {
        return ladders; // Return ladder map
    }
}
