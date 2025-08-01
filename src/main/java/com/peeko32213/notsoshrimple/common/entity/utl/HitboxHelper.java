package com.peeko32213.notsoshrimple.common.entity.utl;

import com.peeko32213.notsoshrimple.core.registry.NSSParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class HitboxHelper {

    private static final double d = 0.6f;
    private static final double angleVar = Math.PI / 12;

    public static void LargeAttack(DamageSource source, float damage, float knockback, PathfinderMob entityIn, Vec3 pos0, double radius, double angleFirst, double angleLast, double hInf, double hSup) {
        Vec2 knockVec = MathHelpers.OrizontalAimVector(
                MathHelpers.AimVector(new Vec3(-entityIn.position().x, -entityIn.position().y, -entityIn.position().z),
                        new Vec3(-entityIn.getTarget().position().x, -entityIn.getTarget().position().y, -entityIn.getTarget().position().z)
                ));

        Vec2 aim = MathHelpers.OrizontalAimVector(entityIn.getLookAngle());
        Level worldIn = entityIn.level();

        for (int i = 0; i <= radius / d; ++i) {
            for (int j = 0; j <= (angleLast - angleFirst) / angleVar; ++j) {
                double angle = angleFirst + angleVar * j;
                double x = pos0.x + i * d * (aim.x * Math.cos(angle) - aim.y * Math.sin(angle));
                double z = pos0.z + i * d * (aim.y * Math.cos(angle) + aim.x * Math.sin(angle));
                for (int k = 0; k <= (hSup - hInf) / d; ++k) {
                    double y = pos0.y + hInf + k * d;
                    AABB scanAbove = new AABB(x - d, y - 4 * d, z - d, x + d, y + 2 * d, z + d);
                    List<LivingEntity> entities = new ArrayList<>(worldIn.getEntitiesOfClass(LivingEntity.class, scanAbove));
                    if (!entities.isEmpty()) {
                        for (int n = 0; n < entities.size(); n++) {
                            LivingEntity target = entities.get(n);
                            if (target != entityIn) {
                                target.hurt(source, damage);
                                target.setLastHurtByMob(entityIn);
                                target.knockback(knockback, knockVec.x, knockVec.y);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void LargeAttackWithTargetCheck(DamageSource source, float damage, float knockback, PathfinderMob entityIn, Vec3 pos0, double radius, double angleFirst, double angleLast, double hInf, double hSup) {
        Vec2 knockVec = MathHelpers.OrizontalAimVector(
                MathHelpers.AimVector(new Vec3(-entityIn.position().x, -entityIn.position().y, -entityIn.position().z),
                        new Vec3(-entityIn.getTarget().position().x, -entityIn.getTarget().position().y, -entityIn.getTarget().position().z)
                ));

        Vec2 aim = MathHelpers.OrizontalAimVector(entityIn.getLookAngle());
        Level worldIn = entityIn.level();

        for (int i = 0; i <= radius / d; ++i) {
            for (int j = 0; j <= (angleLast - angleFirst) / angleVar; ++j) {
                double angle = angleFirst + angleVar * j;
                double x = pos0.x + i * d * (aim.x * Math.cos(angle) - aim.y * Math.sin(angle));
                double z = pos0.z + i * d * (aim.y * Math.cos(angle) + aim.x * Math.sin(angle));
                for (int k = 0; k <= (hSup - hInf) / d; ++k) {
                    double y = pos0.y + hInf + k * d;
                    AABB scanAbove = new AABB(x - d, y - 4 * d, z - d, x + d, y + 2 * d, z + d);
                    List<LivingEntity> entities = new ArrayList<>(worldIn.getEntitiesOfClass(LivingEntity.class, scanAbove));
                    if (!entities.isEmpty()) {
                        for (int n = 0; n < entities.size(); n++) {
                            LivingEntity target = entities.get(n);
                            if (target == entityIn.getTarget()) {
                                target.hurt(source, damage);
                                target.setLastHurtByMob(entityIn);
                                target.knockback(knockback, knockVec.x, knockVec.y);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void LongAttackWithTargetCheck(DamageSource source, float damage, float knockback, PathfinderMob entityIn, Vec3 pos0, double radius, double edgeS, double edgeR, double hInf, double hSup) {
        Vec2 knockVec = MathHelpers.OrizontalAimVector(
                MathHelpers.AimVector(new Vec3(-entityIn.position().x, -entityIn.position().y, -entityIn.position().z),
                        new Vec3(-entityIn.getTarget().position().x, -entityIn.getTarget().position().y, -entityIn.getTarget().position().z)
                ));

        Vec2 aim = MathHelpers.OrizontalAimVector(entityIn.getLookAngle());
        Level worldIn = entityIn.level();

        for (int i = 0; i <= radius / d; ++i) {
            for (int j = Math.round(Math.round(edgeS / d)); j <= edgeR / d; ++j) {
                double angle = edgeR * Math.PI * (Math.pow(2, -2)) / 4 + angleVar * j;
                double x = pos0.x + aim.x * (d * i + d * j);
                double z = pos0.z + aim.y * (d * i + d * j);
                for (int k = 0; k <= (hSup - hInf) / d; ++k) {
                    double y = pos0.y + hInf + k * d;
                    AABB scanAbove = new AABB(x - d, y - 4 * d, z - d, x + d, y + 2 * d, z + d);
                    List<LivingEntity> entities = new ArrayList<>(worldIn.getEntitiesOfClass(LivingEntity.class, scanAbove));
                    if (!entities.isEmpty()) {
                        for (int n = 0; n < entities.size(); n++) {
                            LivingEntity target = entities.get(n);
                            if (target == entityIn.getTarget()) {
                                target.hurt(source, damage);
                                target.setLastHurtByMob(entityIn);
                                target.knockback(knockback, knockVec.x, knockVec.y);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void hitboxOutline(AABB box, ServerLevel world) {
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, box.maxX, box.maxY, box.maxZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.END_ROD, box.maxX, box.minY, box.minZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(NSSParticles.FOAM_STANDARD.get(), box.maxX, box.minY, box.maxZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.HAPPY_VILLAGER, box.maxX, box.maxY, box.minZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.HAPPY_VILLAGER, box.minX, box.maxY, box.maxZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(NSSParticles.FOAM_STANDARD.get(), box.minX, box.minY, box.minZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.END_ROD, box.minX, box.minY, box.maxZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, box.minX, box.maxY, box.minZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }
}