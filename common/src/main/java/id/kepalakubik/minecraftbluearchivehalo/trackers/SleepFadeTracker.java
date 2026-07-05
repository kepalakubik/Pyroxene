package id.kepalakubik.minecraftbluearchivehalo.trackers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class SleepFadeTracker {
    private static final Map<Integer, SleepFadeTracker> TRACKERS = new ConcurrentHashMap<>();

    private static final float FADE_OUT_SECONDS = 0.5f; // Secs to fade out (falling asleep)
    private static final float FADE_OUT_TICKS = FADE_OUT_SECONDS * 20f; // 10 ticks
    private static final float FADE_IN_SECONDS = 1.5f; // Secs to fade in (waking up)

    private boolean lastSleeping = false;
    private long wakeTimeTick = -1L;
    private float alphaAtWake = 1.0f;
    private float lastAlpha = 1.0f;

    /**
     * Call every render frame.
     *
     * @param isSleeping Is the entity sleeping
     * @param sleepTimer Player sleep timer. Pass 0 for non-player
     * @param currentTick Current Minecraft tick
     * @return alpha value 0.0 (invisible) <-> 1.0 (opaque)
     */
    public float computeAlpha(boolean isSleeping, int sleepTimer, long currentTick) {
        if (isSleeping) {
            // Fade out. Linear alpha from 1 to 0 as sleepTimer climbs 0
            float fadeProgress = Math.min(1.0f, sleepTimer / FADE_OUT_TICKS);
            float alpha = 1.0f - fadeProgress;

            // Cache the alpha so we know where to start fading back in
            alphaAtWake = alpha;
            lastSleeping = true;
            lastAlpha = alpha;

            return alpha;
        } else {
            // Player is awake
            if (lastSleeping) {
                // Start the fade-in timer at whatever alpha we ended on
                wakeTimeTick = currentTick;
                lastSleeping = false;
            }

            if (wakeTimeTick < 0) {
                // Was never sleeping this session
                lastAlpha = 1.0f;
                return 1.0f;
            }

            // Fade in. Linear from alphaAtWake to 1.0 over FADE_IN_SECONDS
            float elapsed = (currentTick - wakeTimeTick) / (FADE_IN_SECONDS * 20f);
            if (elapsed >= 1.0f) {
                wakeTimeTick = -1L;
                alphaAtWake = 1.0f;
                lastAlpha = 1.0f;
                return 1.0f;
            }

            float alpha = alphaAtWake + (1.0f - alphaAtWake) * elapsed;
            lastAlpha = alpha;
            return alpha;
        }
    }

    /** True once the entity has never slept, or has fully faded back in after waking. */
    public boolean isFadeComplete() {
        return !lastSleeping && wakeTimeTick < 0;
    }

    public float getLastAlpha() {
        return lastAlpha;
    }

    public static SleepFadeTracker getOrCreate(int entityId) {
        return TRACKERS.computeIfAbsent(entityId, _ -> new SleepFadeTracker());
    }

    public static SleepFadeTracker get(int entityId) {
        return TRACKERS.get(entityId);
    }

    public static void removeIf(Predicate<Integer> shouldRemove) {
        TRACKERS.entrySet().removeIf(entry ->
            shouldRemove.test(entry.getKey()) || entry.getValue().isFadeComplete());
    }
}
