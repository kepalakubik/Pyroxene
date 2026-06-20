package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloItemStackFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.assignCustomOffers;
import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.recipes;
import static id.kepalakubik.minecraftbluearchivehalo.HaloCreativeTab.*;

public class BlueArchiveHalosPyroxene implements ModInitializer {
    protected static final Item HALO_ITEMS = registerItem(HaloItem::new);

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, HALO_TAB_KEY, HALO_TAB);

        CreativeModeTabEvents.modifyOutputEvent(HALO_TAB_KEY).register(this::addHalosToCreative);
        UseEntityCallback.EVENT.register(this::addCustomTrades);

        if (FabricLoader.getInstance().isModLoaded("forgeconfigapiport")) {
            ConfigCompat.register();
        }
    }

    /** Add the halos into the armorer trade offers */
    protected InteractionResult addCustomTrades(
        Player player, Level level, InteractionHand hand,
        Entity entity, @Nullable EntityHitResult entityHitResult
    ) {
        // This is server-side mod, bruh
        if (level.isClientSide()) return InteractionResult.PASS;

        if (!(entity instanceof Villager villager)) return InteractionResult.PASS;

        // Only armorer villager can trade the halos with you
        boolean isArmorerVillager = villager.getVillagerData().profession().is(VillagerProfession.ARMORER);
        if (!isArmorerVillager) return InteractionResult.PASS;

        assignCustomOffers((HaloItem) HALO_ITEMS);
        BlueArchiveHalosPyroxeneCommon.addCustomTrades(villager);
        return InteractionResult.PASS;
    }

    /** Add the halos into the creative combat tab */
    private void addHalosToCreative(FabricCreativeModeTabOutput haloTab) {
        recipes.forEach((name, cmd) -> {
            ItemStack result = HaloItemStackFactory.Create((HaloItem) HALO_ITEMS, name, cmd);
            haloTab.accept(result);
        });
    }

    private static <T extends Item> T registerItem(Function<Properties, T> itemFactory)  {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MODID, "halos"));
        T item = itemFactory.apply(new Properties().setId(itemKey));

        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}
