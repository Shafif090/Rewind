package com.rewind.action;

import com.rewind.capture.SlotDelta;

import java.util.List;

/**
 * Reverses a drag/click move between two inventory slots.
 */
public final class MoveAction extends StrictSlotRestoringAction {
    public MoveAction(List<SlotDelta> deltas, String description) {
        super(deltas, description);
        ActionDeltaRules.requireMove(deltas);
    }
}
