package net.oxcodsnet.bl_accessories_layer.common.compat.accessories;

import net.oxcodsnet.bl_accessories_layer.common.config.SlotConfig;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractAccessoriesCompat<P, S> {

    private static volatile AbstractAccessoriesCompat<?, ?> instance;

    private final Set<UUID> syncing = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, S> pendingRespawn = new ConcurrentHashMap<>();
    private final String platformName;

    protected AbstractAccessoriesCompat(String platformName) {
        this.platformName = platformName;
        instance = this;
    }

    /**
     * Returns the active instance (set during construction of the platform-specific subclass).
     */
    public static AbstractAccessoriesCompat<?, ?> getInstance() {
        return instance;
    }

    public final String modIdImpl() {
        return "accessories";
    }

    public final boolean handlesItemOnDeathImpl() {
        return true;
    }

    public final void initializeImpl() {
        registerEvents();
        BLMod.LOGGER.info("Accessories integration active [{}]", platformName);
    }

    protected abstract void registerEvents();

    protected final void handleSlotChange(P player, SlotAccess<S> reference, S previous, S current) {
        if (player == null) return;
        if (!reference.isValid()) return;
        if (!isAllowedSlot(reference)) return;

        boolean prevIsLamp = isLamp(previous);
        boolean newIsLamp = isLamp(current);

        UUID playerId = getPlayerId(player);

        if (!prevIsLamp && newIsLamp) {
            if (hasMirroredLamp(player) && !syncing.contains(playerId)) {
                S existing = getMirroredStack(player);
                boolean same = existing != null && stacksEqual(existing, current);
                if (!same && !isCreative(player) && existing != null && !isEmpty(existing)) {
                    giveBack(player, copyStack(existing));
                }
            }
            setMirroredLamp(player, copyStack(current));
            reevaluateAndBroadcast(player);
        } else if (prevIsLamp && !newIsLamp) {
            if (!syncing.contains(playerId)) {
                setMirroredLamp(player, null);
                reevaluateAndBroadcast(player);
            }
        } else if (prevIsLamp && newIsLamp) {
            if (!syncing.contains(playerId)) {
                setMirroredLamp(player, copyStack(current));
                reevaluateAndBroadcast(player);
            }
        }
    }

    /**
     * Re-evaluates what lamp should be rendered and broadcasts the result.
     * Checks render toggle and cosmetic slot override.
     */
    public final void reevaluateAndBroadcast(P player) {
        for (SlotAccess<S> ref : createSlotAccessList(player)) {
            if (!ref.isValid()) continue;
            if (!isAllowedSlot(ref)) continue;

            S functional = ref.getStack();
            if (!isLamp(functional)) continue;

            if (!isSlotRenderEnabled(player, ref.slotName(), 0)) {
                broadcast(player, null);
                return;
            }

            Optional<S> cosmetic = getCosmeticStack(player, ref.slotName(), 0);
            if (cosmetic.isPresent()) {
                broadcast(player, cosmetic.get());
            } else {
                broadcast(player, functional);
            }
            return;
        }
        broadcast(player, null);
    }

    protected final void handleClone(P oldPlayer, P newPlayer) {
        if (!hasMirroredLamp(oldPlayer)) return;

        Optional<S> slotStack = getBeltStackImpl(newPlayer);
        if (slotStack.isEmpty() || !isLamp(slotStack.get())) {
            setMirroredLamp(oldPlayer, null);
            return;
        }

        pendingRespawn.put(getPlayerId(newPlayer), copyStack(slotStack.get()));

        setMirroredLamp(oldPlayer, null);
    }

    protected final void handleRespawn(P player) {
        S pending = pendingRespawn.remove(getPlayerId(player));
        if (pending == null || isEmpty(pending)) return;

        Optional<S> currentBelt = getBeltStackImpl(player);
        if (currentBelt.isEmpty() || !isLamp(currentBelt.get())) {
            return;
        }

        setMirroredLamp(player, copyStack(currentBelt.get()));
        reevaluateAndBroadcast(player);
    }

    public final boolean tryToggleLanternImpl(P player) {
        for (SlotAccess<S> reference : createSlotAccessList(player)) {
            if (!reference.isValid()) continue;

            S stack = reference.getStack();
            if (!isLamp(stack)) continue;

            S toReturn = copyStack(stack);
            reference.setStack(emptyStack());
            if (!isCreative(player) && !isEmpty(toReturn)) {
                giveBack(player, toReturn);
            }
            return true;
        }
        return false;
    }

    public final Optional<S> getBeltStackImpl(P player) {
        for (SlotAccess<S> reference : createSlotAccessList(player)) {
            if (!reference.isValid()) continue;
            if (!isAllowedSlot(reference)) continue;
            S stack = reference.getStack();
            if (!isEmpty(stack) && isLamp(stack)) return Optional.of(stack);
        }
        return Optional.empty();
    }

    public final void syncToggleOnImpl(P player) {
        S stored = getMirroredStack(player);
        if (stored == null || isEmpty(stored)) return;

        for (SlotAccess<S> reference : createSlotAccessList(player)) {
            if (!reference.isValid()) continue;
            if (!isEmpty(reference.getStack())) continue;

            UUID playerId = getPlayerId(player);
            syncing.add(playerId);
            try {
                reference.setStack(copyStack(stored));
            } finally {
                syncing.remove(playerId);
            }
            return;
        }
    }

    protected final boolean isAllowedSlot(SlotAccess<S> reference) {
        return SlotConfig.isAllowedSlot(reference.slotName());
    }

    protected abstract List<SlotAccess<S>> createSlotAccessList(P player);

    /**
     * Returns the lamp from the cosmetic slot, if present and valid.
     */
    protected abstract Optional<S> getCosmeticStack(P player, String slotName, int index);

    /**
     * Checks whether the render toggle for the given slot is enabled.
     */
    protected abstract boolean isSlotRenderEnabled(P player, String slotName, int index);

    protected abstract boolean hasMirroredLamp(P player);

    protected abstract S getMirroredStack(P player);

    protected abstract void setMirroredLamp(P player, S stack);

    protected abstract boolean isCreative(P player);

    protected abstract void giveBack(P player, S stack);

    protected abstract void broadcast(P player, S stack);

    protected abstract boolean isLamp(S stack);

    protected abstract boolean isEmpty(S stack);

    protected abstract S copyStack(S stack);

    protected abstract S emptyStack();

    protected abstract boolean stacksEqual(S first, S second);

    protected abstract UUID getPlayerId(P player);

    protected interface SlotAccess<S> {
        boolean isValid();

        String slotName();

        S getStack();

        void setStack(S stack);
    }
}
