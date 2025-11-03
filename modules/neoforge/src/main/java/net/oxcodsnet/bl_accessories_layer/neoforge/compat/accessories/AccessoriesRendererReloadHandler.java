package net.oxcodsnet.bl_accessories_layer.neoforge.compat.accessories;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
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
            setPendingReload();

            NeoForge.EVENT_BUS.addListener(AccessoriesRendererReloadHandler::onClientTick);

            var resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof ReloadableResourceManager reloadable) {
                reloadable.registerReloadListener(createListener());
            }
        } else {
            setPendingReload();
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
                setPendingReload();
            }
        };
    }

    private static void onClientTick(final ClientTickEvent.Post event) {
        if (!PENDING_RELOAD.get() || reloadAction == null) {
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
