package net.oxcodsnet.bl_accessories_layer.common.mixin;

import io.wispforest.accessories.api.menu.AccessoriesBasedSlot;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AccessoriesBasedSlot.class)
public class AccessoriesSlotMixin {
    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"), cancellable = true)
    private void bl_limitLampStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (LampRegistry.isLamp(stack)) {
            cir.setReturnValue(1);
        }
    }
}
