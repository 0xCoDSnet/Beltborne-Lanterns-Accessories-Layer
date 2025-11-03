package net.oxcodsnet.bl_accessories_layer.fabric.compat.accessories;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class AccessoriesRendererReloadHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final AtomicBoolean PENDING_RELOAD = new AtomicBoolean();
    private static final AtomicInteger RETRY_DELAY_TICKS = new AtomicInteger();
    private static final AtomicInteger RETRY_ATTEMPTS = new AtomicInteger();

    private static final int RETRY_DELAY = 5;
    private static final int MAX_RETRY_ATTEMPTS = 40;

    private static Runnable reloadAction;

    private AccessoriesRendererReloadHandler() {
    }

    public static void register(final Runnable action) {
        reloadAction = action;

        if (REGISTERED.compareAndSet(false, true)) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client == null || reloadAction == null || !PENDING_RELOAD.get()) {
                    return;
                }

                int delay = RETRY_DELAY_TICKS.get();
                if (delay > 0) {
                    RETRY_DELAY_TICKS.updateAndGet(value -> Math.max(0, value - 1));
                    return;
                }

                if (!PENDING_RELOAD.compareAndSet(true, false)) {
                    return;
                }

                try {
                    reloadAction.run();
                    RETRY_ATTEMPTS.set(0);
                } catch (IllegalArgumentException ex) {
                    if (isMissingModelException(ex)) {
                        int attempt = RETRY_ATTEMPTS.incrementAndGet();
                        if (attempt <= MAX_RETRY_ATTEMPTS) {
                            if (attempt == 1) {
                                LOGGER.debug("Delaying accessories renderer cache refresh: {}", ex.getMessage());
                            }

                            RETRY_DELAY_TICKS.set(RETRY_DELAY);
                            PENDING_RELOAD.set(true);
                            return;
                        }

                        LOGGER.warn(
                                "Skipping accessories renderer cache refresh after {} failed attempts due to missing model errors (latest: {}).",
                                MAX_RETRY_ATTEMPTS,
                                ex.getMessage()
                        );
                        RETRY_ATTEMPTS.set(0);
                        return;
                    }

                    throw ex;
                }
            });

            ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
                setPendingReload();

                var resourceManager = client.getResourceManager();
                if (resourceManager instanceof ReloadableResourceManager reloadable) {
                    reloadable.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                        @Override
                        protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                            return null;
                        }

                        @Override
                        protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
                            setPendingReload();
                        }
                    });
                }
            });
        } else {
            setPendingReload();
        }
    }

    private static void setPendingReload() {
        PENDING_RELOAD.set(true);
        RETRY_DELAY_TICKS.set(0);
        RETRY_ATTEMPTS.set(0);
    }

    private static boolean isMissingModelException(final IllegalArgumentException ex) {
        var message = ex.getMessage();
        return message != null && message.startsWith("No model for layer ");
    }
}
