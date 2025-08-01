package com.peeko32213.notsoshrimple.client.render;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.client.model.IceWaterModel;
import com.peeko32213.notsoshrimple.common.entity.EntityIceWater;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IceWaterRenderer extends GeoEntityRenderer<EntityIceWater> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(NotSoShrimple.MODID, "textures/entity/icepiss.png");

    public IceWaterRenderer(EntityRendererProvider.Context context) {
        super(context, new IceWaterModel());
    }

    @Override
    public ResourceLocation getTextureLocation(EntityIceWater entity) {
        return TEXTURE_LOCATION;
    }
}