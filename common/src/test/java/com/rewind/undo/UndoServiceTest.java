package com.rewind.undo;

import com.rewind.Rewind;
import com.rewind.action.InventoryAction;
import com.rewind.capture.SlotState;
import com.rewind.history.InventoryHistory;
import com.rewind.platform.EntityLookup;
import com.rewind.platform.InventoryAccess;
import com.rewind.platform.PlayerMessenger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UndoServiceTest {
    @Test
    void successfulUndoPopsHistoryAndSendsMessage() {
        ArrayList<String> messages = new ArrayList<>();
        UndoService service = new UndoService(new InventoryHistory());
        service.record(action(true, UndoResult.success("Moved Stack"), "Moved Stack"));

        UndoResult result = service.undoLatest(context(messages));

        assertEquals(UndoResult.Status.SUCCESS, result.status());
        assertEquals("Undid: Moved Stack", messages.getFirst());
        assertEquals(0, service.history().size());
    }

    @Test
    void failedValidationDoesNotPopHistory() {
        ArrayList<String> messages = new ArrayList<>();
        UndoService service = new UndoService(new InventoryHistory());
        service.record(action(false, UndoResult.success("Moved Stack"), "Moved Stack"));

        UndoResult result = service.undoLatest(context(messages));

        assertEquals(UndoResult.Status.REFUSED, result.status());
        assertEquals(Rewind.CANNOT_UNDO_WORLD_CHANGED, messages.getFirst());
        assertEquals(1, service.history().size());
    }

    @Test
    void emptyHistorySendsHelpfulMessage() {
        ArrayList<String> messages = new ArrayList<>();
        UndoService service = new UndoService(new InventoryHistory());

        UndoResult result = service.undoLatest(context(messages));

        assertEquals(UndoResult.Status.REFUSED, result.status());
        assertEquals("Cannot undo: no inventory actions recorded.", messages.getFirst());
    }

    @Test
    void swapMessageUsesActionDescription() {
        ArrayList<String> messages = new ArrayList<>();
        UndoService service = new UndoService(new InventoryHistory());
        service.record(action(true, UndoResult.success("Swapped Slots"), "Swapped Slots"));

        service.undoLatest(context(messages));

        assertEquals("Undid: Swapped Slots", messages.getFirst());
    }

    @Test
    void dropMessageUsesActionDescription() {
        ArrayList<String> messages = new ArrayList<>();
        UndoService service = new UndoService(new InventoryHistory());
        service.record(action(true, UndoResult.success("Dropped Diamond Pickaxe"), "Dropped Diamond Pickaxe"));

        service.undoLatest(context(messages));

        assertEquals("Undid: Dropped Diamond Pickaxe", messages.getFirst());
    }

    private static UndoContext context(ArrayList<String> messages) {
        PlayerMessenger messenger = messages::add;
        EntityLookup entityLookup = entityId -> Optional.empty();
        InventoryAccess inventoryAccess = new InventoryAccess() {
            @Override
            public Object stackAt(int slotIndex) {
                return new Object();
            }

            @Override
            public boolean matches(int slotIndex, SlotState expected) {
                return true;
            }

            @Override
            public Object emptyStack() {
                return new Object();
            }

            @Override
            public Object copyStackWithCount(Object stack, int count) {
                return stack;
            }

            @Override
            public boolean tryPlace(int slotIndex, Object stack) {
                return true;
            }

            @Override
            public boolean tryMerge(Object stack) {
                return true;
            }

            @Override
            public boolean canMerge(Object stack) {
                return true;
            }
        };

        return new UndoContext(
            UUID.randomUUID(),
            new Object(),
            new Object(),
            new Object(),
            messenger,
            entityLookup,
            inventoryAccess
        );
    }

    private static InventoryAction action(boolean canUndo, UndoResult result, String description) {
        return new InventoryAction() {
            @Override
            public boolean canUndo(UndoContext context) {
                return canUndo;
            }

            @Override
            public UndoResult undo(UndoContext context) {
                return result;
            }

            @Override
            public String description() {
                return description;
            }
        };
    }
}
