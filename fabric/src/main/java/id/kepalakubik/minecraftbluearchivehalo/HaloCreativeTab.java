package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloItemStackFactory;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import java.util.Map;

import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.recipes;

public class HaloCreativeTab {
    public static final ResourceKey<CreativeModeTab> HALO_TAB_KEY = ResourceKey.create(
        BuiltInRegistries.CREATIVE_MODE_TAB.key(),
        Identifier.fromNamespaceAndPath(Constants.MODID, "halos")
    );

    public static final CreativeModeTab HALO_TAB = FabricItemGroup.builder()
        .title(Component.translatable("%s.creative.tab".formatted(Constants.MODID)))
        .icon(() -> {
            Map.Entry<String, Integer> haloItem = recipes.entrySet().iterator().next();
            String name = haloItem.getKey();
            Integer cmd = haloItem.getValue();

            return HaloItemStackFactory.Create((HaloItem) BlueArchiveHalosPyroxene.HALO_ITEMS, name, cmd);
        })
        .build();
}
