package id.kepalakubik.minecraftbluearchivehalo.utils;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import id.kepalakubik.minecraftbluearchivehalo.model.HaloRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HaloRenderProvider implements GeoRenderProvider {
    private final Map<String, HaloRenderer<?>> rendererDic = new HashMap<>();

    @Override
    public @Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, @NonNull EquipmentSlot equipmentSlot) {
        String name = "";

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains("CharacterName")) {
                name = nbt.getString("CharacterName").orElse("");
            }
        }

        if (name.isEmpty()) return null;

        return rendererDic.computeIfAbsent(name, HaloRenderer::new);
    }
}
