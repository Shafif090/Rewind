package com.rewind.action;

import com.rewind.capture.SlotDelta;
import com.rewind.capture.SlotState;
import com.rewind.platform.InventoryAccess;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class StrictSlotRestoringAction implements InventoryAction {
    private final List<SlotDelta> deltas;
    private final String description;

    StrictSlotRestoringAction(List<SlotDelta> deltas, String description) {
        this.deltas = List.copyOf(Objects.requireNonNull(deltas, "deltas"));
        this.description = Objects.requireNonNull(description, "description");
        if (this.deltas.isEmpty()) {
            throw new IllegalArgumentException("deltas must not be empty");
        }
    }

    protected final List<SlotDelta> deltas() {
        return deltas;
    }

    @Override
    public final boolean canUndo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        return currentStateMatches(context.inventoryAccess()) && buildRestoration(context.inventoryAccess()) != null;
    }

    @Override
    public final UndoResult undo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        InventoryAccess inventory = context.inventoryAccess();
        if (!currentStateMatches(inventory)) {
            return UndoResult.refusedWorldChanged();
        }

        List<Placement> restoration = buildRestoration(inventory);
        if (restoration == null) {
            return UndoResult.refusedWorldChanged();
        }

        Object empty = inventory.emptyStack();
        for (SlotDelta delta : deltas) {
            if (!inventory.tryPlace(delta.slotIndex(), empty)) {
                return UndoResult.failed("Undo failed: could not clear inventory slot.");
            }
        }
        for (Placement placement : restoration) {
            if (!placement.state().isEmpty() && !inventory.tryPlace(placement.state().slotIndex(), placement.stack())) {
                return UndoResult.failed("Undo failed: could not restore inventory slot.");
            }
        }

        return UndoResult.success(description);
    }

    @Override
    public final String description() {
        return description;
    }

    private boolean currentStateMatches(InventoryAccess inventory) {
        for (SlotDelta delta : deltas) {
            if (!inventory.matches(delta.slotIndex(), delta.after())) {
                return false;
            }
        }
        return true;
    }

    private List<Placement> buildRestoration(InventoryAccess inventory) {
        ArrayList<Placement> placements = new ArrayList<>();
        for (SlotDelta delta : deltas) {
            SlotState before = delta.before();
            if (before.isEmpty()) {
                placements.add(new Placement(before, inventory.emptyStack()));
                continue;
            }

            Object template = templateFor(inventory, before);
            if (template == null) {
                return null;
            }
            Object restored = inventory.copyStackWithCount(template, before.count());
            if (restored == null) {
                return null;
            }
            placements.add(new Placement(before, restored));
        }
        return placements;
    }

    private Object templateFor(InventoryAccess inventory, SlotState desired) {
        for (SlotDelta delta : deltas) {
            SlotState after = delta.after();
            if (!after.isEmpty() && after.sameStackIdentity(desired)) {
                return inventory.stackAt(delta.slotIndex());
            }
        }
        return null;
    }

    private record Placement(SlotState state, Object stack) {
        private Placement {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(stack, "stack");
        }
    }
}
