package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloItemStackFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import static id.kepalakubik.minecraftbluearchivehalo.BlueArchiveHalosPyroxeneCommon.*;
import static id.kepalakubik.minecraftbluearchivehalo.HaloCreativeTab.*;

public class BlueArchiveHalosPyroxene implements ModInitializer {
    protected static final Item HALO_ITEMS = registerItem(new HaloItem(new Properties()));

    @Override
    public void onInitialize() {
        assignCustomOffers((HaloItem) HALO_ITEMS);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, HALO_TAB_KEY, HALO_TAB);

        ItemGroupEvents.modifyEntriesEvent(HALO_TAB_KEY).register(this::addHalosToCreative);
        UseEntityCallback.EVENT.register(this::addCustomTrades);

        if (FabricLoader.getInstance().isModLoaded("forgeconfigapiport")) {
            ConfigCompat.register();
        }
    }

    private static Item registerItem(Item item) {
        ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "halos");
        return Registry.register(BuiltInRegistries.ITEM, itemID, item);
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
        if (villager.getVillagerData().getProfession() != VillagerProfession.ARMORER) return InteractionResult.PASS;

        BlueArchiveHalosPyroxeneCommon.addCustomTrades(villager);
        return InteractionResult.PASS;
    }

    /** Add the halos into the creative combat tab */
    private void addHalosToCreative(FabricItemGroupEntries itemGroup) {
        recipes.forEach((name, cmd) -> {
            ItemStack result = HaloItemStackFactory.Create((HaloItem) HALO_ITEMS, name, cmd);
            itemGroup.accept(result);
        });
    }
}
