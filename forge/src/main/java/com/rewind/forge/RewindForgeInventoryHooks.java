package com.rewind.forge;

import com.rewind.capture.ServerInventoryRecorder;
import com.rewind.capture.SlotState;
import com.rewind.capture.StackFingerprint;
import com.rewind.undo.UndoController;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge event bridge for inventory action recording.
 */
public final class RewindForgeInventoryHooks {
    private RewindForgeInventoryHooks() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(RewindForgeInventoryHooks::onServerTick);
        ItemTossEvent.BUS.addListener(RewindForgeInventoryHooks::onItemToss);
        EntityItemPickupEvent.BUS.addListener(RewindForgeInventoryHooks::onItemPickup);
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(RewindForgeInventoryHooks::onPlayerLoggedOut);
    }

    private static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        MinecraftServer server = event.server();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerInventoryRecorder.recordSnapshot(player.getUUID(), snapshot(player));
        }
    }

    private static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ItemEntity itemEntity = event.getEntity();
            ServerInventoryRecorder.markDrop(player.getUUID(), itemEntity.getUUID());
        }
    }

    private static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerInventoryRecorder.markPickup(player.getUUID());
        }
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerInventoryRecorder.clear(player.getUUID());
            UndoController.clear(player.getUUID());
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
