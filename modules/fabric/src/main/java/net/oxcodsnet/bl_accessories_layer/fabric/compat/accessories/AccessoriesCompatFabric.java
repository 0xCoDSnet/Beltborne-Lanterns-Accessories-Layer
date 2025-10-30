package net.oxcodsnet.bl_accessories_layer.fabric.compat.accessories;

import io.wispforest.accessories.api.events.AccessoryChangeCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayer;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.fabric.BeltNetworking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accessories (WispForest) integration for Fabric.
 *
 * - Mirrors the belt slot state into BeltState for rendering/lighting.
 * - Keeps inventory semantics consistent with Accessories actions.
 * - Allows using the same toggle key (B) to unequip from the belt slot.
 */
public final class AccessoriesCompatFabric implements CompatibilityLayer {
    private static final String BELT = "belt";
    private static final java.util.Set<java.util.UUID> SYNCING = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
    private static final Map<UUID, ItemStack> PENDING_RESPAWN = new ConcurrentHashMap<>();

    @Override
    public String getModId() {
        return "accessories";
    }

    private static boolean isBeltSlot(SlotReference ref) {
        String name = ref.slotName();
        return BELT.equals(name) || (name != null && name.endsWith(":" + BELT));
    }

    @Override
    public void onInitialize() {
        // Register for any changes in the belt slot and mirror them to BeltState
        AccessoryChangeCallback.EVENT.register((prev, now, ref, change) -> {
            if (!(ref.entity() instanceof ServerPlayer player)) return;
            if (!isBeltSlot(ref)) return;

            boolean prevIsLamp = LampRegistry.isLamp(prev);
            boolean newIsLamp = LampRegistry.isLamp(now);

            if (!prevIsLamp && newIsLamp) {
                // A lamp was equipped into the Accessories belt slot
                // If we previously equipped via B (not via slot), return that lamp to the player's inventory,
                // but only if the newly equipped stack is different from the one we already track in BeltState.
                if (BeltState.hasLamp(player) && !SYNCING.contains(player.getUUID())) {
                    ItemStack current = BeltState.getLampStack(player);
                    boolean same = current != null && ItemStack.matches(current, now);
                    if (!same && !player.isCreative() && current != null && !current.isEmpty()) {
                        player.addItem(current);
                    }
                }
                // Mirror the new slot lamp into BeltState and persist
                BeltState.setLamp(player, now);
                BeltLanternSave.get(player.server).set(player.getUUID(), now);
                BeltNetworking.broadcastBeltState(player, now.getItem());

            } else if (prevIsLamp && !newIsLamp) {
                // A lamp was unequipped from the Accessories belt slot
                BeltState.setLamp(player, (Item) null);
                BeltLanternSave.get(player.server).set(player.getUUID(), (ItemStack) null);
                BeltNetworking.broadcastBeltState(player, null);

            } else if (prevIsLamp && newIsLamp) {
                // Lamp changed/replaced in the slot, update the mirrored state
                BeltState.setLamp(player, now);
                BeltLanternSave.get(player.server).set(player.getUUID(), now);
                BeltNetworking.broadcastBeltState(player, now.getItem());
            }
        });

        // Capture respawn state before Beltborne handles death drops so we can skip duplicate drops
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            if (!BeltState.hasLamp(oldPlayer)) return;

            var slotStack = getBeltStack(newPlayer);
            if (slotStack.isEmpty()) return;

            ItemStack stack = slotStack.get();
            if (!LampRegistry.isLamp(stack)) return;

            PENDING_RESPAWN.put(newPlayer.getUUID(), stack.copy());

            BeltState.setLamp(oldPlayer, (ItemStack) null);
            BeltLanternSave.get(oldPlayer.server).set(oldPlayer.getUUID(), (ItemStack) null);
        });

        // Reapply the preserved lamp state once the new player entity is ready
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            ItemStack pending = PENDING_RESPAWN.remove(newPlayer.getUUID());
            if (pending == null || pending.isEmpty()) return;

            BeltState.setLamp(newPlayer, pending);
            BeltLanternSave.get(newPlayer.server).set(newPlayer.getUUID(), pending);
            BeltNetworking.broadcastBeltState(newPlayer, pending.getItem());
        });
        BLMod.LOGGER.info("Accessories integration active [Fabric]");
    }

    @Override
    public boolean tryToggleLantern(ServerPlayer player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return false;
        ItemStack stack = ref.getStack();
        if (LampRegistry.isLamp(stack)) {
            // Programmatic unequip: explicitly return the item in survival
            ItemStack toReturn = stack.copy();
            ref.setStack(ItemStack.EMPTY);
            if (!player.isCreative() && !toReturn.isEmpty()) {
                player.addItem(toReturn);
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<ItemStack> getBeltStack(ServerPlayer player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return Optional.empty();
        return Optional.of(ref.getStack());
    }

    @Override
    public void syncToggleOn(ServerPlayer player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return;
        if (!ref.getStack().isEmpty()) return;
        ItemStack stored = BeltState.getLampStack(player);
        if (stored == null || stored.isEmpty()) return;
        SYNCING.add(player.getUUID());
        try {
            ref.setStack(stored);
        } finally {
            SYNCING.remove(player.getUUID());
        }
    }
}
