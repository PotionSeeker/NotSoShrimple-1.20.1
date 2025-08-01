package com.peeko32213.notsoshrimple.client.model;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.entity.EntityBloodWater;
import com.peeko32213.notsoshrimple.common.entity.EntityToxicWater;
import net.minecraft.resources.ResourceLocation;
//import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class BloodWaterModel extends GeoModel<EntityBloodWater> {

    @Override
    public ResourceLocation getModelResource(EntityBloodWater object) {
        return new ResourceLocation(NotSoShrimple.MODID, "geo/piss.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityBloodWater object) {
        return new ResourceLocation(NotSoShrimple.MODID, "textures/entity/icepiss.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntityBloodWater animatable) {
        return new ResourceLocation(NotSoShrimple.MODID, "animations/piss.animation.json");
    }
}
