package com.peeko32213.notsoshrimple.client.model;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.entity.ArchivedProjectileWater;
import com.peeko32213.notsoshrimple.common.entity.EntityIceWater;
import net.minecraft.resources.ResourceLocation;
//import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class IceWaterModel extends GeoModel<EntityIceWater> {

    @Override
    public ResourceLocation getTextureResource(EntityIceWater object) {
        return new ResourceLocation(NotSoShrimple.MODID, "textures/entity/poisonpiss.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntityIceWater animatable) {
        return new ResourceLocation(NotSoShrimple.MODID, "animations/piss.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(EntityIceWater object) {
        return new ResourceLocation(NotSoShrimple.MODID, "geo/piss.geo.json");
    }
}
