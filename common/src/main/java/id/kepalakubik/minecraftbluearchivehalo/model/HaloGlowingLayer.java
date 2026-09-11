package id.kepalakubik.minecraftbluearchivehalo.model;

import net.minecraft.client.renderer.LightTexture;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static id.kepalakubik.minecraftbluearchivehalo.model.HaloRenderer.isGlowing;

public class HaloGlowingLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends AutoGlowingGeoLayer<T, O, R> {
    public HaloGlowingLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    protected @NonNull Identifier getTextureResource(@NonNull R renderState) {
        return renderer.getTextureLocation(renderState);
    }

    @Override
    protected int getBrightness(@NonNull R renderState) {
        return LightTexture.FULL_SKY;
    }

    @Override
    protected boolean shouldAddZOffset(@NonNull R renderState) {
        return true;
    }

    @Override
    public void submitRenderTask(@NonNull RenderPassInfo<@NonNull R> renderPassInfo, @NonNull SubmitNodeCollector renderTasks) {
        if (!isGlowing) return;
        super.submitRenderTask(renderPassInfo, renderTasks);
    }
}
