package com.rewind.capture;

import java.util.Objects;

/**
 * Before/after state for one changed inventory slot.
 */
public record SlotDelta(int slotIndex, SlotState before, SlotState after) {
    public SlotDelta {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be non-negative");
        }
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
        if (before.slotIndex() != slotIndex || after.slotIndex() != slotIndex) {
            throw new IllegalArgumentException("slot index must match before and after states");
        }
    }

    public boolean isLoss() {
        return !before.isEmpty()
            && (after.isEmpty() || before.sameStackIdentity(after) && after.count() < before.count());
    }

    public boolean isGain() {
        return !after.isEmpty()
            && (before.isEmpty() || before.sameStackIdentity(after) && after.count() > before.count());
    }

    public int lostCount() {
        return isLoss() ? before.count() - (before.sameStackIdentity(after) ? after.count() : 0) : 0;
    }

    public int gainedCount() {
        return isGain() ? after.count() - (before.sameStackIdentity(after) ? before.count() : 0) : 0;
    }
}
