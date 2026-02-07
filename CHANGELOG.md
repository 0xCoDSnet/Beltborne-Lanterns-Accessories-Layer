v1.0.6 — _Stability & compatibility improvements_
### Bug Fixes
- Fixed lantern disappearing when inventory is full — now drops at your feet
- Fixed lantern duplication/desync after death and respawn
- Fixed lantern state clearing during internal sync operations
- Fixed potential item duplication via mirrored state not being cleared on death
- Fixed `getBeltStackImpl` returning non-empty Optional with an empty ItemStack
- Fixed NeoForge reload listener registration causing potential crash on startup
- Fixed duplicate `no_renderer` registration producing error logs on resource reload

### Improvements
- Added defensive try-catch for Fabric reload listener registration
- Added `accessories` as a required dependency in mod metadata (Fabric & NeoForge)
- Migrated from Architectury Loom to Multiloader build system
