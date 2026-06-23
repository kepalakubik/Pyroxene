package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MODID)
public class BlueArchiveHalosPyroxene {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MODID);
    protected static final DeferredHolder<Item, HaloItem> HALO_ITEMS = ITEMS.registerItem("halos", HaloItem::new);

    public BlueArchiveHalosPyroxene(IEventBus modBus, ModContainer modContainer) {
        ITEMS.register(modBus);
        HaloCreativeTab.register(modBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
