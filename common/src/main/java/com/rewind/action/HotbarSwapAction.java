package com.rewind.action;

import com.rewind.capture.SlotDelta;

import java.util.List;

/**
 * Reverses a number-key hotbar swap.
 */
public final class HotbarSwapAction extends StrictSlotRestoringAction {
    public HotbarSwapAction(List<SlotDelta> deltas, String description) {
        super(deltas, description);
        ActionDeltaRules.requireSwap(deltas, "hotbar swap");
    }
}
