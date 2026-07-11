package com.rewind.action;

import com.rewind.capture.InventoryActionKind;
import com.rewind.capture.SlotDelta;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;

import java.util.List;
import java.util.Objects;

/**
 * Immutable action record produced by the common transaction recorder.
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
        return delegate().canUndo(context);
    }

    @Override
    public UndoResult undo(UndoContext context) {
        return delegate().undo(context);
    }

    private InventoryAction delegate() {
        return switch (kind) {
            case MOVE -> new MoveAction(deltas, description);
            case SWAP -> new SwapAction(deltas, description);
            case SHIFT_CLICK -> new ShiftClickAction(deltas, description);
            case HOTBAR_SWAP -> new HotbarSwapAction(deltas, description);
            case PICKUP -> new PickupAction(deltas, description);
            case DROP -> new DropAction(deltas, description);
            case SORT -> new InventoryAction() {
                @Override
                public boolean canUndo(UndoContext context) {
                    return false;
                }

                @Override
                public UndoResult undo(UndoContext context) {
                    return UndoResult.refusedWorldChanged();
                }

                @Override
                public String description() {
                    return description;
                }
            };
        };
    }
}
