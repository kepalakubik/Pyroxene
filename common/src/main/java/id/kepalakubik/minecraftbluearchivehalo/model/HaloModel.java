package id.kepalakubik.minecraftbluearchivehalo.model;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class HaloModel extends GeoModel<HaloItem> {
    private final String name;

    public HaloModel(String name) {
        this.name = name;
    }

    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("minecraftbluearchivehalo", this.name);
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(
            "minecraftbluearchivehalo",
            String.format("geckolib/textures/%s.geo.texture.png", this.name)
        );
    }

    @Override
    public @NonNull Identifier getAnimationResource(@NonNull HaloItem animatable) {
        return Identifier.fromNamespaceAndPath("minecraftbluearchivehalo", this.name);
    }
}