package com.peeko32213.notsoshrimple.client.render;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.client.model.PoisonWaterModel;
import com.peeko32213.notsoshrimple.common.entity.EntityToxicWater;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ToxicWaterRenderer extends GeoEntityRenderer<EntityToxicWater> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(NotSoShrimple.MODID, "textures/entity/poisonpiss.png");

    public ToxicWaterRenderer(EntityRendererProvider.Context context) {
        super(context, new PoisonWaterModel());
    }

    @Override
    public ResourceLocation getTextureLocation(EntityToxicWater entity) {
        return TEXTURE_LOCATION;
    }
}
