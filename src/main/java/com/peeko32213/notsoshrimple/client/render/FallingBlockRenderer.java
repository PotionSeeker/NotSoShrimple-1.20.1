package com.peeko32213.notsoshrimple.client.render;

import com.peeko32213.notsoshrimple.common.entity.utl.FallingBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public FallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(FallingBlockEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState blockstate = entity.getBlockState();
        if (blockstate.getRenderShape() == RenderShape.MODEL) {
            Level level = entity.level();
            if (blockstate != level.getBlockState(entity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                poseStack.pushPose();
                BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                poseStack.translate(-0.5D, 0.0D, -0.5D);
                var model = this.dispatcher.getBlockModel(blockstate);
                for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(blockstate.getSeed(entity.getStartPos())), net.minecraftforge.client.model.data.ModelData.EMPTY)) {
                    this.dispatcher.getModelRenderer().tesselateBlock(
                            level,
                            model,
                            blockstate,
                            blockpos,
                            poseStack,
                            buffer.getBuffer(renderType),
                            false,
                            RandomSource.create(),
                            blockstate.getSeed(entity.getStartPos()),
                            OverlayTexture.NO_OVERLAY,
                            net.minecraftforge.client.model.data.ModelData.EMPTY,
                            renderType
                    );
                }
                poseStack.popPose();
                super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}