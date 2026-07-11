package com.rewind.undo;

import com.rewind.Rewind;
import com.rewind.action.InventoryAction;
import com.rewind.history.InventoryHistory;

import java.util.Objects;

/**
 * Coordinates history lookup, strict validation, undo execution, and player messaging.
 */
public final class UndoService {
    private final InventoryHistory history;

    public UndoService(InventoryHistory history) {
        this.history = Objects.requireNonNull(history, "history");
    }

    public InventoryHistory history() {
        return history;
    }

    /**
     * Records a new undoable inventory action.
     *
     * @param action action to record
     */
    public void record(InventoryAction action) {
        history.push(action);
    }

    /**
     * Attempts to undo the latest action. Failed validation does not pop the history.
     *
     * @param context server-side context and platform services
     * @return undo result sent to the player
     */
    public UndoResult undoLatest(UndoContext context) {
        Objects.requireNonNull(context, "context");

        var action = history.peek();
        if (action.isEmpty()) {
            return notify(context, UndoResult.refused("Cannot undo: no inventory actions recorded."));
        }

        InventoryAction latest = action.get();
        if (!latest.canUndo(context)) {
            return notify(context, UndoResult.refusedWorldChanged());
        }

        UndoResult result = latest.undo(context);
        if (result.shouldPopHistory()) {
            history.pop();
            return notify(context, UndoResult.success(latest.description()));
        }

        if (result.status() == UndoResult.Status.REFUSED && result.message().isBlank()) {
            return notify(context, UndoResult.refusedWorldChanged());
        }

        return notify(context, result);
    }

    private UndoResult notify(UndoContext context, UndoResult result) {
        String message = result.message().isBlank() ? Rewind.CANNOT_UNDO_WORLD_CHANGED : result.message();
        UndoResult normalized = new UndoResult(result.status(), message);
        context.messenger().sendActionBar(normalized.message());
        return normalized;
    }
}
