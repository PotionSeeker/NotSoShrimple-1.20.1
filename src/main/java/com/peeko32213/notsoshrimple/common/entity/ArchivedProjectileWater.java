package com.peeko32213.notsoshrimple.common.entity;

import com.peeko32213.notsoshrimple.core.registry.NSSParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ArchivedProjectileWater extends AbstractHurtingProjectile implements GeoEntity {
    private LivingEntity owner;
    private int lifeTime;
    private static final ParticleOptions particle = NSSParticles.FOAM_STANDARD.get();
    public float damage = 30.0f;
    public int maxLifeTime = 4000;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ArchivedProjectileWater(EntityType<? extends AbstractHurtingProjectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    protected float getInertia() {
        return 0.95F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return NSSParticles.FOAM_STANDARD.get();
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        System.out.println("hit" + this.position());
        Shulker marker = EntityType.SHULKER.create(this.level());
        if (marker != null) {
            marker.moveTo(this.position());
        }

        HitResult.Type hitresult$type = pResult.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) pResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, pResult.getLocation(), GameEvent.Context.of(this, (BlockState) null));
        } else if (hitresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult) pResult;
            BlockPos blockpos = blockhitresult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
            this.discard();
        }
    }

    private void addParticlesAroundSelf() {
        Vec3 vec3 = this.getDeltaMovement();
        for (int i = 0; i < 20; ++i) {
            double d0 = this.random.nextGaussian() * 0.2D;
            double d1 = this.random.nextGaussian() * 0.2D;
            double d2 = this.random.nextGaussian() * 0.2D;
            double length = this.random.nextGaussian();
            this.level().addParticle(NSSParticles.FOAM_STANDARD.get(), (this.getX() + d0) + (vec3.x * length),
                    (this.getY() + d1) + (vec3.y * length), (this.getZ() + d2) + (vec3.z * length),
                    this.getDeltaMovement().x * 0.02, this.getDeltaMovement().y * 0.02,
                    this.getDeltaMovement().z * 0.02);
        }
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vector3d = this.getDeltaMovement();
        double d0 = this.getX() + vector3d.x;
        double d1 = this.getY() + vector3d.y;
        double d2 = this.getZ() + vector3d.z;

        this.updateRotation();
        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.remove(RemovalReason.DISCARDED);
        } else {
            this.setDeltaMovement(vector3d.scale(1));
            this.setPos(d0, d1, d2);
        }
        this.addParticlesAroundSelf();
        if (vector3d.x == 0 && vector3d.y == 0 && vector3d.z == 0) {
            System.out.println("hit" + this.position());
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitData) {
        super.onHitEntity(hitData);
        Entity entity1 = this.getOwner();
        Entity entity = hitData.getEntity();
        if (entity instanceof LivingEntity) {
            entity.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_PROJECTILE), this, (LivingEntity) entity1), damage);
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
        }
        if (entity1 instanceof LivingEntity) {
            ((LivingEntity) entity1).setLastHurtMob(entity);
        }
    }

    private double horizontalMag(Vec3 vector3d) {
        return vector3d.x * vector3d.x + vector3d.z * vector3d.z;
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