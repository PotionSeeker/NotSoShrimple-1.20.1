package com.peeko32213.notsoshrimple.client;

import com.peeko32213.notsoshrimple.client.render.FallingBlockRenderer;
import com.peeko32213.notsoshrimple.common.entity.utl.FallingBlockEntity;
import com.peeko32213.notsoshrimple.common.entity.utl.ScreenShakeEntity;
import com.peeko32213.notsoshrimple.core.registry.NSSEntities;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "notsoshrimple", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register a no-op renderer for ScreenShakeEntity to prevent rendering crashes
        event.registerEntityRenderer(NSSEntities.SCREEN_SHAKE.get(), context -> new EntityRenderer<ScreenShakeEntity>(context) {
            @Override
            public ResourceLocation getTextureLocation(ScreenShakeEntity entity) {
                return null; // No texture needed as the entity is not rendered
            }

            @Override
            public boolean shouldRender(ScreenShakeEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
                return false; // Prevent rendering of the entity
            }
        });

        // Register a custom renderer for FallingBlockEntity
        event.registerEntityRenderer(NSSEntities.FALLING_BLOCK.get(), FallingBlockRenderer::new);
    }
}