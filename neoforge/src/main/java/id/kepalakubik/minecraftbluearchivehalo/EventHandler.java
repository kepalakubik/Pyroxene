package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.utils.HaloItemStackFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxene.HALO_ITEMS;
import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.assignCustomOffers;
import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.recipes;

@EventBusSubscriber(modid = Constants.MODID)
public class EventHandler {
    // Add the halos into creative tab
    @SubscribeEvent
    private static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == HaloCreativeTab.HALO_TAB.getKey()) {
            recipes.forEach((name, cmd) -> {
                ItemStack result = HaloItemStackFactory.Create(HALO_ITEMS.get(), name, cmd);
                event.accept(result);
            });
        }
    }

    // Add the halos into armorer villager trade offers
    @SubscribeEvent
    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // This is server-side mod, bruh
        if (event.getLevel().isClientSide()) return;

        Entity entity = event.getTarget();
        if (!(entity instanceof Villager villager)) return;

        // Only armorer villager can trade the halos with you
        boolean isArmorerVillager = villager.getVillagerData().profession().is(VillagerProfession.ARMORER);
        if (!isArmorerVillager) return;

        assignCustomOffers(HALO_ITEMS.get());
        BlueArchiveHalosPyroxeneCommon.addCustomTrades(villager);
    }
}
