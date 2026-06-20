package id.kepalakubik.minecraftbluearchivehalo.item;

import id.kepalakubik.minecraftbluearchivehalo.Constants;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HaloItem extends ArmorItem implements GeoItem {
    public static final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final Map<String, AnimatableInstanceCache> haloCaches = new HashMap<>();

    public static boolean disableAnimation = Constants.DISABLE_ANIMATION;
    // TODO: Find another way to know the active halo
    public static String activeHalo = "";

    public HaloItem(Properties properties) {
        super(
            ArmorMaterials.NETHERITE,
            Type.HELMET,
            properties.
                fireResistant()
                .stacksTo(1)
                .durability(407)
        );
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(renderProviderHolder.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController[] {
            new AnimationController<>(this, 0, state -> {
                if (disableAnimation) {
                    return PlayState.STOP;
                }

                state.setAnimation(DefaultAnimations.IDLE);
                return PlayState.CONTINUE;
            })
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        if (!activeHalo.isEmpty()) {
            return haloCaches.computeIfAbsent(activeHalo, k -> GeckoLibUtil.createInstanceCache(this));
        }

        // Fallback
        return cache;
    }
}