package com.asma.snake.model;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final Map<Integer, Integer> snakes;
    private final Map<Integer, Integer> ladders;

    public Board() {
        snakes = new HashMap<>();
        ladders = new HashMap<>();
        setupBoard();
    }

    private void setupBoard() {
        // 🐍 Snakes
        snakes.put(46, 14);
        snakes.put(51, 29);
        snakes.put(60, 22);
        snakes.put(74, 49);
        snakes.put(97, 78);
    
        // 🪜 Ladders
        ladders.put(4, 38);
        ladders.put(36, 75);
        ladders.put(69, 88);
        ladders.put(59, 63);
        ladders.put(12, 32);
    }
    
    public int checkPosition(int position) {
        if (snakes.containsKey(position)) {
            return snakes.get(position);
        }
        if (ladders.containsKey(position)) {
            return ladders.get(position);
        }
        return position;
    }
    
    // Add these getter methods
    public Map<Integer, Integer> getSnakes() {
        return snakes;
    }
    
    public Map<Integer, Integer> getLadders() {
        return ladders;
    }
}