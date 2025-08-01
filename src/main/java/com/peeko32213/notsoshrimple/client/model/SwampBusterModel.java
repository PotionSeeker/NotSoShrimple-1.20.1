package com.peeko32213.notsoshrimple.client.model;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.item.ItemClawblade;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SwampBusterModel extends GeoModel<ItemClawblade> {
    @Override
    public ResourceLocation getModelResource(ItemClawblade object) {
        return new ResourceLocation(NotSoShrimple.MODID, "geo/swampbuster.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ItemClawblade object) {
        return new ResourceLocation(NotSoShrimple.MODID, "textures/item/swampbusterblade.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ItemClawblade animatable) {
        return new ResourceLocation(NotSoShrimple.MODID, "animations/swampbuster.animation.json");
    }
}