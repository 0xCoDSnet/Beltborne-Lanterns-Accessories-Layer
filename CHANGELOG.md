v1.2.0 — _Render toggle & cosmetic slot support_
### New Features
- Render toggle support — pressing the visibility toggle button in the Accessories UI now hides/shows the BL lantern render
- Cosmetic slot support — placing a different lantern in the cosmetic slot now overrides the rendered lantern visual

---

v1.1.0 — _Configurable accessory slots_
### New Features
- Added configurable slot support — modpack makers can now specify which Accessories slots accept lanterns via `config/bl_accessories_layer.json`
- Default configuration uses `["belt"]` only, preserving full backward compatibility
- Config file is auto-generated on first launch with default values
- Slot validation is handled via mixin — no manual datapacks needed for configured slots
- Removed hardcoded `belt.json` item tag in favor of dynamic validation

### Bug Fixes
- Fixed lanterns stacking beyond 1 in accessory slots

---

v1.0.7 — _Respawn reliability fixes_
### Bug Fixes
- Fixed race condition on NeoForge where Clone event handler could read belt slot before Accessories finished copying attachment data, potentially losing lamp state on death
- Fixed phantom lamp after respawn where mirror state could be restored without an actual item in the belt slot

---

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
