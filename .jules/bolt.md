## 2025-05-15 - Avoid Map Churn in Hot Paths
**Learning:** Removing and re-inserting objects in a `ConcurrentHashMap` during the render loop (every frame) causes significant "map churn." This not only has higher overhead than simply keeping a few idle objects but can also lead to visual glitches if the object holds state (like smoothed positions) that gets reset upon re-insertion.

**Action:** Perform cleanup in lower-frequency tasks (like client ticks) and use dormancy timestamps to avoid expensive lookups for active entities.

## 2025-05-15 - Optimize Client Tick Cleanup with Timestamps
**Learning:** In Minecraft, `level.getEntity(id)` can be relatively expensive if called frequently for many entities. When cleaning up render-related trackers in a client tick, many of those trackers are likely still "active" (rendered recently).

**Action:** By tracking the `lastTimeMs` of the last render call, we can skip the `getEntity` check for trackers rendered within a recent window (e.g., 1 second), significantly reducing the number of lookups needed per tick.

## 2025-05-15 - Throttle Cleanup Tasks
**Learning:** Even with optimized checks (like timestamps), iterating over a collection every tick (20 times/second) can still be redundant for tasks like cleanup where a 1-second delay is perfectly acceptable.

**Action:** Throttle lower-priority maintenance tasks (like render tracker cleanup) using `level.getGameTime() % frequency == 0` to run them only at specified intervals (e.g., once every 20 ticks).
