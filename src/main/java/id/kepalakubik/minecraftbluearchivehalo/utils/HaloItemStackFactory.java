package id.kepalakubik.minecraftbluearchivehalo.utils;

import id.kepalakubik.minecraftbluearchivehalo.item.HaloItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class HaloItemStackFactory {
    public static ItemStack Create(HaloItem item, String name, int customModelData) {
        ItemStack haloItemStack = new ItemStack(item);

        haloItemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
            List.of((float) customModelData),
            List.of(),
            List.of(),
            List.of()
        ));

        haloItemStack.set(DataComponents.CUSTOM_NAME,
            Component.translatable("minecraftbluearchivehalo.%s.name".formatted(name))
                .withStyle(style -> style.withColor(0xFFFFFF).withBold(false).withItalic(false))
        );

        Component loreLine = Component.translatable("minecraftbluearchivehalo.%s.lora".formatted(name))
            .withStyle(style -> style.withColor(0xAA00AA).withBold(false).withItalic(false));
        haloItemStack.set(DataComponents.LORE, new ItemLore(List.of(loreLine)));

        CompoundTag customTag = new CompoundTag();
        customTag.putString("CharacterName", name);
        haloItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customTag));

        return haloItemStack;
    }
}