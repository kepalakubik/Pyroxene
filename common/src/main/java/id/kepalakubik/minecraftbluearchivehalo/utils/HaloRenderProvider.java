package id.kepalakubik.minecraftbluearchivehalo.utils;

import id.kepalakubik.minecraftbluearchivehalo.model.HaloRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.HashMap;
import java.util.Map;

public class HaloRenderProvider implements GeoRenderProvider {
    private final Map<String, HaloRenderer> rendererDic = new HashMap<>();

    @Override
    public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
        T livingEntity,
        ItemStack itemStack,
        EquipmentSlot equipmentSlot,
        HumanoidModel<T> original
    ) {
        String name = "";

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains("CharacterName")) {
                name = nbt.getString("CharacterName");
            }
        }

        if (name.isEmpty()) return original;

        return rendererDic.computeIfAbsent(name, HaloRenderer::new);
    }
}
