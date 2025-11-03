package net.oxcodsnet.bl_accessories_layer.neoforge.compat.accessories;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.oxcodsnet.bl_accessories_layer.common.compat.accessories.AccessoriesRendererReloadScheduler;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AccessoriesRendererReloadHandler {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private AccessoriesRendererReloadHandler() {
    }

    public static void register(final Runnable action) {
        AccessoriesRendererReloadScheduler.setReloadAction(action);

        if (REGISTERED.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(AccessoriesRendererReloadHandler::onClientTick);

            var resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof ReloadableResourceManager reloadable) {
                reloadable.registerReloadListener(createListener());
            }
        } else {
            AccessoriesRendererReloadScheduler.requestReload();
        }
    }

    private static PreparableReloadListener createListener() {
        return new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                AccessoriesRendererReloadScheduler.requestReload();
            }
        };
    }

    private static void onClientTick(final ClientTickEvent.Post event) {
        AccessoriesRendererReloadScheduler.runIfReady();
    }
}
