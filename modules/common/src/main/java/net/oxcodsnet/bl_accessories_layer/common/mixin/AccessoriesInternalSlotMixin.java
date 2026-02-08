package net.oxcodsnet.bl_accessories_layer.common.mixin;

import io.wispforest.accessories.api.menu.AccessoriesBasedSlot;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.bl_accessories_layer.common.config.SlotConfig;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AccessoriesBasedSlot.class)
public class AccessoriesInternalSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true, remap = false)
    private void bl_allowLampsInCosmeticSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var self = (AccessoriesBasedSlot) (Object) this;
        if (!self.isCosmeticSlot()) return;
        if (LampRegistry.isLamp(stack) && SlotConfig.isAllowedSlot(self.slotType().name())) {
            cir.setReturnValue(true);
        }
    }
}
