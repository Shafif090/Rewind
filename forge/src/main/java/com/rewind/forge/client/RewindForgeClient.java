package com.rewind.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rewind.Rewind;
import com.rewind.forge.RewindForgeNetworking;
import com.rewind.forge.net.UndoRequestMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Forge client bootstrap.
 */
public final class RewindForgeClient {
    private static final KeyMapping UNDO_KEY = new KeyMapping(
        Rewind.KEY_UNDO,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_Z,
        KeyMapping.Category.INVENTORY
    );

    private RewindForgeClient() {
    }

    public static void register() {
        RegisterKeyMappingsEvent.BUS.addListener(RewindForgeClient::registerKeyMappings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(RewindForgeClient::onClientTick);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(UNDO_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (UNDO_KEY.consumeClick()) {
            if (client.hasControlDown() && client.getConnection() != null) {
                RewindForgeNetworking.CHANNEL.send(UndoRequestMessage.INSTANCE, PacketDistributor.SERVER.noArg());
            }
        }
    }
}
