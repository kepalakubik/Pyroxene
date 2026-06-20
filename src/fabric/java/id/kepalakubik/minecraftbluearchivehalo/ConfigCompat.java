package id.kepalakubik.minecraftbluearchivehalo;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import id.kepalakubik.minecraftbluearchivehalo.model.HaloRenderer;
import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ConfigCompat {
    protected static void register() {
        NeoForgeConfigRegistry.INSTANCE.register(Constants.MODID, ModConfig.Type.CLIENT, Config.SPEC);
    }

    protected static void registerClient() {
        ConfigScreenFactoryRegistry.INSTANCE.register(Constants.MODID, ConfigurationScreen::new);
        reloadConfig(); // Assign the config into the main
        NeoForgeModConfigEvents.reloading(Constants.MODID).register(event -> reloadConfig());
    }

    private static void reloadConfig() {
        HaloRenderer.enableHaloSpring = Config.CONFIG.enableHaloSpring.get();
        HaloRenderer.isGlowing = Config.CONFIG.isGlowing.get();
        HaloItem.disableAnimation = Config.CONFIG.disableAnimation.get();
    }
}
