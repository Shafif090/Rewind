package com.rewind.capture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Records the small set of slots touched by one inventory transaction.
 */
public final class InventoryActionRecorder {
    private final Map<Integer, SlotState> beforeSlots = new LinkedHashMap<>();
    private final Map<Integer, SlotState> afterSlots = new LinkedHashMap<>();

    public void recordBefore(SlotState slot) {
        beforeSlots.put(slot.slotIndex(), slot);
    }

    public void recordAfter(SlotState slot) {
        afterSlots.put(slot.slotIndex(), slot);
    }

    /**
     * Detects the action represented by the recorded before/after slot states.
     *
     * @param hint loader-provided interaction hint
     * @return detected action or a strict rejection reason
     */
    public ActionDetectionResult complete(InventoryActionHint hint) {
        Objects.requireNonNull(hint, "hint");
        return InventoryTransactionDetector.detect(beforeSlots.values(), afterSlots.values(), hint);
    }

    public void clear() {
        beforeSlots.clear();
        afterSlots.clear();
    }
}
