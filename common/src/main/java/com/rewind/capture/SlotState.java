package com.rewind.capture;

import java.util.Objects;

/**
 * Compact state of one inventory slot at one point in time.
 */
public record SlotState(int slotIndex, StackFingerprint stack, int count) {
    public SlotState {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be non-negative");
        }
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) {
            count = 0;
        } else if (count < 1) {
            throw new IllegalArgumentException("non-empty slot count must be at least 1");
        }
    }

    public static SlotState empty(int slotIndex) {
        return new SlotState(slotIndex, StackFingerprint.empty(), 0);
    }

    public static SlotState of(int slotIndex, StackFingerprint stack, int count) {
        return new SlotState(slotIndex, stack, count);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean sameStackIdentity(SlotState other) {
        return stack.equals(other.stack);
    }

    public boolean sameStackAndCount(SlotState other) {
        return sameStackIdentity(other) && count == other.count;
    }
}
