package com.peeko32213.notsoshrimple.common.entity;

import com.peeko32213.notsoshrimple.common.entity.utl.*;
import com.peeko32213.notsoshrimple.core.config.NotSoShrimpleConfig;
import com.peeko32213.notsoshrimple.core.registry.NSSEntities;
import com.peeko32213.notsoshrimple.core.registry.NSSSounds;
import com.peeko32213.notsoshrimple.core.registry.NSSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class EntityManeaterShell extends Monster implements GeoEntity, SemiAquatic {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(EntityManeaterShell.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COMBAT_STATE = SynchedEntityData.defineId(EntityManeaterShell.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENTITY_STATE = SynchedEntityData.defineId(EntityManeaterShell.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityManeaterShell.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Vec3 oldPos;
    public Vec3 newPos;
    public Vec3 velocity;
    public double directionlessSpeed;

    public EntityManeaterShell(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 0.0F);
        this.setMaxUpStep(1.0f);
        this.oldPos = this.position();
        this.newPos = this.position();
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 2D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 50D);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new EntityManeaterShell.ClamMeleeAttackGoal(this, 1F, true));
        this.goalSelector.addGoal(1, new FindWaterGoal(this));
        this.goalSelector.addGoal(1, new LeaveWaterGoal(this));
        this.goalSelector.addGoal(3, new BottomRoamGoal(this, 1.0D, 10, 50));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false, entity -> entity.getType().is(NSSTags.CRAYFISH_VICTIMS)));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity entity) {
        return super.canAttack(entity);
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void tick() {
        this.oldPos = this.newPos;
        this.newPos = this.position();
        this.velocity = this.newPos.subtract(this.oldPos);
        this.directionlessSpeed = Math.abs(Math.sqrt((velocity.x * velocity.x) + (velocity.z * velocity.z) + (velocity.z * velocity.z)));
        super.tick();
    }

    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.jumping) {
                this.setDeltaMovement(this.getDeltaMovement().scale(1.4D));
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.72D, 0.0D));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.4D));
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.08D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
        return worldIn.getFluidState(pos.below()).isEmpty() && worldIn.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) ? 10.0F : super.getWalkTargetValue(pos, worldIn);
    }

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(ANIMATION_STATE, 0);
        this.entityData.define(COMBAT_STATE, 0);
        this.entityData.define(ENTITY_STATE, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setVariant(compound.getInt("Variant"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", getVariant());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.horizontalCollision && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this) && this.isAggressive()) {
            boolean flag = false;
            AABB axisalignedbb = this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D);
            for (BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(axisalignedbb.minX), Mth.floor(axisalignedbb.minY), Mth.floor(axisalignedbb.minZ), Mth.floor(axisalignedbb.maxX), Mth.floor(axisalignedbb.maxY), Mth.floor(axisalignedbb.maxZ))) {
                BlockState blockstate = this.level().getBlockState(blockpos);
                if (blockstate.is(NSSTags.CRAYFISH_BREAKABLES)) {
                    flag = this.level().destroyBlock(blockpos, true, this) || flag;
                }
            }
        }
    }

    public int getMaxHeadYRot() {
        return 45;
    }

    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.hasCustomName();
    }

    public boolean removeWhenFarAway(double d) {
        return !this.hasCustomName();
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public void setAnimationState(int anim) {
        this.entityData.set(ANIMATION_STATE, anim);
    }

    public int getCombatState() {
        return this.entityData.get(COMBAT_STATE);
    }

    public void setCombatState(int anim) {
        this.entityData.set(COMBAT_STATE, anim);
    }

    public int getEntityState() {
        return this.entityData.get(ENTITY_STATE);
    }

    public void setEntityState(int anim) {
        this.entityData.set(ENTITY_STATE, anim);
    }

    @Override
    public boolean shouldEnterWater() {
        return true;
    }

    @Override
    public boolean shouldLeaveWater() {
        return false;
    }

    @Override
    public boolean shouldStopMoving() {
        return false;
    }

    @Override
    public int getWaterSearchRange() {
        return 5;
    }

    static class ClamMeleeAttackGoal extends Goal {
        protected final EntityManeaterShell mob;
        private final int meleeRange = 350;
        private double speedModifier;
        private double baseModifier;
        private double chargeModifier = 2;
        private final boolean followingTargetEvenIfNotSeen;
        private Path path;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private long lastCanUseCheck;
        private int failedPathFindingPenalty = 0;
        private boolean canPenalize = false;
        private int animTime = 0;
        private double chargeSpeedScale = 0.01;
        private int chargeCD;
        private Vec3 chargeMotion = new Vec3(0, 0, 0);
        Vec3 biteOffSet = new Vec3(0, 1, 2);
        Vec3 strikeOffSet = new Vec3(0, 1.5, 3);
        Vec3 chargeOffSet = new Vec3(0, 1, 2);

        public ClamMeleeAttackGoal(EntityManeaterShell theMob, double speedmod, boolean persistentMemory) {
            this.mob = theMob;
            this.speedModifier = speedmod;
            this.baseModifier = speedModifier;
            this.followingTargetEvenIfNotSeen = persistentMemory;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            long i = this.mob.level().getGameTime();
            if (i - this.lastCanUseCheck < 20L) {
                return false;
            }
            this.lastCanUseCheck = i;
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                return false;
            }
            if (canPenalize) {
                if (--this.ticksUntilNextPathRecalculation <= 0) {
                    this.path = this.mob.getNavigation().createPath(livingentity, 0);
                    this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                    return this.path != null;
                }
                return true;
            }
            this.path = this.mob.getNavigation().createPath(livingentity, 0);
            if (this.path != null) {
                return true;
            }
            return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null || this.mob.distanceToSqr(livingentity) >= 70 || !livingentity.isAlive()) {
                return false;
            }
            if (!this.followingTargetEvenIfNotSeen) {
                return !this.mob.getNavigation().isDone();
            }
            if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
                return false;
            }
            return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity).isCreative();
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.path, this.speedModifier);
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
            this.animTime = 0;
            this.chargeCD = 0;
            this.mob.setAnimationState(0);
        }

        public void stop() {
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.mob.setTarget(null);
            }
            this.mob.setAnimationState(0);
        }

        public void tick() {
            LivingEntity target = this.mob.getTarget();
            double distance = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            double reach = this.getAttackReachSqr(target);
            int animState = this.mob.getAnimationState();

            switch (animState) {
                case 21 -> tickStrikeAttack();
                case 22 -> tickBiteAttack();
                case 23 -> tickCharge();
                default -> {
                    this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                    this.ticksUntilNextAttack = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                    this.chargeCD = Math.max(this.chargeCD - 1, 0);
                    this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    this.doMovement(target, distance);
                    this.checkForCloseRangeAttack(distance);
                }
            }
        }

        protected void doMovement(LivingEntity livingentity, Double d0) {
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 &&
                    (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D ||
                            livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D ||
                            this.mob.getRandom().nextFloat() < 0.05F)) {
                this.pathedTargetX = livingentity.getX();
                this.pathedTargetY = livingentity.getY();
                this.pathedTargetZ = livingentity.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                if (this.canPenalize) {
                    this.ticksUntilNextPathRecalculation += failedPathFindingPenalty;
                    if (this.mob.getNavigation().getPath() != null) {
                        Node finalPathPoint = this.mob.getNavigation().getPath().getEndNode();
                        if (finalPathPoint != null && livingentity.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1) {
                            failedPathFindingPenalty = 0;
                        } else {
                            failedPathFindingPenalty += 10;
                        }
                    } else {
                        failedPathFindingPenalty += 10;
                    }
                }
                if (d0 > 1024.0D) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (d0 > 256.0D) {
                    this.ticksUntilNextPathRecalculation += 5;
                }
                if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }
            }
        }

        protected void checkForCloseRangeAttack(double distance) {
            if (distance <= meleeRange && this.ticksUntilNextAttack <= 0) {
                int r = (this.mob.getRandom().nextInt(100) + 1);
                if (distance <= 40) {
                    if (r <= 60) {
                        this.mob.setAnimationState(22); // Bite
                    } else if (60 < r && r <= 80) {
                        this.mob.setAnimationState(21); // Strike
                    } else {
                        this.mob.setAnimationState(23); // Charge
                    }
                } else {
                    if (r <= 40) {
                        this.mob.setAnimationState(21); // Strike
                    } else if (40 < r && r <= 70) {
                        this.mob.setAnimationState(22); // Bite
                    } else {
                        this.mob.setAnimationState(23); // Charge
                    }
                }
            } else if (this.ticksUntilNextAttack <= 0 && this.chargeCD <= 0 && this.mob.onGround()) {
                this.mob.setAnimationState(23);
            }
        }

        protected boolean getRangeCheck() {
            return this.mob.distanceToSqr(this.mob.getTarget().getX(), this.mob.getTarget().getY(), this.mob.getTarget().getZ())
                    <= 1.8F * this.getAttackReachSqr(this.mob.getTarget());
        }

        protected void tickBiteAttack() {
            animTime++;
            LivingEntity target = this.mob.getTarget();
            this.mob.lookAt(target, 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            if (animTime == 14) {
                preformBiteAttack();
            }
            if (animTime >= 26) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown();
                this.ticksUntilNextPathRecalculation = 0;
            }
        }

        protected void tickStrikeAttack() {
            animTime++;
            LivingEntity target = this.mob.getTarget();
            this.mob.lookAt(target, 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            if (animTime == 9) {
                preformStrikeAttack();
            }
            if (animTime >= 10) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown();
                this.ticksUntilNextPathRecalculation = 0;
            }
        }

        protected void tickCharge() {
            animTime++;
            this.mob.getNavigation().stop();
            this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            if (animTime == 1) {
                this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
                this.mob.yBodyRot = this.mob.yHeadRot;
                Entity target = this.mob.getTarget();
                Vec3 targetPos = (target.position());
                double x = -((this.mob.position().x - targetPos.x));
                double z = -((this.mob.position().z - targetPos.z));
                this.chargeMotion = new Vec3(x, this.mob.getDeltaMovement().y, z).normalize();
            }
            if (animTime >= 8 && animTime < 20) {
                this.mob.setDeltaMovement(chargeMotion.x / 2, this.mob.getDeltaMovement().y, chargeMotion.z / 2);
                PisslikeHitboxes.PivotedPolyHitCheck(this.mob, this.chargeOffSet, 1.2, 3, 1.2, (ServerLevel) this.mob.level(), 14,
                        new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 4, false);
            }
            if (animTime >= 24) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown();
                this.ticksUntilNextPathRecalculation = 0;
                this.chargeCD = this.mob.getRandom().nextInt(200);
            }
        }

        protected void preformBiteAttack() {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0));
            this.mob.playSound(NSSSounds.CRAYFISH_ATTACK.get(), 0.5F, 0.5F);
            PisslikeHitboxes.PivotedPolyHitCheck(this.mob, this.biteOffSet, 2, 1, 2, (ServerLevel) this.mob.level(), 12,
                    new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 1, false);
        }

        protected void preformStrikeAttack() {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0));
            this.mob.playSound(NSSSounds.CRAYFISH_ATTACK.get(), 0.5F, 0.5F);
            PisslikeHitboxes.PivotedPolyHitCheck(this.mob, this.strikeOffSet, 1.2, 2, 1.2, (ServerLevel) this.mob.level(), 8,
                    new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 1.5, false);
        }

        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = 0;
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected int getTicksUntilNextAttack() {
            return this.ticksUntilNextAttack;
        }

        protected int getAttackInterval() {
            return 5;
        }

        protected double getAttackReachSqr(LivingEntity entity) {
            return (double) (this.mob.getBbWidth() * 2.5F * this.mob.getBbWidth() * 1.8F + entity.getBbWidth());
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.getDirectEntity() instanceof AbstractArrow) {
            AbstractArrow hitter = (AbstractArrow) source.getDirectEntity();
            if (this.getAnimationState() == 22) {
                return false;
            }
            if (hitter.position().y >= this.position().y() + 2) {
                return true;
            }
        }
        return source.is(DamageTypes.FALL) ||
                source.is(DamageTypes.IN_WALL) ||
                source.is(DamageTypes.FALLING_BLOCK) ||
                source.is(DamageTypes.CACTUS) ||
                source.is(DamageTypes.FALLING_ANVIL) ||
                super.isInvulnerableTo(source);
    }

    protected SoundEvent getAmbientSound() {
        return NSSSounds.CRAYFISH_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return NSSSounds.CRAYFISH_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return NSSSounds.CRAYFISH_DEATH.get();
    }

    protected void playStepSound(BlockPos p_28301_, BlockState p_28302_) {
        this.playSound(NSSSounds.CRAYFISH_SCUTTLE.get(), 0.3F, 1.0F);
    }

    private PlayState predicate(AnimationState<EntityManeaterShell> state) {
        AnimationController<?> controller = state.getController();
        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);

        if (speed > 0.03) { // Adjusted threshold for water movement
            controller.setAnimationSpeed(1.0 + (this.directionlessSpeed / 0.20));
            controller.setAnimation(RawAnimation.begin().thenLoop("animation.maneater_shell.walk"));
        } else {
            controller.setAnimationSpeed(1.0F);
            controller.setAnimation(RawAnimation.begin().thenLoop("animation.maneater_shell.idle"));
        }
        return PlayState.CONTINUE;
    }

    private PlayState atkState(AnimationState<EntityManeaterShell> state) {
        AnimationController<?> controller = state.getController();
        int animState = this.getAnimationState();
        switch (animState) {
            case 21:
                controller.setAnimation(RawAnimation.begin().thenPlay("animation.maneater_shell.strike"));
                controller.setAnimationSpeed(1.0F);
                break;
            case 22:
                controller.setAnimation(RawAnimation.begin().thenPlay("animation.maneater_shell.bite"));
                controller.setAnimationSpeed(1.0F);
                break;
            case 23:
                controller.setAnimation(RawAnimation.begin().thenPlay("animation.maneater_shell.ram"));
                controller.setAnimationSpeed(1.0F);
                break;
            default:
                controller.setAnimation(RawAnimation.begin().thenPlay("animation.maneater_shell.empty"));
                controller.setAnimationSpeed(1.0F);
                break;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 1, this::predicate));
        controllers.add(new AnimationController<>(this, "atkcontroller", 1, this::atkState));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    private void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        int rarityRoll = (this.getRandom().nextInt(100) + 1);
        int i;
        if (rarityRoll > 90) {
            i = 9;
        } else if (rarityRoll > 80) {
            i = 8;
        } else if (rarityRoll > 70) {
            i = 7;
        } else if (rarityRoll > 60) {
            i = 6;
        } else if (rarityRoll > 50) {
            i = 5;
        } else if (rarityRoll > 40) {
            i = 4;
        } else if (rarityRoll > 30) {
            i = 3;
        } else if (rarityRoll > 20) {
            i = 2;
        } else if (rarityRoll > 10) {
            i = 1;
        } else {
            i = 0;
        }
        if (reason == MobSpawnType.STRUCTURE) {
            this.restrictTo(this.blockPosition(), 15);
        }
        this.setVariant(i);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    public static boolean canSpawn(EntityType<EntityManeaterShell> type, ServerLevelAccessor levelAccessor, MobSpawnType p_32352_, BlockPos pos, RandomSource random) {
        Boolean biomeRight = levelAccessor.getBiome(pos).is(NSSTags.MANEATERSPAWNS);
        if (!levelAccessor.getFluidState(pos.below()).is(net.minecraft.tags.FluidTags.WATER)) {
            return false;
        }
        return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && levelAccessor.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) && biomeRight;
    }

    public boolean checkSpawnRules(LevelAccessor worldIn, MobSpawnType spawnReasonIn) {
        return NSSEntities.rollSpawn(NotSoShrimpleConfig.maneaterRolls, this.getRandom(), spawnReasonIn);
    }

    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }
}