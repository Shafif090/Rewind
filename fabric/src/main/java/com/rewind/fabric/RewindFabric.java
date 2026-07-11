package com.rewind.fabric;

import com.rewind.Rewind;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

/**
 * Fabric common bootstrap.
 */
public final class RewindFabric implements ModInitializer {
    public static final Identifier UNDO_REQUEST_ID = Identifier.fromNamespaceAndPath(Rewind.MOD_ID, Rewind.UNDO_REQUEST_PATH);

    @Override
    public void onInitialize() {
        RewindFabricNetworking.registerServerReceivers();
    }
}
