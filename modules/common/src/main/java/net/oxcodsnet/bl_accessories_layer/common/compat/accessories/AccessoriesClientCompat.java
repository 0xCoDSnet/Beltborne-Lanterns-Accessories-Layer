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

    public static void disableDefaultRendering() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }

        Set<Item> lamps = LampRegistry.items();
        lamps.forEach(AccessoriesRendererRegistry::registerNoRenderer);
    }
}
