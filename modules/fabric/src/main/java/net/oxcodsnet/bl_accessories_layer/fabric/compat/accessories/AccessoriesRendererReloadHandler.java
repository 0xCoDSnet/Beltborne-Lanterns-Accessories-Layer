package net.oxcodsnet.bl_accessories_layer.fabric.compat.accessories;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AccessoriesRendererReloadHandler {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final AtomicBoolean PENDING_RELOAD = new AtomicBoolean();

    private static Runnable reloadAction;

    private AccessoriesRendererReloadHandler() {
    }

    public static void register(final Runnable action) {
        reloadAction = action;

        if (REGISTERED.compareAndSet(false, true)) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client == null) {
                    return;
                }

                if (PENDING_RELOAD.compareAndSet(true, false) && reloadAction != null) {
                    reloadAction.run();
                }
            });

            ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
                PENDING_RELOAD.set(true);

                var resourceManager = client.getResourceManager();
                if (resourceManager instanceof ReloadableResourceManager reloadable) {
                    reloadable.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                        @Override
                        protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                            return null;
                        }

                        @Override
                        protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
                            PENDING_RELOAD.set(true);
                        }
                    });
                }
            });
        } else {
            PENDING_RELOAD.set(true);
        }
    }
}
