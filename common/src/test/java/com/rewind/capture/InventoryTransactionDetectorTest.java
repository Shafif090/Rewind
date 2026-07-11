package com.rewind.capture;

import com.rewind.action.RecordedInventoryAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InventoryTransactionDetectorTest {
    private static final StackFingerprint DIAMOND = StackFingerprint.of("minecraft:diamond", "{}");
    private static final StackFingerprint STONE = StackFingerprint.of("minecraft:stone", "{}");

    @Test
    void detectsMoveBetweenSlots() {
        ActionDetectionResult result = detect(
            List.of(slot(0, DIAMOND, 10), empty(1)),
            List.of(slot(0, DIAMOND, 6), slot(1, DIAMOND, 4)),
            InventoryActionHint.MOVE
        );

        RecordedInventoryAction action = action(result);
        assertEquals(InventoryActionKind.MOVE, action.kind());
        assertEquals("Moved Stack", action.description());
    }

    @Test
    void detectsSlotSwap() {
        ActionDetectionResult result = detect(
            List.of(slot(0, DIAMOND, 3), slot(1, STONE, 8)),
            List.of(slot(0, STONE, 8), slot(1, DIAMOND, 3)),
            InventoryActionHint.SWAP
        );

        assertEquals(InventoryActionKind.SWAP, action(result).kind());
    }

    @Test
    void detectsHotbarSwapFromHint() {
        ActionDetectionResult result = detect(
            List.of(slot(0, DIAMOND, 3), slot(9, STONE, 8)),
            List.of(slot(0, STONE, 8), slot(9, DIAMOND, 3)),
            InventoryActionHint.HOTBAR_SWAP
        );

        assertEquals(InventoryActionKind.HOTBAR_SWAP, action(result).kind());
    }

    @Test
    void detectsShiftClickSpreadAcrossSlots() {
        ActionDetectionResult result = detect(
            List.of(slot(5, DIAMOND, 12), slot(10, DIAMOND, 32), empty(11)),
            List.of(empty(5), slot(10, DIAMOND, 40), slot(11, DIAMOND, 4)),
            InventoryActionHint.SHIFT_CLICK
        );

        assertEquals(InventoryActionKind.SHIFT_CLICK, action(result).kind());
    }

    @Test
    void detectsDropAsInventoryLoss() {
        ActionDetectionResult result = detect(
            List.of(slot(0, DIAMOND, 2)),
            List.of(slot(0, DIAMOND, 1)),
            InventoryActionHint.DROP
        );

        assertEquals(InventoryActionKind.DROP, action(result).kind());
    }

    @Test
    void detectsPickupAsInventoryGain() {
        ActionDetectionResult result = detect(
            List.of(empty(0)),
            List.of(slot(0, DIAMOND, 1)),
            InventoryActionHint.PICKUP
        );

        assertEquals(InventoryActionKind.PICKUP, action(result).kind());
    }

    @Test
    void detectsSortWhenItemsAreRedistributedWithoutLoss() {
        ActionDetectionResult result = detect(
            List.of(slot(0, DIAMOND, 2), slot(1, STONE, 8), empty(2)),
            List.of(empty(0), slot(1, DIAMOND, 2), slot(2, STONE, 8)),
            InventoryActionHint.SORT
        );

        assertEquals(InventoryActionKind.SORT, action(result).kind());
    }

    @Test
    void rejectsUnexpectedUnbalancedChange() {
        ActionDetectionResult result = detect(
            List.of(slot(0, DIAMOND, 4)),
            List.of(slot(0, STONE, 4)),
            InventoryActionHint.UNKNOWN
        );

        assertFalse(result.isDetected());
        assertTrue(result.reason().contains("unexpected"));
    }

    @Test
    void recorderBuildsActionFromTouchedSlotsOnly() {
        InventoryActionRecorder recorder = new InventoryActionRecorder();
        recorder.recordBefore(slot(4, DIAMOND, 7));
        recorder.recordBefore(empty(8));
        recorder.recordAfter(slot(4, DIAMOND, 2));
        recorder.recordAfter(slot(8, DIAMOND, 5));

        assertEquals(InventoryActionKind.MOVE, action(recorder.complete(InventoryActionHint.MOVE)).kind());
    }

    private static ActionDetectionResult detect(List<SlotState> before, List<SlotState> after, InventoryActionHint hint) {
        return InventoryTransactionDetector.detect(before, after, hint);
    }

    private static RecordedInventoryAction action(ActionDetectionResult result) {
        assertTrue(result.isDetected(), result.reason());
        return assertInstanceOf(RecordedInventoryAction.class, result.action().orElseThrow());
    }

    private static SlotState slot(int slot, StackFingerprint fingerprint, int count) {
        return SlotState.of(slot, fingerprint, count);
    }

    private static SlotState empty(int slot) {
        return SlotState.empty(slot);
    }
}
