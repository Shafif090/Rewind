package com.rewind.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rewind.Rewind;
import com.rewind.fabric.net.UndoRequestPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Fabric client bootstrap.
 */
public final class RewindFabricClient implements ClientModInitializer {
    private static final KeyMapping UNDO_KEY = new KeyMapping(
        Rewind.KEY_UNDO,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_Z,
        KeyMapping.Category.INVENTORY
    );

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(UNDO_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(RewindFabricClient::handleClientTick);
    }

    private static void handleClientTick(Minecraft client) {
        while (UNDO_KEY.consumeClick()) {
            if (client.hasControlDown() && client.getConnection() != null) {
                ClientPlayNetworking.send(UndoRequestPayload.INSTANCE);
            }
        }
    }
}
