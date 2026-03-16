# Known Issue: Multiple Lanterns in Different Slots

## Description
When a player configures multiple slots (e.g. `["belt", "charm"]`), they can place
lanterns into more than one slot simultaneously. This is a consequence of multi-slot support.

## Impact
- `BeltState` in the parent BL mod mirrors only ONE lamp per player
- `getBeltStackImpl` returns the first lamp found (iterates slots in config order)
- `tryToggleLanternImpl` removes the first lamp found
- The second lantern becomes "orphaned" — BL doesn't track it, no light/sync for it
- Not harmful but potentially confusing for players

## Status
Acknowledged as acceptable trade-off for configurable slots. Not fixing now.

## Possible Future Solutions
1. Block insertion if any configured slot already has a lamp (enforce single-lamp invariant)
2. Track all lamps across slots (requires BL parent API changes)
3. Add a config option: `allow_multiple_lanterns: true/false`
