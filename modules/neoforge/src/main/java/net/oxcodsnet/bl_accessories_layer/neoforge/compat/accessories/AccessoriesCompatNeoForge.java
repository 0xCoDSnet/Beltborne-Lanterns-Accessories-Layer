package net.oxcodsnet.bl_accessories_layer.neoforge.compat.accessories;

import io.wispforest.accessories.api.events.AccessoryChangeCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.oxcodsnet.bl_accessories_layer.common.config.SlotConfig;
import net.oxcodsnet.bl_accessories_layer.common.compat.accessories.AbstractAccessoriesCompat;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayer;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.neoforge.BeltNetworking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Accessories (WispForest) integration for NeoForge.
 */
public final class AccessoriesCompatNeoForge extends AbstractAccessoriesCompat<ServerPlayer, ItemStack> implements CompatibilityLayer {
    public AccessoriesCompatNeoForge() {
        super("NeoForge");
    }

    @Override
    public String getModId() {
        return modIdImpl();
    }

    @Override
    public void onInitialize() {
        initializeImpl();
    }

    @Override
    public boolean tryToggleLantern(ServerPlayer player) {
        return tryToggleLanternImpl(player);
    }

    @Override
    public Optional<ItemStack> getBeltStack(ServerPlayer player) {
        return getBeltStackImpl(player);
    }

    @Override
    public void syncToggleOn(ServerPlayer player) {
        syncToggleOnImpl(player);
    }

    @Override
    protected void registerEvents() {
        registerAccessoryCallbacks();
        registerRespawnCallbacks();
    }

    private void registerAccessoryCallbacks() {
        AccessoryChangeCallback.EVENT.register((previous, current, reference, change) -> {
            if (!(reference.entity() instanceof ServerPlayer player)) return;
            handleSlotChange(player, wrap(reference), previous, current);
        });
    }

    private void registerRespawnCallbacks() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, this::onClone);
        NeoForge.EVENT_BUS.addListener(this::onRespawn);
    }

    private void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        handleClone(oldPlayer, newPlayer);
    }

    private void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        handleRespawn(player);
    }

    @Override
    protected List<SlotAccess<ItemStack>> createSlotAccessList(ServerPlayer player) {
        return SlotConfig.allowedSlots().stream()
            .map(slot -> (SlotAccess<ItemStack>) new SlotReferenceAccess(
                SlotReference.of(player, slot, 0)))
            .toList();
    }

    @Override
    protected boolean hasMirroredLamp(ServerPlayer player) {
        return BeltState.hasLamp(player);
    }

    @Override
    protected ItemStack getMirroredStack(ServerPlayer player) {
        return BeltState.getLampStack(player);
    }

    @Override
    protected void setMirroredLamp(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            BeltState.setLamp(player, (ItemStack) null);
            BeltLanternSave.get(player.getServer()).set(player.getUUID(), (ItemStack) null);
        } else {
            BeltState.setLamp(player, stack);
            BeltLanternSave.get(player.getServer()).set(player.getUUID(), stack);
        }
    }

    @Override
    protected boolean isCreative(ServerPlayer player) {
        return player.isCreative();
    }

    @Override
    protected void giveBack(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    protected void broadcast(ServerPlayer player, ItemStack stack) {
        BeltNetworking.broadcastBeltState(player, stack == null || stack.isEmpty() ? null : stack.getItem());
    }

    @Override
    protected boolean isLamp(ItemStack stack) {
        return stack != null && LampRegistry.isLamp(stack);
    }

    @Override
    protected boolean isEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    @Override
    protected ItemStack copyStack(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    protected ItemStack emptyStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean stacksEqual(ItemStack first, ItemStack second) {
        if (first == second) return true;
        if (first == null || second == null) return false;
        return ItemStack.isSameItemSameComponents(first, second);
    }

    @Override
    protected UUID getPlayerId(ServerPlayer player) {
        return player.getUUID();
    }

    private SlotAccess<ItemStack> wrap(SlotReference reference) {
        return new SlotReferenceAccess(reference);
    }

    private static final class SlotReferenceAccess implements SlotAccess<ItemStack> {
        private final SlotReference delegate;

        private SlotReferenceAccess(SlotReference delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isValid() {
            return delegate != null && delegate.isValid();
        }

        @Override
        public String slotName() {
            return delegate.slotName();
        }

        @Override
        public ItemStack getStack() {
            return delegate.getStack();
        }

        @Override
        public void setStack(ItemStack stack) {
            delegate.setStack(stack);
        }
    }
}
