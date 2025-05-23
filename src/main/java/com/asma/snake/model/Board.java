package com.asma.snake.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the game board with all snake and ladder positions.
 * Uses two hash maps to store snake heads and ladder bottoms.
 */
public class Board {

    private final Map<Integer, Integer> snakes;
    private final Map<Integer, Integer> ladders;

    /**
     * Initializes the board by setting up snakes and ladders.
     */
    public Board() {
        snakes = new HashMap<>();
        ladders = new HashMap<>();
        setupBoard();
    }

    /**
     * Sets up the positions for snakes and ladders on the board.
     * Format: key = start position, value = end position.
     */
    private void setupBoard() {
        // Snakes: head -> tail
        snakes.put(46, 14);
        snakes.put(51, 29);
        snakes.put(60, 22);
        snakes.put(74, 49);
        snakes.put(97, 78);

        // Ladders: bottom -> top
        ladders.put(4, 38);
        ladders.put(12, 32);
        ladders.put(36, 75);
        ladders.put(59, 63);
        ladders.put(69, 88);
    }

    /**
     * Checks the given position for a snake or ladder.
     *
     * @param position the player's current position
     * @return the new position after applying snake/ladder logic
     */
    public int checkPosition(int position) {
        if (snakes.containsKey(position)) {
            return snakes.get(position);
        }
        if (ladders.containsKey(position)) {
            return ladders.get(position);
        }
        return position;
    }

    /**
     * @return the map of snake positions.
     */
    public Map<Integer, Integer> getSnakes() {
        return snakes;
    }

    /**
     * @return the map of ladder positions.
     */
    public Map<Integer, Integer> getLadders() {
        return ladders;
    }
}
