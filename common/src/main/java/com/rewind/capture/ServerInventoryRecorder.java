package com.rewind.capture;

import com.rewind.action.InventoryAction;
import com.rewind.action.RecordedInventoryAction;
import com.rewind.undo.UndoController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side recorder that turns loader snapshots and event hints into undo history entries.
 */
public final class ServerInventoryRecorder {
    private static final Map<UUID, List<SlotState>> PREVIOUS_SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, PendingHint> PENDING_HINTS = new ConcurrentHashMap<>();

    private ServerInventoryRecorder() {
    }

    public static void markDrop(UUID playerId, UUID droppedEntityId) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(droppedEntityId, "droppedEntityId");
        PENDING_HINTS.put(playerId, new PendingHint(InventoryActionHint.DROP, Optional.of(droppedEntityId)));
    }

    public static void markPickup(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        PENDING_HINTS.put(playerId, new PendingHint(InventoryActionHint.PICKUP, Optional.empty()));
    }

    public static Optional<InventoryAction> recordSnapshot(UUID playerId, List<SlotState> currentSnapshot) {
        Objects.requireNonNull(playerId, "playerId");
        currentSnapshot = List.copyOf(Objects.requireNonNull(currentSnapshot, "currentSnapshot"));

        List<SlotState> previousSnapshot = PREVIOUS_SNAPSHOTS.put(playerId, currentSnapshot);
        if (previousSnapshot == null) {
            return Optional.empty();
        }

        PendingHint hint = PENDING_HINTS.remove(playerId);
        InventoryActionHint actionHint = hint == null ? InventoryActionHint.UNKNOWN : hint.hint();
        ActionDetectionResult result = InventoryTransactionDetector.detect(previousSnapshot, currentSnapshot, actionHint);
        if (!result.isDetected()) {
            return Optional.empty();
        }

        InventoryAction action = result.action().orElseThrow();
        if (hint != null && hint.droppedEntityId().isPresent() && action instanceof RecordedInventoryAction recorded) {
            action = recorded.withDroppedEntity(hint.droppedEntityId().get());
        }

        UndoController.serviceFor(playerId).record(action);
        return Optional.of(action);
    }

    public static void clear(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        PREVIOUS_SNAPSHOTS.remove(playerId);
        PENDING_HINTS.remove(playerId);
    }

    private record PendingHint(InventoryActionHint hint, Optional<UUID> droppedEntityId) {
        private PendingHint {
            Objects.requireNonNull(hint, "hint");
            Objects.requireNonNull(droppedEntityId, "droppedEntityId");
        }
    }
}
