package com.rewind.fabric;

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
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Fabric bridge from server packets into the shared undo controller.
 */
public final class RewindFabricServerUndo {
    private RewindFabricServerUndo() {
    }

    public static void handleUndoRequest(ServerPlayer player) {
        UndoController.undoLatest(contextFor(player));
    }

    private static UndoContext contextFor(ServerPlayer player) {
        PlayerMessenger messenger = message -> player.sendOverlayMessage(Component.literal(message));
        EntityLookup entityLookup = entityId -> Optional.ofNullable(player.level().getEntityInAnyDimension(entityId));
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
}
