package id.kepalakubik.minecraftbluearchivehalo.model;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HaloModel extends GeoModel<HaloItem> {
    private final String name;

    public HaloModel(String name) {
        this.name = name;
    }

    @Override
    public ResourceLocation getModelResource(HaloItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
            "minecraftbluearchivehalo",
            String.format("geo/%s.geo.json", this.name)
        );
    }

    @Override
    public ResourceLocation getTextureResource(HaloItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
            "minecraftbluearchivehalo",
            String.format("textures/%s.geo.texture.png", this.name)
        );
    }

    @Override
    public ResourceLocation getAnimationResource(HaloItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
            "minecraftbluearchivehalo",
            String.format("animations/%s.animation.json", this.name)
        );
    }
}
