package id.kepalakubik.minecraftbluearchivehalo.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import id.kepalakubik.minecraftbluearchivehalo.Constants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class HaloItem extends Item implements GeoItem {
    public static final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static boolean disableAnimation = Constants.DISABLE_ANIMATION;

    public HaloItem(Properties properties) {
        super(properties
            .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.HELMET)
            .fireResistant()
            .stacksTo(1)
            .durability(407)
        );
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(renderProviderHolder.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("haloAnimation", state -> {
            if (disableAnimation) {
                return PlayState.STOP;
            }

            return state.setAndContinue(DefaultAnimations.IDLE);
        }).setOverrideEasingType(EasingType.EASE_IN_OUT_SINE));
    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
