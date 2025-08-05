package com.peeko32213.notsoshrimple.common.entity;

import com.peeko32213.notsoshrimple.core.registry.NSSParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class EntityToxicWater extends AbstractHurtingProjectile implements GeoEntity {
    private Monster owner;
    public int lifeTime = 0;
    private static final ParticleOptions particle = NSSParticles.FOAM_STANDARD.get();
    public float damage = 10.0f;
    public double pissspeed = 7.5;
    public int maxLifeTime = 60; // Increased to 3 seconds at 20 ticks/second
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Float> DATA_PISS_STARTPOSX = SynchedEntityData.defineId(EntityToxicWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_STARTPOSY = SynchedEntityData.defineId(EntityToxicWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_STARTPOSZ = SynchedEntityData.defineId(EntityToxicWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_ENDPOSX = SynchedEntityData.defineId(EntityToxicWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_ENDPOSY = SynchedEntityData.defineId(EntityToxicWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_ENDPOSZ = SynchedEntityData.defineId(EntityToxicWater.class, EntityDataSerializers.FLOAT);
    public Vec3 startPos;
    public Vec3 endPos;
    public Vec3 normalDeltaPos;
    public double boxRadius = 2.25;

    public EntityToxicWater(EntityType<? extends AbstractHurtingProjectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        this.entityData.define(DATA_PISS_STARTPOSX, 0F);
        this.entityData.define(DATA_PISS_STARTPOSY, 0F);
        this.entityData.define(DATA_PISS_STARTPOSZ, 0F);
        this.entityData.define(DATA_PISS_ENDPOSX, 0F);
        this.entityData.define(DATA_PISS_ENDPOSY, 0F);
        this.entityData.define(DATA_PISS_ENDPOSZ, 0F);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.noPhysics = true; // Allow passing through entities
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return NSSParticles.FOAM_STANDARD.get();
    }

    @Override
    public Monster getOwner() {
        return owner;
    }

    public void setOwner(Monster owner) {
        this.owner = owner;
    }

    public void setTargetPos(Vec3 finalTargetPos) {
        this.startPos = this.position();
        this.endPos = finalTargetPos;
        this.normalDeltaPos = finalTargetPos.subtract(this.startPos).normalize();
        this.entityData.set(DATA_PISS_STARTPOSX, (float) startPos.x);
        this.entityData.set(DATA_PISS_STARTPOSY, (float) startPos.y);
        this.entityData.set(DATA_PISS_STARTPOSZ, (float) startPos.z);
        this.entityData.set(DATA_PISS_ENDPOSX, (float) finalTargetPos.x);
        this.entityData.set(DATA_PISS_ENDPOSY, (float) finalTargetPos.y);
        this.entityData.set(DATA_PISS_ENDPOSZ, (float) finalTargetPos.z);
        // Set rotation to face target
        double dx = finalTargetPos.x - this.startPos.x;
        double dz = finalTargetPos.z - this.startPos.z;
        double dy = finalTargetPos.y - this.startPos.y;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horizontalDist) * (180F / Math.PI)));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        int timer = this.lifeTime;

        if (this.level().isClientSide()) {
            if (this.lifeTime <= 1) {
                this.startPos = new Vec3(this.entityData.get(DATA_PISS_STARTPOSX), this.entityData.get(DATA_PISS_STARTPOSY), this.entityData.get(DATA_PISS_STARTPOSZ));
                this.endPos = new Vec3(this.entityData.get(DATA_PISS_ENDPOSX), this.entityData.get(DATA_PISS_ENDPOSY), this.entityData.get(DATA_PISS_ENDPOSZ));
                this.normalDeltaPos = endPos.subtract(startPos).normalize();
                // Recompute rotation on first tick to ensure accuracy
                double dx = endPos.x - startPos.x;
                double dz = endPos.z - startPos.z;
                double dy = endPos.y - startPos.y;
                double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
                float pitch = (float) (-(Mth.atan2(dy, horizontalDist) * (180F / Math.PI)));
                this.setYRot(yaw);
                this.setXRot(pitch);
                this.yRotO = yaw;
                this.xRotO = pitch;
            }
            if (startPos != null && endPos != null) {
                double distance = startPos.distanceTo(endPos);
                // Spawn particles along the entire path for a laser-like effect
                for (double d = 0; d <= distance; d += 0.2) { // Even denser particle spawning
                    Vec3 pos = startPos.add(normalDeltaPos.scale(d));
                    this.level().addParticle(NSSParticles.FOAM_STANDARD.get(), pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
                    for (int p = 0; p < 2; ++p) { // Further reduced for performance
                        double d0 = this.random.nextGaussian() * 0.03D; // Tighter spread
                        double d1 = this.random.nextGaussian() * 0.03D;
                        double d2 = this.random.nextGaussian() * 0.03D;
                        this.level().addParticle(NSSParticles.FOAM_STANDARD.get(),
                                pos.x + d0, pos.y + d1, pos.z + d2,
                                0.0D, 0.0D, 0.0D);
                    }
                }
                // Keep entity at startPos for rendering
                this.setPos(startPos);
            }
        } else {
            if (this.getOwner() != null && startPos != null && endPos != null) {
                Vec3 scaledPos = startPos.add(normalDeltaPos.scale((double) timer * pissspeed));
                ServerLevel world = (ServerLevel) this.level();
                BlockPos center = BlockPos.containing(scaledPos);
                BlockState state = world.getBlockState(center);
                Block currentBlock = state.getBlock();

                // Check for block collision
                if (!state.getCollisionShape(world, center, CollisionContext.empty()).isEmpty() && !(currentBlock instanceof LeavesBlock)) {
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }

                // Check for entity hits along the path
                double distance = startPos.distanceTo(endPos);
                for (double d = 0; d <= distance; d += 0.5) {
                    Vec3 checkPos = startPos.add(normalDeltaPos.scale(d));
                    AABB hitbox = new AABB(checkPos.x - boxRadius, checkPos.y - boxRadius, checkPos.z - boxRadius,
                            checkPos.x + boxRadius, checkPos.y + boxRadius, checkPos.z + boxRadius);
                    List<LivingEntity> potentialVictims = world.getEntitiesOfClass(LivingEntity.class, hitbox, entity -> entity != this.owner);
                    for (LivingEntity victim : potentialVictims) {
                        AABB targetBox = getAABB(victim.getX(), victim.getY(), victim.getZ(), victim);
                        if (targetBox.intersects(hitbox)) {
                            victim.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_PROJECTILE), this, this.owner), damage);
                            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                            double knockback = 0.2D * (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                            victim.push(normalDeltaPos.x() * knockback, normalDeltaPos.y() * knockback * 0.5, normalDeltaPos.z() * knockback);
                        }
                    }
                }

                // Update position for hit detection
                this.setPos(scaledPos);
            }
            if (this.lifeTime >= maxLifeTime) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public static AABB getAABB(double pX, double pY, double pZ, LivingEntity entity) {
        float f = entity.getBbWidth() / 2.0F;
        return new AABB(pX - f, pY, pZ - f, pX + f, pY + entity.getBbHeight(), pZ + f);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 10000.0D; // Ensure rendering at long distances
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof LivingEntity && entity != this.owner;
    }

    protected static float lerpRotation(float p_234614_0_, float p_234614_1_) {
        while (p_234614_1_ - p_234614_0_ < -180.0F) {
            p_234614_0_ -= 360.0F;
        }
        while (p_234614_1_ - p_234614_0_ >= 180.0F) {
            p_234614_0_ += 360.0F;
        }
        return Mth.lerp(0.2F, p_234614_0_, p_234614_1_);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 1, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.piss.move"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
//