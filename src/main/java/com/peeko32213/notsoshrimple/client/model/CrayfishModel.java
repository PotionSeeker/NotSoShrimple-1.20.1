package com.peeko32213.notsoshrimple.client.model;


import com.google.common.collect.Maps;
import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.entity.EntityCrayfish;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
//import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

public class CrayfishModel extends GeoModel<EntityCrayfish> {
    public static final Map<Integer, ResourceLocation> TEXTURES = Util.make(Maps.newHashMap(), (hashMap) -> {
        hashMap.put(0, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_brown.png"));
        hashMap.put(1, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_shit.png"));
        hashMap.put(2, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_green.png"));
        hashMap.put(3, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_damascus.png"));
        hashMap.put(4, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_golden.png"));
        //brown selection^
        //damascus is uncommon, golden is rare
        hashMap.put(5, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_snow.png"));
        hashMap.put(6, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_frosted.png"));
        hashMap.put(7, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_greenice.png"));
        hashMap.put(8, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_crystal.png"));
        hashMap.put(9, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_blue.png"));
        //frost selection^
        //crystal is uncommon, blue is rare
        hashMap.put(10, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_blood.png"));
        hashMap.put(11, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_rosy.png"));
        hashMap.put(12, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_withered.png"));
        hashMap.put(13, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_peppermint.png"));
        hashMap.put(14, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/crayfish_halloween.png"));
        //blood selection^
        //peppermint is uncommon, halloween is rare
    });

    @Override
    public ResourceLocation getModelResource(EntityCrayfish object) {
        return new ResourceLocation(NotSoShrimple.MODID, "geo/crayfish.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityCrayfish object) {
        return TEXTURES.getOrDefault(object.getVariant(), TEXTURES.get(0));
        //object is the entity
    }

    @Override
    public ResourceLocation getAnimationResource(EntityCrayfish animatable) {
        return new ResourceLocation(NotSoShrimple.MODID, "animations/crayfish.animation.json");
    }
}

