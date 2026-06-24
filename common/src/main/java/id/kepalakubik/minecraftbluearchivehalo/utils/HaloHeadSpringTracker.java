package id.kepalakubik.minecraftbluearchivehalo.utils;

import net.minecraft.util.Mth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class HaloHeadSpringTracker {
    /**
     * Spring stiffness (k) — how hard the spring pulls the halo back, in 1/s².
     * Higher = tighter/faster spring.
     * Recommended range: 80–300
     */
    public static float SPRING_STIFFNESS = 300;

    /**
     * Spring damping (d) — how quickly oscillation dies out, in 1/s.
     * Critical damping (no bounce) = 2 × √SPRING_STIFFNESS.
     *   e.g. k=150 → critical d ≈ 24.5
     * Below critical → underdamped (springy/bouncy). Recommended: 10–15.
     * At or above critical → no bounce, smooth settle.
     */
    public static float SPRING_DAMPING = 14;

    /**
     * How far (in blocks) the halo dips toward the head the instant the
     * player leaves the ground — the "squash from below" at takeoff.
     * Recommended range: 0.03–0.08
     */
    public static float JUMP_SQUASH_DEPTH = 0.05f;

    /**
     * How far (in blocks) the halo stretches away from the head while
     * the player is falling — the "stretch from above" on the way down.
     * Recommended range: 0.1–0.2
     */
    public static float JUMP_STRETCH_HEIGHT = 0.15f;

    /**
     * Vertical speed (blocks/tick, i.e. the wearer's raw motionY) at which
     * squash/stretch reaches its full configured depth/height. Vanilla's
     * default jump impulse is ~0.42 blocks/tick. Lower this for a more
     * exaggerated effect on small hops, raise it to require a faster
     * jump/fall before the effect maxes out.
     */
    public static float JUMP_VELOCITY_REFERENCE = 0.35f;

    /**
     * Maximum physics sub-step size in seconds.
     * Explicit Euler integration goes unstable when dt ≥ 2/√k.
     * With k=300, that threshold is ≈ 0.115 s — the old 100 ms cap was
     * dangerously close. Capping each sub-step at 1/120 s (≈ 8.3 ms)
     * keeps us ~14× below the instability limit regardless of stiffness,
     * so lag spikes can no longer fling the halo.
     */
    private static final float MAX_SUBSTEP_S = 1f / 120f; // ≈ 8.3 ms

    // ConcurrentHashMap: render thread calls update(), game thread calls remove()
    private static final Map<Integer, SmoothState> STATES = new ConcurrentHashMap<>();

    public static class SmoothState {
        // Smoothed position & rotation
        public double x, y, z;
        public float yaw, pitch;
        // Spring velocities
        public double vx, vy, vz;       // world units per second
        public float  vyaw, vpitch;     // degrees per second
        public float jumpOffset, vjumpOffset;
        public long lastTimeMs = -1;    // sentinel: not yet initialized
    }

    /** Called every render frame from the renderer. */
    public static SmoothState update(
        int id,
        double tx, double ty, double tz,
        float tyaw, float tpitch,
        boolean isOnGround,
        double verticalVelocity
    ) {
        SmoothState s = STATES.computeIfAbsent(id, _ -> new SmoothState());

        long now = System.currentTimeMillis();

        if (s.lastTimeMs < 0) {
            // First frame — snap to target, zero velocity
            s.x = tx; s.y = ty; s.z = tz;
            s.yaw = tyaw; s.pitch = tpitch;
            s.vx = s.vy = s.vz = 0;
            s.vyaw = s.vpitch = 0;
            s.jumpOffset = 0; s.vjumpOffset = 0;
            s.lastTimeMs = now;
            return s;
        }

        long elapsed = now - s.lastTimeMs;
        s.lastTimeMs = now;

        // FIX: If the tracker was dormant for too long the halo simply wasn't being renderer
        // Most commonly because the local player switched to
        // first-person view. In that case the stored position is arbitrarily
        // stale, so spring-interpolating back to the player would produce the
        // "halo runs across the map" artifact. Snap to target instead.
        // 500 ms is comfortably above any realistic lag spike (~100 ms cap below)
        // while still catching even a brief camera-mode switch.
        if (elapsed > 500) {
            s.x = tx; s.y = ty; s.z = tz;
            s.yaw = tyaw; s.pitch = tpitch;
            s.vx = s.vy = s.vz = 0;
            s.vyaw = s.vpitch = 0;
            s.jumpOffset = 0; s.vjumpOffset = 0;
            return s;
        }

        // Total dt in seconds, still capped so a single huge spike can't
        // inject energy even before sub-stepping kicks in.
        float totalDt = Math.min(elapsed / 1000f, 0.1f);

        // Sub-step the spring integration so each individual step is well
        // below the Euler stability limit (2/√k ≈ 0.115 s for k=300).
        // This is the primary fix: lag spikes previously produced one large
        // dt≈0.1 s step that sat right at the instability boundary; now the
        // same interval is split into ~12 × 8.3 ms steps, each trivially stable.
        int steps = (totalDt <= MAX_SUBSTEP_S)
            ? 1
            : (int) Math.ceil(totalDt / MAX_SUBSTEP_S);
        float dt = totalDt / steps;

        // Compute once for the Jump squash/stretch spring
        float targetJump;
        if (isOnGround) {
            targetJump = 0f;
        } else {
            float v = (float) (verticalVelocity / JUMP_VELOCITY_REFERENCE);
            targetJump = v >= 0
                ? -JUMP_SQUASH_DEPTH * Mth.clamp(v, 0f, 1f)
                : JUMP_STRETCH_HEIGHT * Mth.clamp(-v, 0f, 1f);
        }

        // Y vertical spring (keep it snap. jumpoffset already do the works)
        s.y = ty;
        s.vy = 0;

        for (int step = 0; step < steps; step++) {
            // X / Z lateral spring
            // acceleration = k*(target - pos) - d*vel  (Hooke's law + damper)
            double ax = SPRING_STIFFNESS * (tx - s.x) - SPRING_DAMPING * s.vx;
            s.vx += ax * dt;
            s.x += s.vx * dt;

            double az = SPRING_STIFFNESS * (tz - s.z) - SPRING_DAMPING * s.vz;
            s.vz += az * dt;
            s.z  += s.vz * dt;

            // Yaw angular spring
            // wrapDegrees ensures we always take the shortest arc (avoids 359°→1° spinning)
            float dyaw = Mth.wrapDegrees(tyaw - s.yaw);
            float ayaw = SPRING_STIFFNESS * dyaw - SPRING_DAMPING * s.vyaw;
            s.vyaw += ayaw * dt;
            s.yaw = Mth.wrapDegrees(s.yaw + s.vyaw * dt);

            // Pitch angular spring
            float dpitch = tpitch - s.pitch;
            float apitch = SPRING_STIFFNESS * dpitch - SPRING_DAMPING * s.vpitch;
            s.vpitch += apitch * dt;
            s.pitch += s.vpitch * dt;

            // Jump squash/stretch spring
            // Target is driven continuously by vertical velocity instead of a
            // simple on/off "airborne" flag, so the phases blend into each
            // other with no explicit state machine needed:
            //
            //   takeoff (vy > 0, just left ground) → target goes negative
            //       = halo squashes toward the head, from below
            //   ascending (vy still > 0, shrinking) → target stays negative
            //       = squash holds while rising, easing out near the apex
            //   apex → falling (vy crosses to < 0) → target flips positive
            //       = halo stretches away from the head, from above
            //   descending (vy < 0, growing) → target stays positive
            //       = stretch holds (and grows) while falling
            //   landing (isOnGround again) → target snaps to 0; the
            //       spring is still sitting at a stretched position with
            //       downward velocity, so it overshoots past 0 into a squash,
            //       then bounces back — for free, from the underdamped spring.
            float ajump = SPRING_STIFFNESS * (targetJump - s.jumpOffset) - SPRING_DAMPING * s.vjumpOffset;
            s.vjumpOffset += ajump * dt;
            s.jumpOffset += s.vjumpOffset * dt;
        }

        return s;
    }

    public static boolean isEmpty() {
        return STATES.isEmpty();
    }

    public static void removeIf(Predicate<Integer> shouldRemove) {
        STATES.keySet().removeIf(shouldRemove);
    }
}