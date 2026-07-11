package com.rewind.history;

import com.rewind.action.InventoryAction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * Bounded LIFO history for inventory actions.
 */
public final class InventoryHistory {
    public static final int DEFAULT_MAX_SIZE = 100;

    private final int maxSize;
    private final Deque<InventoryAction> actions;

    public InventoryHistory() {
        this(DEFAULT_MAX_SIZE);
    }

    public InventoryHistory(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be at least 1");
        }
        this.maxSize = maxSize;
        this.actions = new ArrayDeque<>(maxSize);
    }

    /**
     * Records an action and discards the oldest entry if the history is full.
     *
     * @param action action to push onto the history
     */
    public void push(InventoryAction action) {
        Objects.requireNonNull(action, "action");
        if (actions.size() == maxSize) {
            actions.removeLast();
        }
        actions.addFirst(action);
    }

    /**
     * Returns the next action that would be undone.
     *
     * @return latest action, if present
     */
    public Optional<InventoryAction> peek() {
        return Optional.ofNullable(actions.peekFirst());
    }

    /**
     * Removes and returns the latest action.
     *
     * @return latest action, if present
     */
    public Optional<InventoryAction> pop() {
        return Optional.ofNullable(actions.pollFirst());
    }

    public int size() {
        return actions.size();
    }

    public int maxSize() {
        return maxSize;
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public void clear() {
        actions.clear();
    }
}
