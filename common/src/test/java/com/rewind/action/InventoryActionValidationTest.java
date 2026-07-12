package com.rewind.action;

import com.rewind.capture.SlotDelta;
import com.rewind.capture.SlotState;
import com.rewind.capture.StackFingerprint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class InventoryActionValidationTest {
    private static final StackFingerprint DIAMOND = StackFingerprint.of("minecraft:diamond", "{}");
    private static final StackFingerprint STONE = StackFingerprint.of("minecraft:stone", "{}");

    @Test
    void moveRequiresOneBalancedLossAndGain() {
        assertThrows(IllegalArgumentException.class, () -> new MoveAction(List.of(
            delta(slot(0, DIAMOND, 3), slot(0, DIAMOND, 1)),
            delta(empty(1), slot(1, DIAMOND, 1))
        ), "Moved Stack"));
    }

    @Test
    void swapRequiresTwoSlotsThatExchangeStates() {
        assertThrows(IllegalArgumentException.class, () -> new SwapAction(List.of(
            delta(slot(0, DIAMOND, 1), slot(0, STONE, 1)),
            delta(slot(1, STONE, 2), slot(1, DIAMOND, 1))
        ), "Swapped Slots"));
    }

    @Test
    void shiftClickRequiresOneLossAndMatchingGains() {
        assertThrows(IllegalArgumentException.class, () -> new ShiftClickAction(List.of(
            delta(slot(0, DIAMOND, 2), empty(0)),
            delta(empty(1), slot(1, STONE, 2))
        ), "Shift-click Transfer"));
    }

    @Test
    void hotbarSwapUsesSwapValidationRules() {
        assertThrows(IllegalArgumentException.class, () -> new HotbarSwapAction(List.of(
            delta(slot(0, DIAMOND, 1), slot(0, STONE, 1))
        ), "Hotbar Swap"));
    }

    @Test
    void pickupCannotContainLosses() {
        assertThrows(IllegalArgumentException.class, () -> new PickupAction(List.of(
            delta(slot(0, DIAMOND, 1), empty(0))
        ), "Picked Up Item"));
    }

    @Test
    void dropCannotContainGains() {
        assertThrows(IllegalArgumentException.class, () -> new DropAction(List.of(
            delta(empty(0), slot(0, DIAMOND, 1))
        ), "Dropped Item", UUID.randomUUID()));
    }

    private static SlotDelta delta(SlotState before, SlotState after) {
        return new SlotDelta(before.slotIndex(), before, after);
    }

    private static SlotState slot(int slotIndex, StackFingerprint fingerprint, int count) {
        return SlotState.of(slotIndex, fingerprint, count);
    }

    private static SlotState empty(int slotIndex) {
        return SlotState.empty(slotIndex);
    }
}
