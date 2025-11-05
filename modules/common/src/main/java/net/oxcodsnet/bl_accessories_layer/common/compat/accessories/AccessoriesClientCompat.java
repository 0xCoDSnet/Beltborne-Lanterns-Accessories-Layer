package net.oxcodsnet.bl_accessories_layer.common.compat.accessories;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.minecraft.world.item.Item;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-only helpers for Accessories integration.
 */
public final class AccessoriesClientCompat {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private AccessoriesClientCompat() {
    }

    /**
     * Marks Accessories' default renderer as disabled for all Beltborne lanterns.
     *
     * @return a task that should be executed when it is safe to rebuild the Accessories renderer cache
     */
    public static Runnable disableDefaultRendering() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return AccessoriesClientCompat::refreshRendererCache;
        }

        registerNoRenderers();
        return AccessoriesClientCompat::refreshRendererCache;
    }

    private static void registerNoRenderers() {
        Set<Item> lamps = LampRegistry.items();
        lamps.forEach(AccessoriesRendererRegistry::registerNoRenderer);
    }

    public static void refreshRendererCache() {
        registerNoRenderers();
        AccessoriesRendererRegistry.onReload();
    }
}
