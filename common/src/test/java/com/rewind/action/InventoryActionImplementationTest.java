package com.rewind.action;

import com.rewind.capture.SlotDelta;
import com.rewind.capture.SlotState;
import com.rewind.capture.StackFingerprint;
import com.rewind.platform.EntityLookup;
import com.rewind.platform.InventoryAccess;
import com.rewind.platform.PlayerMessenger;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InventoryActionImplementationTest {
    private static final StackFingerprint DIAMOND = StackFingerprint.of("minecraft:diamond", "{}");
    private static final StackFingerprint STONE = StackFingerprint.of("minecraft:stone", "{}");

    @Test
    void moveUndoRestoresSourceAndDestinationSlots() {
        FakeInventory inventory = inventory(
            stack(0, DIAMOND, 6),
            stack(1, DIAMOND, 4)
        );
        MoveAction action = new MoveAction(List.of(
            delta(slot(0, DIAMOND, 10), slot(0, DIAMOND, 6)),
            delta(empty(1), slot(1, DIAMOND, 4))
        ), "Moved Stack");

        UndoResult result = action.undo(context(inventory));

        assertEquals(UndoResult.Status.SUCCESS, result.status());
        assertSlot(inventory, slot(0, DIAMOND, 10));
        assertSlot(inventory, empty(1));
    }

    @Test
    void swapUndoRestoresBothSlots() {
        FakeInventory inventory = inventory(
            stack(0, STONE, 8),
            stack(1, DIAMOND, 3)
        );
        SwapAction action = new SwapAction(List.of(
            delta(slot(0, DIAMOND, 3), slot(0, STONE, 8)),
            delta(slot(1, STONE, 8), slot(1, DIAMOND, 3))
        ), "Swapped Slots");

        assertTrue(action.canUndo(context(inventory)));
        assertEquals(UndoResult.Status.SUCCESS, action.undo(context(inventory)).status());
        assertSlot(inventory, slot(0, DIAMOND, 3));
        assertSlot(inventory, slot(1, STONE, 8));
    }

    @Test
    void shiftClickUndoRestoresSpreadTransfer() {
        FakeInventory inventory = inventory(
            emptyTestStack(5),
            stack(10, DIAMOND, 40),
            stack(11, DIAMOND, 4)
        );
        ShiftClickAction action = new ShiftClickAction(List.of(
            delta(slot(5, DIAMOND, 12), empty(5)),
            delta(slot(10, DIAMOND, 32), slot(10, DIAMOND, 40)),
            delta(empty(11), slot(11, DIAMOND, 4))
        ), "Shift-click Transfer");

        assertEquals(UndoResult.Status.SUCCESS, action.undo(context(inventory)).status());
        assertSlot(inventory, slot(5, DIAMOND, 12));
        assertSlot(inventory, slot(10, DIAMOND, 32));
        assertSlot(inventory, empty(11));
    }

    @Test
    void hotbarSwapUndoRestoresSlots() {
        FakeInventory inventory = inventory(
            stack(0, STONE, 8),
            stack(9, DIAMOND, 3)
        );
        HotbarSwapAction action = new HotbarSwapAction(List.of(
            delta(slot(0, DIAMOND, 3), slot(0, STONE, 8)),
            delta(slot(9, STONE, 8), slot(9, DIAMOND, 3))
        ), "Hotbar Swap");

        assertEquals(UndoResult.Status.SUCCESS, action.undo(context(inventory)).status());
        assertSlot(inventory, slot(0, DIAMOND, 3));
        assertSlot(inventory, slot(9, STONE, 8));
    }

    @Test
    void pickupUndoClearsGainedSlots() {
        FakeInventory inventory = inventory(stack(0, DIAMOND, 1));
        PickupAction action = new PickupAction(List.of(
            delta(empty(0), slot(0, DIAMOND, 1))
        ), "Picked Up Item");

        assertEquals(UndoResult.Status.SUCCESS, action.undo(context(inventory)).status());
        assertSlot(inventory, empty(0));
    }

    @Test
    void changedWorldRefusesWithoutMutatingInventory() {
        FakeInventory inventory = inventory(
            stack(0, STONE, 6),
            stack(1, DIAMOND, 4)
        );
        MoveAction action = new MoveAction(List.of(
            delta(slot(0, DIAMOND, 10), slot(0, DIAMOND, 6)),
            delta(empty(1), slot(1, DIAMOND, 4))
        ), "Moved Stack");

        assertFalse(action.canUndo(context(inventory)));
        assertEquals(UndoResult.refusedWorldChanged(), action.undo(context(inventory)));
        assertSlot(inventory, slot(0, STONE, 6));
        assertSlot(inventory, slot(1, DIAMOND, 4));
    }

    @Test
    void dropActionRefusesUntilDroppedEntityValidationExists() {
        FakeInventory inventory = inventory(stack(0, DIAMOND, 1));
        DropAction action = new DropAction(List.of(
            delta(slot(0, DIAMOND, 2), slot(0, DIAMOND, 1))
        ), "Dropped Item");

        assertFalse(action.canUndo(context(inventory)));
        assertEquals(UndoResult.refusedWorldChanged(), action.undo(context(inventory)));
        assertSlot(inventory, slot(0, DIAMOND, 1));
    }

    @Test
    void recordedActionDelegatesToConcreteAction() {
        FakeInventory inventory = inventory(
            stack(0, DIAMOND, 6),
            stack(1, DIAMOND, 4)
        );
        RecordedInventoryAction action = new RecordedInventoryAction(
            com.rewind.capture.InventoryActionKind.MOVE,
            List.of(
                delta(slot(0, DIAMOND, 10), slot(0, DIAMOND, 6)),
                delta(empty(1), slot(1, DIAMOND, 4))
            ),
            "Moved Stack"
        );

        assertEquals(UndoResult.Status.SUCCESS, action.undo(context(inventory)).status());
        assertSlot(inventory, slot(0, DIAMOND, 10));
        assertSlot(inventory, empty(1));
    }

    private static UndoContext context(FakeInventory inventory) {
        PlayerMessenger messenger = message -> {
        };
        EntityLookup entityLookup = entityId -> Optional.empty();
        return new UndoContext(
            UUID.randomUUID(),
            new Object(),
            new Object(),
            new Object(),
            messenger,
            entityLookup,
            inventory
        );
    }

    private static FakeInventory inventory(TestStack... stacks) {
        FakeInventory inventory = new FakeInventory();
        for (TestStack stack : stacks) {
            inventory.tryPlace(stack.slotIndex(), stack);
        }
        return inventory;
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

    private static TestStack stack(int slotIndex, StackFingerprint fingerprint, int count) {
        return new TestStack(slotIndex, fingerprint, count);
    }

    private static TestStack emptyTestStack(int slotIndex) {
        return new TestStack(slotIndex, StackFingerprint.empty(), 0);
    }

    private static void assertSlot(FakeInventory inventory, SlotState expected) {
        assertTrue(inventory.matches(expected.slotIndex(), expected));
    }

    private record TestStack(int slotIndex, StackFingerprint fingerprint, int count) {
        boolean isEmpty() {
            return fingerprint.isEmpty();
        }

        TestStack withCount(int newCount) {
            return new TestStack(slotIndex, newCount == 0 ? StackFingerprint.empty() : fingerprint, newCount);
        }

        TestStack inSlot(int newSlotIndex) {
            return new TestStack(newSlotIndex, fingerprint, count);
        }
    }

    private static final class FakeInventory implements InventoryAccess {
        private final Map<Integer, TestStack> slots = new HashMap<>();

        @Override
        public Object stackAt(int slotIndex) {
            return slots.getOrDefault(slotIndex, emptyTestStack(slotIndex));
        }

        @Override
        public boolean matches(int slotIndex, SlotState expected) {
            TestStack current = (TestStack) stackAt(slotIndex);
            if (expected.isEmpty()) {
                return current.isEmpty();
            }
            return current.fingerprint().equals(expected.stack()) && current.count() == expected.count();
        }

        @Override
        public Object emptyStack() {
            return emptyTestStack(-1);
        }

        @Override
        public Object copyStackWithCount(Object stack, int count) {
            if (!(stack instanceof TestStack testStack) || count < 0) {
                return null;
            }
            return testStack.withCount(count);
        }

        @Override
        public boolean tryPlace(int slotIndex, Object stack) {
            if (!(stack instanceof TestStack testStack)) {
                return false;
            }
            slots.put(slotIndex, testStack.inSlot(slotIndex));
            return true;
        }

        @Override
        public boolean tryMerge(Object stack) {
            return false;
        }
    }
}
