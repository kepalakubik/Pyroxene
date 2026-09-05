package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.trackers.HaloHeadSpringTracker;
import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.trackers.SleepFadeTracker;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloRenderProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class BlueArchiveHalosPyroxeneClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HaloItem.renderProviderHolder.setValue(new HaloRenderProvider());
        if (FabricLoader.getInstance().isModLoaded("forgeconfigapiport")) {
            ConfigCompat.registerClient();
        }
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.isPaused()) return;

        HaloHeadSpringTracker.removeIf(id -> {
            Entity entity = minecraft.level.getEntity(id);
            return !(entity instanceof Player player)
                || !(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HaloItem);
        });

        SleepFadeTracker.removeIf(id -> {
            Entity entity = minecraft.level.getEntity(id);
            return !(entity instanceof Player player)
                || !(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HaloItem);
        });
    }
}
