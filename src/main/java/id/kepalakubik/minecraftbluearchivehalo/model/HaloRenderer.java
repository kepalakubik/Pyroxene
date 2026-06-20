package id.kepalakubik.minecraftbluearchivehalo.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import id.kepalakubik.minecraftbluearchivehalo.Constants;
import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloHeadSpringTracker;
import id.kepalakubik.minecraftbluearchivehalo.utils.SleepFadeTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.Color;

import java.util.HashMap;
import java.util.Map;

public class HaloRenderer extends GeoArmorRenderer<HaloItem> {
    private final String name;

    private static final float SLEEP_ROT_DELTA = (float) Math.toRadians(-45.0);
    // Store the sleep fade tracker per entity via id
    private final Map<Integer, SleepFadeTracker> fadeTrackers = new HashMap<>();

    public static boolean enableHaloSpring = Constants.ENABLE_HALO_SPRING;
    public static boolean isGlowing = Constants.IS_GLOWING;

    public HaloRenderer(String name) {
        super(new HaloModel(name));
        this.name = name;
    }

    /** Returns the current entity as a LivingEntity, or null. */
    @Nullable
    private LivingEntity getCurrentWearer() {
        return this.currentEntity instanceof LivingEntity w ? w : null;
    }

    /** Gets (or creates) the SleepFadeTracker for a given wearer. */
    private SleepFadeTracker getFadeTracker(LivingEntity wearer) {
        return fadeTrackers.computeIfAbsent(wearer.getId(), k -> new SleepFadeTracker());
    }

    @Override
    public void preRender(
        PoseStack poseStack, HaloItem animatable, BakedGeoModel model,
        MultiBufferSource bufferSource, VertexConsumer buffer,
        boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour
    ) {
        // Assign current active halo for caching
        HaloItem.activeHalo = this.name;

        super.preRender(
            poseStack, animatable, model, bufferSource, buffer,
            isReRender, partialTick, packedLight, packedOverlay, colour
        );

        if (enableHaloSpring) applySmoothedOffset(partialTick);
        applyFadeSleep();
    }

    @Override
    public void actuallyRender(
        PoseStack poseStack, HaloItem animatable, BakedGeoModel model, @Nullable RenderType renderType,
        MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
        float partialTick, int packedLight, int packedOverlay, int colour
    ) {
        super.actuallyRender(
            poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick,
            isGlowing ? LightTexture.FULL_BRIGHT : packedLight,
            packedOverlay, colour
        );
    }

    @Override
    public void postRender(
        PoseStack poseStack, HaloItem animatable, BakedGeoModel model,
        MultiBufferSource bufferSource, VertexConsumer buffer,
        boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour
    ) {
        super.postRender(
            poseStack, animatable, model, bufferSource, buffer,
            isReRender, partialTick, packedLight, packedOverlay, colour
        );

        // And remove it post render. I don't know why but Claude said "trust me, bro"
        HaloItem.activeHalo = "";
    }

    @Override
    public Color getRenderColor(HaloItem animatable, float partialTick, int packedLight) {
        LivingEntity wearer = getCurrentWearer();
        if (wearer == null) return super.getRenderColor(animatable, partialTick, packedLight);

        int a = Math.round(getFadeTracker(wearer).getLastAlpha() * 255f);
        return Color.ofARGB(a, 255, 255, 255);
    }

    @Override
    public RenderType getRenderType(HaloItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        LivingEntity wearer = getCurrentWearer();

        if (wearer != null) {
            int sleepTimer = wearer instanceof Player player ? player.getSleepTimer() : 0;
            long currentTick = (Minecraft.getInstance().level != null)
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;

            if (getFadeTracker(wearer).computeAlpha(wearer.isSleeping(), sleepTimer, currentTick) < 1.0f) {
                // Use translucent when sleeping so alpha can be modified
                return RenderType.entityTranslucentCull(texture);
            }
        }

        // Use default if not sleeping
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    private void applySmoothedOffset(float partialTick) {
        LivingEntity wearer = getCurrentWearer();
        if (wearer == null || this.currentSlot != EquipmentSlot.HEAD) return;

        getGeoModel().getBone("armorHead").ifPresent(bone -> {
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

            // Rotation
            float dpitch = smoothState.pitch - interpPitch;
            float dyaw = Mth.wrapDegrees(smoothState.yaw - interpHeadYaw);

            bone.setPosX(bone.getPosX() + modelDX * 16f);
            bone.setPosY(bone.getPosY() + modelDY * 16f);
            bone.setPosZ(bone.getPosZ() + modelDZ * 16f);

            bone.setRotX(bone.getRotX() + (dpitch * Mth.DEG_TO_RAD)); // rotX: pitch lag
            bone.setRotY(bone.getRotY() + (-dyaw * Mth.DEG_TO_RAD)); // rotY: yaw lag (negated: MC yaw is CW, GeckoLib is CCW)
        });
    }

    private void applyFadeSleep() {
        LivingEntity wearer = getCurrentWearer();
        if (wearer == null || this.currentSlot != EquipmentSlot.HEAD || !wearer.isSleeping()) return;

        getGeoModel().getBone("armorHead")
            .ifPresent(bone -> bone.setRotX(bone.getRotX() + SLEEP_ROT_DELTA));
    }

    @Override
    public GeoBone getHeadBone(GeoModel<HaloItem> model) {
        return this.model.getBone("armorHead").orElse(null);
    }
}
