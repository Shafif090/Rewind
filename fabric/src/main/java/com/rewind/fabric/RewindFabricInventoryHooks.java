package com.rewind.fabric;

import com.rewind.capture.ServerInventoryRecorder;
import com.rewind.capture.SlotState;
import com.rewind.capture.StackFingerprint;
import com.rewind.undo.UndoController;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric event bridge for inventory action recording.
 */
public final class RewindFabricInventoryHooks {
    private RewindFabricInventoryHooks() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RewindFabricInventoryHooks::recordPlayerSnapshots);
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ItemEntity itemEntity && itemEntity.getOwner() instanceof ServerPlayer player) {
                ServerInventoryRecorder.markDrop(player.getUUID(), itemEntity.getUUID());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerInventoryRecorder.clear(handler.player.getUUID());
            UndoController.clear(handler.player.getUUID());
        });
    }

    private static void recordPlayerSnapshots(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerInventoryRecorder.recordSnapshot(player.getUUID(), snapshot(player));
        }
    }

    private static List<SlotState> snapshot(ServerPlayer player) {
        ArrayList<SlotState> slots = new ArrayList<>(player.getInventory().getContainerSize());
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            slots.add(slotState(slot, player.getInventory().getItem(slot)));
        }
        return slots;
    }

    private static SlotState slotState(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return SlotState.empty(slot);
        }
        return SlotState.of(slot, fingerprint(stack), stack.getCount());
    }

    private static StackFingerprint fingerprint(ItemStack stack) {
        if (stack.isEmpty()) {
            return StackFingerprint.empty();
        }
        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return StackFingerprint.of(itemKey, stack.getComponents().toString());
    }
}
