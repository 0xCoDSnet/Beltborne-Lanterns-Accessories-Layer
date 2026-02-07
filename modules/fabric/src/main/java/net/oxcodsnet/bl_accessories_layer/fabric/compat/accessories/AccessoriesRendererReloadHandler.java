package net.oxcodsnet.bl_accessories_layer.fabric.compat.accessories;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.oxcodsnet.bl_accessories_layer.common.compat.accessories.AccessoriesRendererReloadScheduler;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AccessoriesRendererReloadHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private AccessoriesRendererReloadHandler() {
    }

    public static void register(final Runnable action) {
        AccessoriesRendererReloadScheduler.setReloadAction(action);

        if (REGISTERED.compareAndSet(false, true)) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client == null) {
                    return;
                }

                AccessoriesRendererReloadScheduler.runIfReady();
            });

            ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
                AccessoriesRendererReloadScheduler.requestReload();

                var resourceManager = client.getResourceManager();
                if (resourceManager instanceof ReloadableResourceManager reloadable) {
                    try {
                        reloadable.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                            @Override
                            protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                                return null;
                            }

                            @Override
                            protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
                                AccessoriesRendererReloadScheduler.requestReload();
                            }
                        });
                    } catch (UnsupportedOperationException e) {
                        LOGGER.warn("Could not register reload listener, relying on tick-based refresh");
                    }
                }
            });
        } else {
            AccessoriesRendererReloadScheduler.requestReload();
        }
    }
}
