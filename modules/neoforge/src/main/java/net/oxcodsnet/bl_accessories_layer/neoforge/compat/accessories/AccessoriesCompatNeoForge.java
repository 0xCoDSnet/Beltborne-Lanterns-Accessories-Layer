package net.oxcodsnet.bl_accessories_layer.neoforge.compat.accessories;

import io.wispforest.accessories.api.events.AccessoryChangeCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayer;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.neoforge.BeltNetworking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accessories (WispForest) integration for NeoForge.
 */
public final class AccessoriesCompatNeoForge implements CompatibilityLayer {
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
        AccessoryChangeCallback.EVENT.register((prev, now, ref, change) -> {
            if (!(ref.entity() instanceof ServerPlayer player)) return;
            if (!isBeltSlot(ref)) return;

            boolean prevIsLamp = LampRegistry.isLamp(prev);
            boolean newIsLamp = LampRegistry.isLamp(now);

            if (!prevIsLamp && newIsLamp) {
                if (BeltState.hasLamp(player) && !SYNCING.contains(player.getUUID())) {
                    ItemStack current = BeltState.getLampStack(player);
                    boolean same = current != null && ItemStack.matches(current, now);
                    if (!same && !player.isCreative() && current != null && !current.isEmpty()) {
                        player.addItem(current);
                    }
                }
                BeltState.setLamp(player, now);
                BeltLanternSave.get(player.server).set(player.getUUID(), now);
                BeltNetworking.broadcastBeltState(player, now.getItem());
            } else if (prevIsLamp && !newIsLamp) {
                BeltState.setLamp(player, (Item) null);
                BeltLanternSave.get(player.server).set(player.getUUID(), (ItemStack) null);
                BeltNetworking.broadcastBeltState(player, null);
            } else if (prevIsLamp && newIsLamp) {
                BeltState.setLamp(player, now);
                BeltLanternSave.get(player.server).set(player.getUUID(), now);
                BeltNetworking.broadcastBeltState(player, now.getItem());
            }
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, AccessoriesCompatNeoForge::handleClone);
        NeoForge.EVENT_BUS.addListener(AccessoriesCompatNeoForge::handleRespawn);

        BLMod.LOGGER.info("Accessories integration active [NeoForge]");
    }

    private static void handleClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        if (!BeltState.hasLamp(oldPlayer)) return;

        var slotStack = getBeltStack(newPlayer);
        if (slotStack.isEmpty()) return;

        ItemStack stack = slotStack.get();
        if (!LampRegistry.isLamp(stack)) return;

        PENDING_RESPAWN.put(newPlayer.getUUID(), stack.copy());

        BeltState.setLamp(oldPlayer, (ItemStack) null);
        BeltLanternSave.get(oldPlayer.server).set(oldPlayer.getUUID(), (ItemStack) null);
    }

    private static void handleRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack pending = PENDING_RESPAWN.remove(player.getUUID());
        if (pending == null || pending.isEmpty()) return;

        BeltState.setLamp(player, pending);
        BeltLanternSave.get(player.server).set(player.getUUID(), pending);
        BeltNetworking.broadcastBeltState(player, pending.getItem());
    }

    @Override
    public boolean tryToggleLantern(ServerPlayer player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return false;
        ItemStack stack = ref.getStack();
        if (LampRegistry.isLamp(stack)) {
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
