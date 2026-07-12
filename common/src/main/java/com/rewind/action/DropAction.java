package com.rewind.action;

import com.rewind.capture.SlotDelta;
import com.rewind.capture.SlotState;
import com.rewind.platform.EntityLookup;
import com.rewind.platform.InventoryAccess;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Reverses an item drop only while the original dropped item entity still exists unchanged.
 */
public final class DropAction implements InventoryAction {
    private final List<SlotDelta> deltas;
    private final String description;
    private final Optional<UUID> droppedEntityId;

    public DropAction(List<SlotDelta> deltas, String description) {
        this(deltas, description, null);
    }

    public DropAction(List<SlotDelta> deltas, String description, UUID droppedEntityId) {
        this.deltas = List.copyOf(Objects.requireNonNull(deltas, "deltas"));
        this.description = Objects.requireNonNull(description, "description");
        this.droppedEntityId = Optional.ofNullable(droppedEntityId);
        ActionDeltaRules.requireDrop(this.deltas);
    }

    @Override
    public boolean canUndo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        return validate(context).isPresent();
    }

    @Override
    public UndoResult undo(UndoContext context) {
        Objects.requireNonNull(context, "context");

        Optional<DropUndoPlan> plan = validate(context);
        if (plan.isEmpty()) {
            return UndoResult.refusedWorldChanged();
        }

        DropUndoPlan undoPlan = plan.get();
        EntityLookup entities = context.entityLookup();
        InventoryAccess inventory = context.inventoryAccess();
        if (!entities.removeDroppedItem(undoPlan.entity())) {
            return UndoResult.refusedWorldChanged();
        }

        boolean restored = undoPlan.restoreOriginalSlot()
            ? inventory.tryPlace(undoPlan.originalSlot(), undoPlan.stack())
            : inventory.tryMerge(undoPlan.stack());
        if (!restored) {
            return UndoResult.failed("Undo failed: could not restore dropped item.");
        }

        return UndoResult.success(description);
    }

    @Override
    public String description() {
        return description;
    }

    private Optional<DropUndoPlan> validate(UndoContext context) {
        if (droppedEntityId.isEmpty() || deltas.size() != 1) {
            return Optional.empty();
        }

        SlotDelta delta = deltas.getFirst();
        EntityLookup entities = context.entityLookup();
        InventoryAccess inventory = context.inventoryAccess();
        Optional<Object> entity = entities.findEntity(droppedEntityId.get());
        if (entity.isEmpty() || !entities.isLiveDroppedItem(entity.get())) {
            return Optional.empty();
        }
        if (!entities.droppedItemMatches(entity.get(), delta.before().stack(), delta.lostCount())) {
            return Optional.empty();
        }

        Optional<Object> droppedStack = entities.copyDroppedItemStack(entity.get());
        if (droppedStack.isEmpty()) {
            return Optional.empty();
        }

        if (inventory.matches(delta.slotIndex(), delta.after())) {
            Object restoredSlot = stackForOriginalSlot(inventory, delta, droppedStack.get());
            if (restoredSlot == null) {
                return Optional.empty();
            }
            return Optional.of(new DropUndoPlan(entity.get(), delta.slotIndex(), restoredSlot, true));
        }

        Object mergeStack = inventory.copyStackWithCount(droppedStack.get(), delta.lostCount());
        if (mergeStack == null || !inventory.canMerge(mergeStack)) {
            return Optional.empty();
        }
        return Optional.of(new DropUndoPlan(entity.get(), delta.slotIndex(), mergeStack, false));
    }

    private Object stackForOriginalSlot(InventoryAccess inventory, SlotDelta delta, Object droppedStack) {
        SlotState after = delta.after();
        Object template = after.isEmpty() ? droppedStack : inventory.stackAt(delta.slotIndex());
        return inventory.copyStackWithCount(template, delta.before().count());
    }

    private record DropUndoPlan(Object entity, int originalSlot, Object stack, boolean restoreOriginalSlot) {
        private DropUndoPlan {
            Objects.requireNonNull(entity, "entity");
            Objects.requireNonNull(stack, "stack");
        }
    }
}
