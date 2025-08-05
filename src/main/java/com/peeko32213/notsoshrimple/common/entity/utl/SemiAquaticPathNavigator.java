package com.peeko32213.notsoshrimple.common.entity.utl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SemiAquaticPathNavigator extends WaterBoundPathNavigation {

    public SemiAquaticPathNavigator(Mob entitylivingIn, Level worldIn) {
        super(entitylivingIn, worldIn);
        this.setCanFloat(true); // Enable floating in water
    }

    protected PathFinder createPathFinder(int p_179679_1_) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(true);
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        return new PathFinder(this.nodeEvaluator, p_179679_1_);
    }

    protected boolean canUpdatePath() {
        return true;
    }

    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5D), this.mob.getZ());
    }

    protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ) {
        Vec3 vector3d = new Vec3(posVec32.x, posVec32.y + (double)this.mob.getBbHeight() * 0.5D, posVec32.z);
        return this.level.clip(new ClipContext(posVec31, vector3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, this.mob)).getType() == HitResult.Type.MISS;
    }

    public boolean isStableDestination(BlockPos pos) {
        return !this.level.getBlockState(pos.below()).isAir();
    }

    @Override
    protected void followThePath() {
        super.followThePath();
        // Apply friction in water to reduce slipperiness
        if (this.mob.isInWater()) {
            Vec3 motion = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(motion.scale(0.9D)); // Additional friction
        }
    }

    public void setCanFloat(boolean canSwim) {
        super.setCanFloat(canSwim);
    }
}