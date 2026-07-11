package com.rewind.forge;

import com.rewind.platform.EntityLookup;
import com.rewind.platform.InventoryAccess;
import com.rewind.platform.PlayerMessenger;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        EntityLookup entityLookup = entityId -> Optional.ofNullable(player.level().getEntityInAnyDimension(entityId));
        InventoryAccess inventoryAccess = new InventoryAccess() {
            @Override
            public Object stackAt(int slotIndex) {
                return player.getInventory().getItem(slotIndex);
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
}
