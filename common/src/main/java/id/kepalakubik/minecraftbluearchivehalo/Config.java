package id.kepalakubik.minecraftbluearchivehalo;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final Config CONFIG;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue isGlowing;
    public final ModConfigSpec.BooleanValue disableAnimation;
    public final ModConfigSpec.BooleanValue enableHaloSpring;

    static {
        final Pair<Config, ModConfigSpec> specPair = BUILDER.configure(Config::new);
        CONFIG = specPair.getLeft();
        SPEC = specPair.getRight();
    }

    private Config(ModConfigSpec.Builder builder) {
        isGlowing = builder
            .translation(String.format("%s.config.is_glowing", Constants.MODID))
            .comment("Make the halo have glowing/emissive effect")
            .define("is_glowing", Constants.IS_GLOWING);

        disableAnimation = builder
            .translation(String.format("%s.config.disable_animation", Constants.MODID))
            .comment("Disable the halo animation. Can improve performance slightly")
            .define("disable_animation", Constants.DISABLE_ANIMATION);

        enableHaloSpring = builder
            .translation(String.format("%s.config.enable_halo_spring", Constants.MODID))
            .comment("Activates the spring between the head and halo, giving a natural effect")
            .define("enable_halo_spring", Constants.ENABLE_HALO_SPRING);
    }
}
