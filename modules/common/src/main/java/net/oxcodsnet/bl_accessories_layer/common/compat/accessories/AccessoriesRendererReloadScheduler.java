package net.oxcodsnet.bl_accessories_layer.common.compat.accessories;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared logic for scheduling safe Accessories renderer cache refreshes across platforms.
 */
public final class AccessoriesRendererReloadScheduler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicBoolean PENDING_RELOAD = new AtomicBoolean();
    private static final AtomicInteger RETRY_DELAY_TICKS = new AtomicInteger();
    private static final AtomicInteger RETRY_ATTEMPTS = new AtomicInteger();

    private static final int RETRY_DELAY = 5;
    private static final int MAX_RETRY_ATTEMPTS = 40;

    private static volatile Runnable reloadAction;

    private AccessoriesRendererReloadScheduler() {
    }

    public static void setReloadAction(final Runnable action) {
        reloadAction = action;
        requestReload();
    }

    public static void requestReload() {
        PENDING_RELOAD.set(true);
        RETRY_DELAY_TICKS.set(0);
        RETRY_ATTEMPTS.set(0);
    }

    /**
     * Attempts to execute the current reload action. Should be called from a client tick once per frame.
     *
     * @return {@code true} if the action executed successfully during this call, {@code false} otherwise
     */
    public static boolean runIfReady() {
        var action = reloadAction;
        if (!PENDING_RELOAD.get() || action == null) {
            return false;
        }

        int delay = RETRY_DELAY_TICKS.get();
        if (delay > 0) {
            RETRY_DELAY_TICKS.updateAndGet(value -> Math.max(0, value - 1));
            return false;
        }

        if (!PENDING_RELOAD.compareAndSet(true, false)) {
            return false;
        }

        try {
            action.run();
            RETRY_ATTEMPTS.set(0);
            return true;
        } catch (IllegalArgumentException ex) {
            if (isMissingModelException(ex)) {
                int attempt = RETRY_ATTEMPTS.incrementAndGet();
                if (attempt <= MAX_RETRY_ATTEMPTS) {
                    if (attempt == 1) {
                        LOGGER.debug("Delaying accessories renderer cache refresh: {}", ex.getMessage());
                    }

                    RETRY_DELAY_TICKS.set(RETRY_DELAY);
                    PENDING_RELOAD.set(true);
                    return false;
                }

                LOGGER.warn(
                        "Skipping accessories renderer cache refresh after {} failed attempts due to missing model errors (latest: {}).",
                        MAX_RETRY_ATTEMPTS,
                        ex.getMessage()
                );
                RETRY_ATTEMPTS.set(0);
                return false;
            }

            throw ex;
        }
    }

    private static boolean isMissingModelException(final IllegalArgumentException ex) {
        var message = ex.getMessage();
        return message != null && message.startsWith("No model for layer ");
    }
}
