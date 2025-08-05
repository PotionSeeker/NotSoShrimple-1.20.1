package com.peeko32213.notsoshrimple.common.entity.utl;

import com.peeko32213.notsoshrimple.common.entity.EntityCrayfish;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class CrayfishMoveControl extends MoveControl {
    private final EntityCrayfish crayfish;
    private final int maxTurn;

    public CrayfishMoveControl(EntityCrayfish crayfish, int maxTurn) {
        super(crayfish);
        this.crayfish = crayfish;
        this.maxTurn = maxTurn;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double dx = this.wantedX - crayfish.getX();
            double dz = this.wantedZ - crayfish.getZ();
            double dy = this.wantedY - crayfish.getY();
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            if (distanceSqr < 2.5000003E-7F) {
                crayfish.setZza(0.0F);
                return;
            }

            // Calculate target yaw
            float targetYaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
            float deltaYaw = Mth.wrapDegrees(targetYaw - crayfish.getYRot());
            float lerpFactor = Math.abs(deltaYaw) > 30F ? 0.2F : 0.4F; // Faster rotation for smaller angles
            crayfish.setYRot(Mth.lerp(lerpFactor, crayfish.getYRot(), crayfish.getYRot() + Mth.clamp(deltaYaw, -maxTurn, maxTurn)));
            crayfish.yBodyRot = crayfish.getYRot();

            // Set speed
            float speed = (float) (this.speedModifier * crayfish.getAttributeValue(Attributes.MOVEMENT_SPEED));
            crayfish.setSpeed(speed);

            // Handle vertical movement
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(dy) > 1.0E-5F || Math.abs(horizontalDistance) > 1.0E-5F) {
                float targetPitch = (float) (-(Mth.atan2(dy, horizontalDistance) * (180F / Math.PI)));
                crayfish.setXRot(Mth.lerp(0.4F, crayfish.getXRot(), Mth.clamp(targetPitch, -45F, 45F)));
                crayfish.setYya(dy > 0.0D ? speed : -speed);
            }

            // Check path validity and attempt to navigate around obstacles
            Path path = crayfish.getNavigation().getPath();
            if (path != null && !path.isDone()) {
                Vec3 nextPos = path.getNextEntityPos(crayfish);
                double nextDx = nextPos.x - crayfish.getX();
                double nextDz = nextPos.z - crayfish.getZ();
                float nextYaw = (float) (Mth.atan2(nextDz, nextDx) * (180F / Math.PI)) - 90.0F;
                float nextDeltaYaw = Mth.wrapDegrees(nextYaw - crayfish.getYRot());
                crayfish.setYRot(Mth.lerp(0.4F, crayfish.getYRot(), crayfish.getYRot() + Mth.clamp(nextDeltaYaw, -maxTurn, maxTurn)));
                if (!this.isWalkable(nextDx, dy, nextDz)) {
                    // Attempt to jump over obstacles
                    if (crayfish.onGround()) {
                        crayfish.getJumpControl().jump();
                    }
                }
            }
        } else if (this.operation == Operation.STRAFE) {
            float speed = (float) (this.speedModifier * crayfish.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float forward = this.strafeForwards;
            float strafe = this.strafeRight;
            float magnitude = Mth.sqrt(forward * forward + strafe * strafe);
            if (magnitude < 1.0F) {
                magnitude = 1.0F;
            }

            magnitude = speed / magnitude;
            forward *= magnitude;
            strafe *= magnitude;

            float sinYaw = Mth.sin(crayfish.getYRot() * ((float) Math.PI / 180F));
            float cosYaw = Mth.cos(crayfish.getYRot() * ((float) Math.PI / 180F));
            float moveX = forward * cosYaw - strafe * sinYaw;
            float moveZ = strafe * cosYaw + forward * sinYaw;

            if (!this.isWalkable(moveX, 0, moveZ)) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }

            crayfish.setSpeed(speed);
            crayfish.setZza(this.strafeForwards);
            crayfish.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else {
            crayfish.setSpeed(0.0F);
            crayfish.setZza(0.0F);
            crayfish.setXxa(0.0F);
        }
    }

    public boolean isWalkable(double moveX, double moveY, double moveZ) {
        if (crayfish.getNavigation() == null) {
            return true;
        }
        BlockPos targetPos = crayfish.blockPosition().offset((int) moveX, (int) moveY, (int) moveZ);
        return crayfish.getNavigation().isStableDestination(targetPos) &&
                crayfish.level().getBlockState(targetPos.below()).canOcclude() &&
                crayfish.level().noCollision(crayfish, crayfish.getBoundingBox().move(moveX, moveY, moveZ));
    }
}
//