package net.oxcodsnet.bl_accessories_layer.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.oxcodsnet.bl_accessories_layer.common.compat.accessories.AccessoriesClientCompat;

public final class BL_Accessories_LayerFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("accessories")) {
            AccessoriesClientCompat.disableDefaultRendering();
        }
    }
}
