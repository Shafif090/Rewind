package com.rewind.action;

import com.rewind.capture.SlotDelta;

import java.util.List;

/**
 * Reverses a shift-click transfer from one slot into one or more destination slots.
 */
public final class ShiftClickAction extends StrictSlotRestoringAction {
    public ShiftClickAction(List<SlotDelta> deltas, String description) {
        super(deltas, description);
        ActionDeltaRules.requireShiftClick(deltas);
    }
}
