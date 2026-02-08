package net.oxcodsnet.bl_accessories_layer.common.mixin;

import io.wispforest.accessories.impl.core.AccessoriesContainerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.oxcodsnet.bl_accessories_layer.common.compat.accessories.AbstractAccessoriesCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessoriesContainerImpl.class)
public class AccessoriesContainerMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "setShouldRender", at = @At("TAIL"), remap = false)
    private void bl_onRenderToggle(int index, boolean value, CallbackInfo ci) {
        var self = (io.wispforest.accessories.api.AccessoriesContainer) (Object) this;
        var entity = self.capability().entity();
        if (!(entity instanceof ServerPlayer player)) return;

        var compat = AbstractAccessoriesCompat.getInstance();
        if (compat == null) return;

        ((AbstractAccessoriesCompat<ServerPlayer, ?>) compat).reevaluateAndBroadcast(player);
    }
}
