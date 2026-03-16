# BL: Accessories Layer — Project Overview

## Purpose
Compatibility addon bridging **Beltborne Lanterns** (BL) with **Accessories** (WispForest).
Allows lanterns to be worn in the Accessories *Belt* slot and mirrors state to BL's internal systems.

## Tech Stack
- Java 21, Minecraft 1.21.8
- Multiloader: Fabric (fabric-loom) + NeoForge (net.neoforged.moddev)
- Gradle, buildSrc convention plugins
- SPI (ServiceLoader) for CompatibilityLayer registration

## Architecture
```
modules/
├── common/     — Shared logic (AbstractAccessoriesCompat, AccessoriesClientCompat, RendererReloadScheduler)
├── fabric/     — Fabric entry points, event handlers, SPI service file
└── neoforge/   — NeoForge entry points, event handlers, SPI service file
```

## Key Classes
- `AbstractAccessoriesCompat<P,S>` — Core integration logic (slot change, toggle, death/respawn)
- `AccessoriesClientCompat` — Client-side renderer suppression for Accessories
- `AccessoriesRendererReloadScheduler` — Delayed retry for renderer cache refresh
- `AccessoriesCompatFabric` / `AccessoriesCompatNeoForge` — Platform implementations
- `AccessoriesRendererReloadHandler` — Platform-specific tick + resource reload handling

## Parent Mod API (BL)
- `CompatibilityLayer` — SPI interface: getModId, onInitialize, tryToggleLantern, syncToggleOn, getBeltStack
- `BeltState` — Runtime ConcurrentHashMap<UUID, ItemStack> cache
- `BeltLanternSave` — Persistent SavedData storage
- `LampRegistry` — Registry of valid lamp items
- `BeltNetworking` — Network sync (platform-specific)

## Build Commands
```bash
cd /home/alexey/Projects/mods/Beltborne-Lanterns-Accessories-Layer/1.21.8-multiloader
./gradlew build
./gradlew :modules:fabric:runClient
./gradlew :modules:neoforge:runClient
```

## Code Style
- Final classes, private constructors for utility classes
- AtomicBoolean for registration guards
- ConcurrentHashMap for thread-safe state
- Generics for platform abstraction (P=player, S=stack)
