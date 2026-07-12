package com.rewind.forge;

import com.rewind.Rewind;
import com.rewind.forge.client.RewindForgeClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge bootstrap.
 */
@Mod(Rewind.MOD_ID)
public final class RewindForge {
    public RewindForge() {
        RewindForgeNetworking.registerPayloads();
        RewindForgeInventoryHooks.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RewindForgeClient.register();
        }
    }
}
