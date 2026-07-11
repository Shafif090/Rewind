package com.rewind.fabric.net;

import com.rewind.Rewind;
import com.rewind.fabric.RewindFabric;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Empty Fabric client-to-server payload requesting the latest inventory undo.
 */
public record UndoRequestPayload() implements CustomPacketPayload {
    public static final UndoRequestPayload INSTANCE = new UndoRequestPayload();
    public static final Type<UndoRequestPayload> TYPE = new Type<>(RewindFabric.UNDO_REQUEST_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UndoRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
