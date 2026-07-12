package com.rewind.forge;

import com.rewind.platform.EntityLookup;
import com.rewind.platform.InventoryAccess;
import com.rewind.platform.PlayerMessenger;
import com.rewind.capture.SlotState;
import com.rewind.capture.StackFingerprint;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoController;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Forge bridge from server packets into the shared undo controller.
 */
public final class RewindForgeServerUndo {
    private RewindForgeServerUndo() {
    }

    public static void handleUndoRequest(ServerPlayer player) {
        UndoController.undoLatest(contextFor(player));
    }

    private static UndoContext contextFor(ServerPlayer player) {
        PlayerMessenger messenger = message -> player.sendOverlayMessage(Component.literal(message));
        EntityLookup entityLookup = new EntityLookup() {
            @Override
            public Optional<Object> findEntity(java.util.UUID entityId) {
                return Optional.ofNullable(player.level().getEntityInAnyDimension(entityId));
            }

            @Override
            public boolean isLiveDroppedItem(Object entity) {
                return entity instanceof ItemEntity itemEntity
                    && !itemEntity.isRemoved()
                    && itemEntity.isAlive()
                    && !itemEntity.getItem().isEmpty();
            }

            @Override
            public boolean droppedItemMatches(Object entity, StackFingerprint stack, int count) {
                if (!(entity instanceof ItemEntity itemEntity)) {
                    return false;
                }
                ItemStack itemStack = itemEntity.getItem();
                return itemStack.getCount() == count && fingerprint(itemStack).equals(stack);
            }

            @Override
            public Optional<Object> copyDroppedItemStack(Object entity) {
                if (!(entity instanceof ItemEntity itemEntity)) {
                    return Optional.empty();
                }
                return Optional.of(itemEntity.getItem().copy());
            }

            @Override
            public boolean removeDroppedItem(Object entity) {
                if (!(entity instanceof ItemEntity itemEntity) || itemEntity.isRemoved()) {
                    return false;
                }
                itemEntity.discard();
                return true;
            }
        };
        InventoryAccess inventoryAccess = new InventoryAccess() {
            @Override
            public Object stackAt(int slotIndex) {
                return player.getInventory().getItem(slotIndex);
            }

            @Override
            public boolean matches(int slotIndex, SlotState expected) {
                ItemStack current = player.getInventory().getItem(slotIndex);
                if (expected.isEmpty()) {
                    return current.isEmpty();
                }
                return current.getCount() == expected.count()
                    && fingerprint(current).equals(expected.stack());
            }

            @Override
            public Object emptyStack() {
                return ItemStack.EMPTY;
            }

            @Override
            public Object copyStackWithCount(Object stack, int count) {
                if (!(stack instanceof ItemStack itemStack) || count < 0) {
                    return null;
                }
                ItemStack copy = itemStack.copy();
                copy.setCount(count);
                return copy;
            }

            @Override
            public boolean tryPlace(int slotIndex, Object stack) {
                if (!(stack instanceof ItemStack itemStack)) {
                    return false;
                }
                player.getInventory().setItem(slotIndex, itemStack);
                return true;
            }

            @Override
            public boolean tryMerge(Object stack) {
                return stack instanceof ItemStack itemStack && player.getInventory().add(itemStack);
            }

            @Override
            public boolean canMerge(Object stack) {
                return stack instanceof ItemStack itemStack && canMergeIntoInventory(player, itemStack);
            }
        };

        return new UndoContext(
            player.getUUID(),
            player.level().getServer(),
            player.level(),
            player,
            messenger,
            entityLookup,
            inventoryAccess
        );
    }

    private static StackFingerprint fingerprint(ItemStack stack) {
        if (stack.isEmpty()) {
            return StackFingerprint.empty();
        }
        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return StackFingerprint.of(itemKey, stack.getComponents().toString());
    }

    private static boolean canMergeIntoInventory(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        int remaining = stack.getCount();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current.isEmpty()) {
                remaining -= stack.getMaxStackSize();
            } else if (fingerprint(current).equals(fingerprint(stack))) {
                remaining -= Math.max(0, Math.min(current.getMaxStackSize(), stack.getMaxStackSize()) - current.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }
}
