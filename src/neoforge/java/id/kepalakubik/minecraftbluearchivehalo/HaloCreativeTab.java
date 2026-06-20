package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.utils.HaloItemStackFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map.Entry;

import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.recipes;

public class HaloCreativeTab {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HALO_TAB = CREATIVE_MODE_TAB.register("halos", () -> CreativeModeTab.builder()
        .title(Component.translatable("%s.creative.tab".formatted(Constants.MODID)))
        .icon(() -> {
            Entry<String, Integer> haloItem = recipes.entrySet().iterator().next();
            String name = haloItem.getKey();
            Integer cmd = haloItem.getValue();

            return HaloItemStackFactory.Create(BlueArchiveHalosPyroxene.HALO_ITEMS.get(), name, cmd);
        })
        .build()
    );

    protected static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
