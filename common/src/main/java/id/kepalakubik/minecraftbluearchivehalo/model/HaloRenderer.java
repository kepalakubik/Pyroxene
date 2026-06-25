package id.kepalakubik.minecraftbluearchivehalo.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import id.kepalakubik.minecraftbluearchivehalo.Constants;
import id.kepalakubik.minecraftbluearchivehalo.ScreenCompat;
import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloHeadSpringTracker;
import id.kepalakubik.minecraftbluearchivehalo.utils.SleepFadeTracker;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static id.kepalakubik.minecraftbluearchivehalo.Constants.LOGGER;

public class HaloRenderer <R extends HumanoidRenderState & GeoRenderState> extends GeoArmorRenderer<HaloItem, @NonNull R> {
    public static boolean enableHaloSpring = Constants.ENABLE_HALO_SPRING;
    public static boolean isGlowing = Constants.IS_GLOWING;

    // Store the sleep fade tracker per entity via id
    private final Map<Integer, SleepFadeTracker> fadeTrackers = new HashMap<>();

    private static final DataTicket<float[]> HALO_OFFSET = DataTicket.create("halo_bone_offset", float[].class);
    private static final DataTicket<float[]> HALO_ROT_OFFSET = DataTicket.create("halo_bone_rot_offset", float[].class);
    private static final DataTicket<Float> SLEEP_ALPHA = DataTicket.create("halo_sleep_alpha", Float.class);
    private static final DataTicket<Boolean> IS_SLEEPING = DataTicket.create("halo_is_sleeping", Boolean.class);
    private static final boolean shouldUseCompat = !SharedConstants.getCurrentVersion().name().startsWith("26.1");

    static {
        if (shouldUseCompat) {
            LOGGER.info(
                "Using compatibility layer for Minecraft {}",
                SharedConstants.getCurrentVersion().name()
            );
        }
    }

    public HaloRenderer(String name) {
        super(new HaloModel(name));

        if (!shouldUseCompat) {
            withRenderLayer(new HaloGlowingLayer<>(this));
        }
    }

    @Override
    public void addRenderData(@NonNull HaloItem animatable, GeoArmorRenderer.@Nullable RenderData relatedObject, @NonNull R renderState, float partialTick) {
        if (relatedObject == null) return;

        if (enableHaloSpring) applySmoothedOffset(partialTick, relatedObject, renderState);

        LivingEntity entity = relatedObject.entity();
        boolean isSleeping = entity.isSleeping();
        int sleepTimer = (entity instanceof Player player) ? player.getSleepTimer() : 0;

        SleepFadeTracker fadeTracker = fadeTrackers.computeIfAbsent(entity.getId(), _ -> new SleepFadeTracker());
        long currentTick = (Minecraft.getInstance().level != null)
            ? Minecraft.getInstance().level.getGameTime()
            : 0L;
        float alpha = fadeTracker.computeAlpha(isSleeping, sleepTimer, currentTick);

        renderState.addGeckolibData(SLEEP_ALPHA, alpha);
        renderState.addGeckolibData(IS_SLEEPING, isSleeping);
    }

    @Override
    public int getRenderColor(@NonNull HaloItem animatable, @Nullable RenderData stackAndSlot, float partialTick) {
        if (stackAndSlot == null) {
            return ARGB.color(255, 255, 255, 255);
        }

        LivingEntity entity = stackAndSlot.entity();
        SleepFadeTracker fadeTracker = fadeTrackers.get(entity.getId());

        if (fadeTracker != null) {
            int a = Math.round(fadeTracker.getLastAlpha() * 255f);
            return ARGB.color(a, 255, 255, 255);
        }

        return super.getRenderColor(animatable, stackAndSlot, partialTick);
    }

    @Override
    public @Nullable RenderType getRenderType(@NonNull R renderState, @NonNull Identifier texture) {
        Float alpha = renderState.getGeckolibData(SLEEP_ALPHA);
        if (alpha != null && alpha < 1.0f) {
            // Use translucent when sleeping so alpha can be modified
            return RenderTypes.entityTranslucent(texture);
        }

        // Use default if not sleeping
        return super.getRenderType(renderState, texture);
    }

    @Override
    public void adjustModelBonesForRender(@NonNull RenderPassInfo<@NonNull R> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        Boolean isSleeping = renderPassInfo.getGeckolibData(IS_SLEEPING);
        if (isSleeping != null) {
            float rotDelta = isSleeping ? (float) Math.toRadians(-45.0) : 0f;
            snapshots.get("armorHead").ifPresent(snapshot -> snapshot.setRotX(snapshot.getRotX() + rotDelta));
        }

        float[] offset = renderPassInfo.renderState().getGeckolibData(HALO_OFFSET);
        if (offset != null) {
            snapshots.get("armorHead").ifPresent(snapshot -> {
                snapshot.setTranslateX(snapshot.getTranslateX() + offset[0]);
                snapshot.setTranslateY(snapshot.getTranslateY() + offset[1]);
                snapshot.setTranslateZ(snapshot.getTranslateZ() + offset[2]);
            });
        }

        float[] rotOffset = renderPassInfo.renderState().getGeckolibData(HALO_ROT_OFFSET);
        if (rotOffset != null) {
            snapshots.get("armorHead").ifPresent(snapshot -> {
                snapshot.setRotX(snapshot.getRotX() + rotOffset[0]);
                snapshot.setRotY(snapshot.getRotY() + rotOffset[1]);
            });
        }
    }

    @Override
    public @NonNull List<ArmorSegment> getSegmentsForSlot(@NonNull R renderState, @NonNull EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
            ? java.util.List.of(ArmorSegment.HEAD)
            : java.util.List.of();
    }

    // HACK: I don't know why RenderLayer ain't working in 26.2, so i'll just use this hack instead.
    @Override
    public void performRenderPass(@NonNull R renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector renderTasks, @NonNull CameraRenderState cameraState, @Nullable List<RenderPassInfo.BoneUpdater<@NonNull R>> boneUpdaters) {
        if (shouldUseCompat && isGlowing) {
            renderState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }

        super.performRenderPass(renderState, poseStack, renderTasks, cameraState, boneUpdaters);
    }

    private void applySmoothedOffset(float partialTick, RenderData renderData, R renderState) {
        LivingEntity wearer = renderData.entity();

        if (renderData.slot() != EquipmentSlot.HEAD) return;
        if (ScreenCompat.getCurrentScreen() != null) return;

        double targetX = Mth.lerp(partialTick, wearer.xo, wearer.getX());
        double targetY = Mth.lerp(partialTick, wearer.yo, wearer.getY());
        double targetZ = Mth.lerp(partialTick, wearer.zo, wearer.getZ());

        float interpHeadYaw = Mth.rotLerp(partialTick, wearer.yHeadRotO, wearer.getYHeadRot());
        float interpPitch = Mth.lerp(partialTick, wearer.xRotO, wearer.getXRot());

        HaloHeadSpringTracker.SmoothState smoothState = HaloHeadSpringTracker.update(
            wearer.getId(),
            targetX, targetY, targetZ,
            interpHeadYaw, interpPitch,
            wearer.onGround(),
            wearer.getDeltaMovement().y
        );

        // Where the halo was vs where the player is now
        double worldDX = smoothState.x - targetX;
        double worldDY = smoothState.y - targetY;
        double worldDZ = smoothState.z - targetZ;

        // Fix the halo flying behind the player when walking in left/right
        float bodyYaw = Mth.rotLerp(partialTick, wearer.yBodyRotO, wearer.yBodyRot);
        float yawRad = -bodyYaw * Mth.DEG_TO_RAD;
        float modelDX = (float) (worldDX * Mth.cos(yawRad) - worldDZ * Mth.sin(yawRad));
        float modelDY = (float) worldDY + smoothState.jumpOffset;
        float modelDZ = (float) (-worldDX * Mth.sin(yawRad) - worldDZ * Mth.cos(yawRad));

        renderState.addGeckolibData(HALO_OFFSET, new float[] {
            modelDX * 16f,
            modelDY * 16f,
            modelDZ * 16
        });

        float dyaw = Mth.wrapDegrees(smoothState.yaw - interpHeadYaw);
        float dpitch = smoothState.pitch - interpPitch;

        renderState.addGeckolibData(HALO_ROT_OFFSET, new float[] {
            dpitch * Mth.DEG_TO_RAD, // rotX: pitch lag
            -dyaw * Mth.DEG_TO_RAD // rotY: yaw lag (negated: MC yaw is CW, GeckoLib is CCW)
        });
    }
}