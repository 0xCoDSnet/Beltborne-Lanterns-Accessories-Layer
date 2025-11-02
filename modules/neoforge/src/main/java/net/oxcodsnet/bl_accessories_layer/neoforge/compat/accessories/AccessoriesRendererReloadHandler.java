package net.oxcodsnet.bl_accessories_layer.neoforge.compat.accessories;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

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
            PENDING_RELOAD.set(true);

            NeoForge.EVENT_BUS.addListener(AccessoriesRendererReloadHandler::onClientTick);

            var resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof ReloadableResourceManager reloadable) {
                reloadable.registerReloadListener(createListener());
            }
        } else {
            PENDING_RELOAD.set(true);
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
                PENDING_RELOAD.set(true);
            }
        };
    }

    private static void onClientTick(final ClientTickEvent.Post event) {
        if (PENDING_RELOAD.compareAndSet(true, false) && reloadAction != null) {
            reloadAction.run();
        }
    }
}
