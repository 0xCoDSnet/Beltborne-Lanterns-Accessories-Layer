v1.2.0 — Render toggle & cosmetic slot support
### New Features
- Render toggle support — pressing the visibility toggle button in the Accessories UI now hides/shows the BL lantern render
- Cosmetic slot support — placing a different lantern in the cosmetic slot now overrides the rendered lantern visual

---

v1.1.0 — Configurable accessory slots
### New Features
- Added configurable slot support — modpack makers can now specify which Accessories slots accept lanterns via config/bl_accessories_layer.json
- Default configuration uses ["belt"] only, preserving full backward compatibility
- Config file is auto-generated on first launch with default values
- Slot validation is handled via mixin — no manual datapacks needed for configured slots
- Removed hardcoded belt.json item tag in favor of dynamic validation

### Bug Fixes
- Fixed lanterns stacking beyond 1 in accessory slots

---

v1.0.5 — _Belt sync fixes_
### Bug Fixes
- Lantern no longer disappears when inventory is full — drops at your feet instead
- Fixed lantern duplication/desync after death and respawn
- Fixed lantern state clearing during internal sync operations
