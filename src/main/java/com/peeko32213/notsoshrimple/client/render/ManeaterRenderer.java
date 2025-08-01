package com.peeko32213.notsoshrimple.client.render;

import com.peeko32213.notsoshrimple.client.model.ManeaterModel;
import com.peeko32213.notsoshrimple.common.entity.EntityManeaterShell;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ManeaterRenderer extends GeoEntityRenderer<EntityManeaterShell> {
    public ManeaterRenderer(EntityRendererProvider.Context context) {
        super(context, new ManeaterModel());
        this.shadowRadius = 0.5F;
    }
}