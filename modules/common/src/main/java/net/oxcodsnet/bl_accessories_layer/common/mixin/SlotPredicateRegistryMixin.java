package net.oxcodsnet.bl_accessories_layer.common.mixin;

import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.bl_accessories_layer.common.config.SlotConfig;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.wispforest.accessories.api.slot.SlotPredicateRegistry;

@Mixin(SlotPredicateRegistry.class)
public class SlotPredicateRegistryMixin {
    @Inject(method = "canInsertIntoSlot", at = @At("HEAD"), cancellable = true, remap = false)
    private static void bl_allowConfiguredLamps(ItemStack stack, SlotReference reference,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (LampRegistry.isLamp(stack) && SlotConfig.isAllowedSlot(reference.slotName())) {
            cir.setReturnValue(true);
        }
    }
}
