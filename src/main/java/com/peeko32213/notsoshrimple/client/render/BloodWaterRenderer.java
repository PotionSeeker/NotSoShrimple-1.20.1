package com.peeko32213.notsoshrimple.client.render;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.client.model.BloodWaterModel;
import com.peeko32213.notsoshrimple.common.entity.EntityBloodWater;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BloodWaterRenderer extends GeoEntityRenderer<EntityBloodWater> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(NotSoShrimple.MODID, "textures/entity/bloodpiss.png");

    public BloodWaterRenderer(EntityRendererProvider.Context context) {
        super(context, new BloodWaterModel());
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBloodWater entity) {
        return TEXTURE_LOCATION;
    }
}