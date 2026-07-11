package com.rewind.action;

import com.rewind.capture.SlotDelta;
import com.rewind.platform.InventoryAccess;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;

import java.util.List;
import java.util.Objects;

/**
 * Represents an item drop. Full safe restoration requires dropped-entity validation in Phase 6.
 */
public final class DropAction implements InventoryAction {
    private final List<SlotDelta> deltas;
    private final String description;

    public DropAction(List<SlotDelta> deltas, String description) {
        this.deltas = List.copyOf(Objects.requireNonNull(deltas, "deltas"));
        this.description = Objects.requireNonNull(description, "description");
        ActionDeltaRules.requireDrop(this.deltas);
    }

    @Override
    public boolean canUndo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        return false;
    }

    @Override
    public UndoResult undo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        InventoryAccess inventory = context.inventoryAccess();
        for (SlotDelta delta : deltas) {
            if (!inventory.matches(delta.slotIndex(), delta.after())) {
                return UndoResult.refusedWorldChanged();
            }
        }
        return UndoResult.refusedWorldChanged();
    }

    @Override
    public String description() {
        return description;
    }
}
