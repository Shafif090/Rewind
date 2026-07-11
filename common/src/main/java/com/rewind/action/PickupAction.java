package com.rewind.action;

import com.rewind.capture.SlotDelta;

import java.util.List;

/**
 * Reverses the inventory-side effect of picking up an item.
 */
public final class PickupAction extends StrictSlotRestoringAction {
    public PickupAction(List<SlotDelta> deltas, String description) {
        super(deltas, description);
        ActionDeltaRules.requirePickup(deltas);
    }
}
