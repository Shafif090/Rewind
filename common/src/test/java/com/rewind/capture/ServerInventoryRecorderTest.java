package com.rewind.capture;

import com.rewind.action.DropAction;
import com.rewind.action.InventoryAction;
import com.rewind.action.RecordedInventoryAction;
import com.rewind.undo.UndoController;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerInventoryRecorderTest {
    private static final StackFingerprint DIAMOND = StackFingerprint.of("minecraft:diamond", "{}");

    @Test
    void firstSnapshotOnlySeedsRecorder() {
        UUID playerId = UUID.randomUUID();

        Optional<InventoryAction> action = ServerInventoryRecorder.recordSnapshot(playerId, List.of(slot(0, 1)));

        assertTrue(action.isEmpty());
        assertEquals(0, UndoController.serviceFor(playerId).history().size());
        ServerInventoryRecorder.clear(playerId);
        UndoController.clear(playerId);
    }

    @Test
    void unknownSnapshotChangeRecordsDetectedAction() {
        UUID playerId = UUID.randomUUID();
        ServerInventoryRecorder.recordSnapshot(playerId, List.of(slot(0, 4), empty(1)));

        Optional<InventoryAction> action = ServerInventoryRecorder.recordSnapshot(playerId, List.of(slot(0, 2), slot(1, 2)));

        assertEquals(InventoryActionKind.MOVE, assertInstanceOf(RecordedInventoryAction.class, action.orElseThrow()).kind());
        assertEquals(1, UndoController.serviceFor(playerId).history().size());
        ServerInventoryRecorder.clear(playerId);
        UndoController.clear(playerId);
    }

    @Test
    void dropHintAttachesDroppedEntityId() {
        UUID playerId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        ServerInventoryRecorder.recordSnapshot(playerId, List.of(slot(0, 2)));
        ServerInventoryRecorder.markDrop(playerId, entityId);

        Optional<InventoryAction> action = ServerInventoryRecorder.recordSnapshot(playerId, List.of(slot(0, 1)));

        assertInstanceOf(DropAction.class, action.orElseThrow());
        assertEquals(1, UndoController.serviceFor(playerId).history().size());
        ServerInventoryRecorder.clear(playerId);
        UndoController.clear(playerId);
    }

    private static SlotState slot(int slotIndex, int count) {
        return SlotState.of(slotIndex, DIAMOND, count);
    }

    private static SlotState empty(int slotIndex) {
        return SlotState.empty(slotIndex);
    }
}
