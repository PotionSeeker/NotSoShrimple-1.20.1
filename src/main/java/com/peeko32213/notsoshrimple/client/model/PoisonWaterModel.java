package com.peeko32213.notsoshrimple.client.model;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.entity.EntityToxicWater;
import net.minecraft.resources.ResourceLocation;
//import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class PoisonWaterModel extends GeoModel<EntityToxicWater> {

    @Override
    public ResourceLocation getModelResource(EntityToxicWater object) {
        return new ResourceLocation(NotSoShrimple.MODID, "geo/piss.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityToxicWater object) {
        return new ResourceLocation(NotSoShrimple.MODID, "textures/entity/icepiss.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntityToxicWater animatable) {
        return new ResourceLocation(NotSoShrimple.MODID, "animations/piss.animation.json");
    }
}
