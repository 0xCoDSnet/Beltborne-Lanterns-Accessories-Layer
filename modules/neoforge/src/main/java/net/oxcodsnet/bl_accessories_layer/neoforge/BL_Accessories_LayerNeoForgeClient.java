package net.oxcodsnet.bl_accessories_layer.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.oxcodsnet.bl_accessories_layer.BL_Accessories_Layer;
import net.oxcodsnet.bl_accessories_layer.common.compat.accessories.AccessoriesClientCompat;
import net.oxcodsnet.bl_accessories_layer.neoforge.compat.accessories.AccessoriesRendererReloadHandler;

@EventBusSubscriber(modid = BL_Accessories_Layer.MOD_ID, value = Dist.CLIENT)
public final class BL_Accessories_LayerNeoForgeClient {
    private BL_Accessories_LayerNeoForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("accessories")) {
            event.enqueueWork(() -> AccessoriesRendererReloadHandler.register(AccessoriesClientCompat.disableDefaultRendering()));
        }
    }

    @SubscribeEvent
    public static void onAddClientReloadListeners(final AddClientReloadListenersEvent event) {
        if (ModList.get().isLoaded("accessories")) {
            event.addListener(
                    ResourceLocation.fromNamespaceAndPath(BL_Accessories_Layer.MOD_ID, "accessories_renderer_reload"),
                    AccessoriesRendererReloadHandler.createReloadListener()
            );
        }
    }
}
