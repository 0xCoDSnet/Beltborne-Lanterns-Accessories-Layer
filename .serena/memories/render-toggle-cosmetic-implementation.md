# Render Toggle & Cosmetic Slot Implementation

## What was done
Implemented two features from the TODO in `BL_Accessories_Layer.init()`:

1. **Render Toggle**: When user clicks visibility toggle button in Accessories UI → BL lantern render hides/shows
2. **Cosmetic Slot**: When user places a different lantern in cosmetic slot → BL renders that lantern instead

## Architecture

### Key formula
```
broadcast_item = shouldRender ? (cosmetic_lamp ?? functional_lamp) : null
```

### Changes made
- `AbstractAccessoriesCompat` — added `reevaluateAndBroadcast()` helper, `getCosmeticStack()` and `isSlotRenderEnabled()` abstract methods, static `getInstance()` for mixin access
- `AccessoriesCompatFabric/NeoForge` — implemented new abstract methods using `AccessoriesCapability.get()`, `container.getCosmeticAccessories()`, `container.shouldRender()`
- `AccessoriesContainerMixin` — mixin into `AccessoriesContainerImpl.setShouldRender()` (TAIL) → triggers `reevaluateAndBroadcast`
- Both platform classes register `ContainersChangeCallback.EVENT` for cosmetic slot changes (since `AccessoryChangeCallback` does NOT fire for cosmetic changes)

### Accessories API notes (v1.3.4-beta for MC 1.21.8)
- `AccessoryChangeCallback` only fires for functional slot changes, NOT cosmetic
- `setShouldRender` is called server-side only (from `SyncCosmeticToggle.handlePacket()`)
- Entity access: `container.capability().entity()`
- Container by string name: `capability.getContainers().get("belt")`
- `ContainersChangeCallback` fires at end of tick after all container changes
