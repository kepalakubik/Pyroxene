package id.kepalakubik.minecraftbluearchivehalo;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import id.kepalakubik.minecraftbluearchivehalo.utils.HaloItemStackFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import java.util.*;
import java.util.stream.Collectors;

public class BlueArchiveHalosPyroxeneCommon {
    public static final MerchantOffers haloTradeRecipes = new MerchantOffers();

    public static final Map<String, Integer> recipes = Arrays.stream(BlueArchiveHalosPyroxeneEntry.values())
        .collect(Collectors.toMap(
            BlueArchiveHalosPyroxeneEntry::getName,
            BlueArchiveHalosPyroxeneEntry::getCmd,
            (a, _) -> a,
            LinkedHashMap::new
        ));

    /** Assign the items into the MerchantOffers */
    protected static void assignCustomOffers(HaloItem item) {
        if (haloTradeRecipes.isEmpty()) recipes.forEach((name, cmd) -> {
            ItemStack result = HaloItemStackFactory.Create(item, name, cmd);
            MerchantOffer offer = new MerchantOffer(
                new ItemCost(Items.NETHERITE_HELMET),
                Optional.empty(),
                result,
                Integer.MAX_VALUE,
                0, 0.0F
            );

            haloTradeRecipes.add(offer);
        });
    }

    /** Add the custom trades into the villager */
    protected static void addCustomTrades(Villager villager) {
        Set<Integer> customModelDataSet = haloTradeRecipes.stream()
            .map(recipe -> {
                ItemStack resultStack = recipe.getResult();
                CustomModelData data = resultStack.get(DataComponents.CUSTOM_MODEL_DATA);
                return (data != null)
                    ? data.floats().getFirst().intValue()
                    : 0;
            })
            .collect(Collectors.toSet());

        MerchantOffers merchantRecipes = new MerchantOffers();
        for (MerchantOffer recipe : villager.getOffers()) {
            ItemStack stack = recipe.getResult();
            CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            // We will add the halos later
            if (data != null && customModelDataSet.contains(data.floats().getFirst().intValue())) {
                continue;
            }
            merchantRecipes.add(recipe);
        }

        // Now we can add the halos alongside with the original trade offers
        merchantRecipes.addAll(haloTradeRecipes);
        villager.setOffers(merchantRecipes);
    }
}
