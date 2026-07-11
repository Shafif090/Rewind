package com.rewind.fabric;

import com.rewind.fabric.net.UndoRequestPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Fabric networking registration.
 */
public final class RewindFabricNetworking {
    private RewindFabricNetworking() {
    }

    public static void registerServerReceivers() {
        PayloadTypeRegistry.serverboundPlay().register(UndoRequestPayload.TYPE, UndoRequestPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UndoRequestPayload.TYPE, (payload, context) ->
            context.server().execute(() -> RewindFabricServerUndo.handleUndoRequest(context.player()))
        );
    }
}
