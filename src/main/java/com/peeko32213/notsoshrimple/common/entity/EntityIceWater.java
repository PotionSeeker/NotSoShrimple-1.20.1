package com.peeko32213.notsoshrimple.common.entity;

import com.peeko32213.notsoshrimple.core.registry.NSSParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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

public class EntityIceWater extends AbstractHurtingProjectile implements GeoEntity {
    private Monster owner;
    public int lifeTime = 0;
    private static final ParticleOptions particle = NSSParticles.FOAM_STANDARD.get();
    public float damage = 10.0f;
    public double pissspeed = 7.5;
    public int maxLifeTime = (int) (1500 / pissspeed);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Float> DATA_PISS_STARTPOSX = SynchedEntityData.defineId(EntityIceWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_STARTPOSY = SynchedEntityData.defineId(EntityIceWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_STARTPOSZ = SynchedEntityData.defineId(EntityIceWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_DELTAPOSX = SynchedEntityData.defineId(EntityIceWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_DELTAPOSY = SynchedEntityData.defineId(EntityIceWater.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PISS_DELTAPOSZ = SynchedEntityData.defineId(EntityIceWater.class, EntityDataSerializers.FLOAT);
    public Vec3 startPos;
    public Vec3 deltaPos;
    public Vec3 normalDeltaPos;
    public double boxRadius = 2.25;
    public Vec3 scanBox = new Vec3(50, 50, 50);

    public EntityIceWater(EntityType<? extends AbstractHurtingProjectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        this.entityData.define(DATA_PISS_STARTPOSX, 0F);
        this.entityData.define(DATA_PISS_STARTPOSY, 0F);
        this.entityData.define(DATA_PISS_STARTPOSZ, 0F);
        this.entityData.define(DATA_PISS_DELTAPOSX, 0F);
        this.entityData.define(DATA_PISS_DELTAPOSY, 0F);
        this.entityData.define(DATA_PISS_DELTAPOSZ, 0F);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.ENTITY_EFFECT;
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
        this.deltaPos = finalTargetPos.subtract(this.position());
        this.normalDeltaPos = deltaPos.normalize();
        this.entityData.set(DATA_PISS_STARTPOSX, (float) startPos.x);
        this.entityData.set(DATA_PISS_STARTPOSY, (float) startPos.y);
        this.entityData.set(DATA_PISS_STARTPOSZ, (float) startPos.z);
        this.entityData.set(DATA_PISS_DELTAPOSX, (float) deltaPos.x);
        this.entityData.set(DATA_PISS_DELTAPOSY, (float) deltaPos.y);
        this.entityData.set(DATA_PISS_DELTAPOSZ, (float) deltaPos.z);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        int timer = this.lifeTime;

        if (this.level().isClientSide() && this.lifeTime <= 1) {
            this.startPos = new Vec3(this.entityData.get(DATA_PISS_STARTPOSX), this.entityData.get(DATA_PISS_STARTPOSY), this.entityData.get(DATA_PISS_STARTPOSZ));
            this.deltaPos = new Vec3(this.entityData.get(DATA_PISS_DELTAPOSX), this.entityData.get(DATA_PISS_DELTAPOSY), this.entityData.get(DATA_PISS_DELTAPOSZ));
            this.normalDeltaPos = deltaPos.normalize();
        }

        this.setInvisible(true);
        if (this.lifeTime >= maxLifeTime) {
            this.remove(RemovalReason.DISCARDED);
        }

        if (this.level().isClientSide() && startPos != null) {
            this.normalDeltaPos = deltaPos.normalize();
            Vec3 scaledPos = startPos.add(normalDeltaPos.scale((double) timer * pissspeed));
            this.level().addParticle(ParticleTypes.CLOUD, scaledPos.x, scaledPos.y, scaledPos.z, 0.0D, 0.0D, 0.0D);
            for (int p = 0; p < 6 * (1 + Math.sqrt(0.001 * timer)); ++p) {
                double d0 = this.random.nextGaussian() * 0.125D;
                double d1 = this.random.nextGaussian() * 0.125D;
                double d2 = this.random.nextGaussian() * 0.125D;
                double length = this.random.nextDouble();
                this.level().addParticle(ParticleTypes.SNOWFLAKE, (scaledPos.x + (d0 * Math.sqrt(timer))) + (deltaPos.x * length), (scaledPos.y + (d1 * Math.sqrt(timer))) + (deltaPos.y * length), (scaledPos.z + (d2 * Math.sqrt(timer))) + (deltaPos.z * length), 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.SNOWFLAKE, (scaledPos.x + d0) + (deltaPos.x * length), (scaledPos.y + d1) + (deltaPos.y * length), (scaledPos.z + d2) + (deltaPos.z * length), 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.SNOWFLAKE, (scaledPos.x + d1) + (deltaPos.x * length), (scaledPos.y + d2) + (deltaPos.y * length), (scaledPos.z + d0) + (deltaPos.z * length), 0.0D, 0.0D, 0.0D);
            }
        }

        if (this.getOwner() != null) {
            Vec3 scaledPos = startPos.add(normalDeltaPos.scale((double) timer * pissspeed));
            ServerLevel world = (ServerLevel) owner.level();
            BlockPos center = BlockPos.containing(scaledPos);
            Block currentBlock = world.getBlockState(center).getBlock();

            AABB checkZone = new AABB(center).inflate(boxRadius + pissspeed);
            AABB hitboxbox = new AABB(center).inflate(boxRadius);

            List<LivingEntity> potentialVictims = world.getEntitiesOfClass(LivingEntity.class, checkZone);

            for (LivingEntity victim : potentialVictims) {
                if (victim != owner) {
                    AABB targetbox = getAABB(victim.getX(), victim.getY(), victim.getZ(), victim);
                    if (targetbox.intersects(hitboxbox)) {
                        victim.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_PROJECTILE), this, (LivingEntity) owner), damage);
                        victim.setTicksFrozen(400);
                        double dA = 0.2D * (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        double dB = 1.0D * (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        victim.push(normalDeltaPos.x() * dB, normalDeltaPos.y() * dA, normalDeltaPos.z() * dB);
                    }
                }
            }

            BlockState state = world.getBlockState(center);
            if (currentBlock.getCollisionShape(state, world, center, CollisionContext.empty()).isEmpty() && !(currentBlock instanceof LeavesBlock)) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public static AABB getAABB(double pX, double pY, double pZ, LivingEntity entity) {
        float f = entity.getBbWidth() / 2.0F;
        return new AABB(pX - f, pY, pZ - f, pX + f, pY + entity.getBbHeight(), pZ + f);
    }

    public void hitboxOutline(Vec3 pos, double rX, double rY, double rZ, ServerLevel world) {
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x + rX, pos.y + rY, pos.z + rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x + rX, pos.y - rY, pos.z + rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(NSSParticles.FOAM_STANDARD.get(), pos.x + rX, pos.y + rY, pos.z - rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x + rX, pos.y - rY, pos.z - rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x - rX, pos.y + rY, pos.z + rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(NSSParticles.FOAM_STANDARD.get(), pos.x - rX, pos.y - rY, pos.z + rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x - rX, pos.y + rY, pos.z - rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x - rX, pos.y - rY, pos.z - rZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
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