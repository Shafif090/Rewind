package com.rewind.action;

import com.rewind.capture.SlotDelta;

import java.util.List;

/**
 * Reverses a direct swap between two inventory slots.
 */
public final class SwapAction extends StrictSlotRestoringAction {
    public SwapAction(List<SlotDelta> deltas, String description) {
        super(deltas, description);
        ActionDeltaRules.requireSwap(deltas, "swap");
    }
}
