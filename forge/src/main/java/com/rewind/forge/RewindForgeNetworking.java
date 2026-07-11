package com.rewind.forge;

import com.rewind.Rewind;
import com.rewind.forge.net.UndoRequestMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

/**
 * Forge networking registration.
 */
public final class RewindForgeNetworking {
    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(Rewind.MOD_ID, Rewind.UNDO_REQUEST_PATH))
        .networkProtocolVersion(1)
        .simpleChannel();

    private RewindForgeNetworking() {
    }

    public static void registerPayloads() {
        CHANNEL.messageBuilder(UndoRequestMessage.class, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> {
            })
            .decoder(buffer -> UndoRequestMessage.INSTANCE)
            .consumerMainThread((message, context) -> {
                var sender = context.getSender();
                if (sender != null) {
                    RewindForgeServerUndo.handleUndoRequest(sender);
                }
                context.setPacketHandled(true);
            })
            .add()
            .build();
    }
}
