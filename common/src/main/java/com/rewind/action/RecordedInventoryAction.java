package com.rewind.action;

import com.rewind.capture.InventoryActionKind;
import com.rewind.capture.SlotDelta;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;

import java.util.List;
import java.util.Objects;

/**
 * Immutable action record produced by the common transaction recorder.
 *
 * <p>Concrete slot-restoration logic is added by the action implementation phase. Until then,
 * recorded actions fail closed so a detected action can never duplicate or recreate items.</p>
 */
public record RecordedInventoryAction(
    InventoryActionKind kind,
    List<SlotDelta> deltas,
    String description
) implements InventoryAction {
    public RecordedInventoryAction {
        Objects.requireNonNull(kind, "kind");
        deltas = List.copyOf(Objects.requireNonNull(deltas, "deltas"));
        Objects.requireNonNull(description, "description");
    }

    @Override
    public boolean canUndo(UndoContext context) {
        return false;
    }

    @Override
    public UndoResult undo(UndoContext context) {
        return UndoResult.refusedWorldChanged();
    }
}
