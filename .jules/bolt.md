# Performance Learning Journal

## SleepFadeTracker Optimization (2024-05-22)
**Problem:** `SleepFadeTracker.getOrCreate()` was called every render frame for all halo-wearing entities, causing unnecessary `ConcurrentHashMap` lookups and maintaining tracker instances for entities that weren't sleeping.

**Solution:**
- Added `isIdle()` to `SleepFadeTracker` to detect when a transition is finished (alpha = 1.0).
- Modified `HaloRenderer` to only create/retrieve trackers when `isSleeping` is true or if a tracker already exists (to finish a fade-in).
- Explicitly removed trackers when they become idle.

**Result:** Reduced memory usage and CPU cycles by ensuring the `TRACKERS` map only contains active transition states.

## HaloHeadSpringTracker Optimization (2024-05-22)
**Problem:** `HaloHeadSpringTracker.removeIf()` was iterating through entities in the client tick even when the spring feature was disabled in config.

**Solution:**
- Added config check in `BlueArchiveHalosPyroxeneClient` (Fabric & NeoForge).
- If `enableHaloSpring` is false, skip `removeIf` and just clear the map if it's not empty.

**Result:** Avoided unnecessary entity lookups and map iterations when the feature is disabled.
