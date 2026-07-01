## 2025-05-14 - SleepFadeTracker Memory Leak Fix
**Learning:** Storing entity-related data in a simple `HashMap` within a renderer leads to memory leaks because renderers are often singleton-like and never cleared, while entities are frequently spawned and despawned.
**Action:** Use a centralized tracker registry with a `removeIf` cleanup mechanism called from platform-specific client tick events (Fabric's `ClientTickEvents.START_CLIENT_TICK` and NeoForge's `ClientTickEvent.Pre`).

## 2025-05-14 - Timing Consistency
**Learning:** Using `System.currentTimeMillis()` for animation or physics timing can be inconsistent with the game loop's own perception of time.
**Action:** Prefer `net.minecraft.util.Util.getMillis()` for game-consistent timing in Minecraft mods.
