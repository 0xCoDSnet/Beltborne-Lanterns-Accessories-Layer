package net.oxcodsnet.bl_accessories_layer.common.compat.accessories;

import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared Accessories (WispForest) integration logic used by each loader.
 *
 * @param <P> platform specific player type
 * @param <S> platform specific stack type
 */
public abstract class AbstractAccessoriesCompat<P, S> {
    protected static final String BELT = "belt";

    private final Set<UUID> syncing = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, S> pendingRespawn = new ConcurrentHashMap<>();
    private final String platformName;

    protected AbstractAccessoriesCompat(String platformName) {
        this.platformName = platformName;
    }

    public final String modIdImpl() {
        return "accessories";
    }

    public final void initializeImpl() {
        registerEvents();
        BLMod.LOGGER.info("Accessories integration active [{}]", platformName);
    }

    protected abstract void registerEvents();

    protected final void handleSlotChange(P player, SlotAccess<S> reference, S previous, S current) {
        if (player == null) return;
        if (!reference.isValid()) return;
        if (!isBeltSlot(reference)) return;

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
            broadcast(player, current);
        } else if (prevIsLamp && !newIsLamp) {
            if (!syncing.contains(playerId)) {
                setMirroredLamp(player, null);
                broadcast(player, null);
            }
        } else if (prevIsLamp && newIsLamp) {
            if (!syncing.contains(playerId)) {
                setMirroredLamp(player, copyStack(current));
                broadcast(player, current);
            }
        }
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
            // Belt slot is empty after respawn — don't restore phantom lamp state
            return;
        }

        setMirroredLamp(player, copyStack(currentBelt.get()));
        broadcast(player, currentBelt.get());
    }

    public final boolean tryToggleLanternImpl(P player) {
        SlotAccess<S> reference = createSlotAccess(player);
        if (!reference.isValid()) return false;

        S stack = reference.getStack();
        if (!isLamp(stack)) {
            return false;
        }

        S toReturn = copyStack(stack);
        reference.setStack(emptyStack());
        if (!isCreative(player) && !isEmpty(toReturn)) {
            giveBack(player, toReturn);
        }
        return true;
    }

    public final Optional<S> getBeltStackImpl(P player) {
        SlotAccess<S> reference = createSlotAccess(player);
        if (!reference.isValid()) return Optional.empty();
        if (!isBeltSlot(reference)) return Optional.empty();
        S stack = reference.getStack();
        if (isEmpty(stack)) return Optional.empty();
        return Optional.of(stack);
    }

    public final void syncToggleOnImpl(P player) {
        SlotAccess<S> reference = createSlotAccess(player);
        if (!reference.isValid()) return;
        if (!isEmpty(reference.getStack())) return;

        S stored = getMirroredStack(player);
        if (stored == null || isEmpty(stored)) return;

        UUID playerId = getPlayerId(player);
        syncing.add(playerId);
        try {
            reference.setStack(copyStack(stored));
        } finally {
            syncing.remove(playerId);
        }
    }

    protected final boolean isBeltSlot(SlotAccess<S> reference) {
        String name = reference.slotName();
        return BELT.equals(name) || (name != null && name.endsWith(":" + BELT));
    }

    protected abstract SlotAccess<S> createSlotAccess(P player);

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
