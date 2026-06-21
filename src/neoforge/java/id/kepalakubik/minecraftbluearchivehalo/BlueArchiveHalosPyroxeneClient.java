package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.utils.HaloHeadSpringTracker;
import id.kepalakubik.minecraftbluearchivehalo.model.HaloRenderer;
import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloRenderProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Constants.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class BlueArchiveHalosPyroxeneClient {
    public BlueArchiveHalosPyroxeneClient(IEventBus eventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        eventBus.addListener(ModConfigEvent.Loading.class, event -> reloadConfig());
        eventBus.addListener(ModConfigEvent.Reloading.class, event -> reloadConfig());
    }

    @SubscribeEvent
    private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        HaloItem.renderProviderHolder.setValue(new HaloRenderProvider());
    }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Pre event) {
        if (HaloHeadSpringTracker.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;

        HaloHeadSpringTracker.removeIf(id -> {
            Entity entity = minecraft.level.getEntity(id);
            return !(entity instanceof Player player)
                || !(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HaloItem);
        });
    }

    private static void reloadConfig() {
        HaloRenderer.enableHaloSpring = Config.CONFIG.enableHaloSpring.get();
        HaloRenderer.isGlowing = Config.CONFIG.isGlowing.get();
        HaloItem.disableAnimation = Config.CONFIG.disableAnimation.get();
    }
}
