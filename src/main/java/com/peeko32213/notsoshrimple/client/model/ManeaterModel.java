package com.peeko32213.notsoshrimple.client.model;


import com.google.common.collect.Maps;
import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.entity.EntityManeaterShell;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
//import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

public class ManeaterModel extends GeoModel<EntityManeaterShell>{

    public static final Map<Integer, ResourceLocation> TEXTURES = Util.make(Maps.newHashMap(), (hashMap) -> {
        hashMap.put(0, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell.png"));
        hashMap.put(1, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_both_1.png"));
        hashMap.put(2, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_both_2.png"));
        hashMap.put(3, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_both_3.png"));
        hashMap.put(5, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_drowned_1.png"));
        hashMap.put(6, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_drowned_2.png"));
        hashMap.put(7, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_drowned_3.png"));
        hashMap.put(8, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_zombie_1.png"));
        hashMap.put(9, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_zombie_2.png"));
        hashMap.put(10, new ResourceLocation(NotSoShrimple.MODID, "textures/entity/maneater_shell_zombie_3.png"));
    });

    @Override
    public ResourceLocation getModelResource(EntityManeaterShell object) {
        return new ResourceLocation(NotSoShrimple.MODID, "geo/maneater_shell.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityManeaterShell object) {
        return TEXTURES.getOrDefault(object.getVariant(), TEXTURES.get(0));
    }

    @Override
    public ResourceLocation getAnimationResource(EntityManeaterShell animatable) {
        return new ResourceLocation(NotSoShrimple.MODID, "animations/maneater_shell.animation.json");
    }
}

