package com.peeko32213.notsoshrimple.common.entity;

import com.peeko32213.notsoshrimple.common.entity.utl.*;
import com.peeko32213.notsoshrimple.core.config.NotSoShrimpleConfig;
import com.peeko32213.notsoshrimple.core.registry.NSSEntities;
import com.peeko32213.notsoshrimple.core.registry.NSSSounds;
import com.peeko32213.notsoshrimple.core.registry.NSSTags;
import com.peeko32213.notsoshrimple.common.entity.utl.FallingBlockEntity;
import com.peeko32213.notsoshrimple.common.entity.utl.ScreenShakeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
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
import java.util.List;

public class EntityCrayfish extends Monster implements GeoEntity {
    private static final RawAnimation RIGHT_CLAW_ANIM = RawAnimation.begin().thenPlayAndHold("animation.crayfish.rightclaw");
    private static final RawAnimation LEFT_CLAW_ANIM = RawAnimation.begin().thenPlayAndHold("animation.crayfish.leftclaw");
    private static final RawAnimation SLAM_ANIM = RawAnimation.begin().thenPlay("animation.crayfish.slam");
    private static final RawAnimation SNIPE_ANIM = RawAnimation.begin().thenPlayAndHold("animation.crayfish.snipe");
    private static final RawAnimation AIR_ANIM = RawAnimation.begin().thenPlay("animation.crayfish.air");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.crayfish.walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.crayfish.idle");
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(EntityCrayfish.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COMBAT_STATE = SynchedEntityData.defineId(EntityCrayfish.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENTITY_STATE = SynchedEntityData.defineId(EntityCrayfish.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityCrayfish.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }
    public double oldSpeedMod;
    public Vec3 oldPos;
    public Vec3 newPos;
    public Vec3 velocity;
    public double directionlessSpeed;
    public int biomeVariant; // 0 = swamp, 1 = ice, 2 = blood
    private boolean isJumping;

    public EntityCrayfish(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(3.0f);
        this.oldPos = this.position();
        this.newPos = this.position();
        this.isJumping = false;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.ARMOR, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 70D);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new EntityCrayfish.CrayfishMeleeAttackGoal(this, 1.5F, true));
        this.goalSelector.addGoal(3, new CustomRandomStrollGoal(this, 10, 1.0D, 600, 34));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false, entity -> entity.getType().is(NSSTags.CRAYFISH_VICTIMS)));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setVariant(compound.getInt("Variant"));
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
    public void tick() {
        this.oldPos = this.newPos;
        this.newPos = this.position();
        this.velocity = this.newPos.subtract(this.oldPos);
        this.directionlessSpeed = Math.abs(Math.sqrt((velocity.x * velocity.x) + (velocity.z * velocity.z) + (velocity.z * velocity.z)));
        if (this.isJumping && this.onGround()) {
            this.isJumping = false;
            int animState = this.getAnimationState();
            if (animState == 25 || animState == 26) {
                this.largeSlamBlockEffects(this.position());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), NSSSounds.CRAYFISH_SMASH.get(), this.getSoundSource(), 0.7F, 0.5F);
                ScreenShakeEntity.ScreenShake(this.level(), this.position(), 20.0f, 0.5f, 30, 7);
                if (animState == 26) {
                    // Apply damage only to entities within the slam effect range
                    double radius = 5.0;
                    AABB hitbox = new AABB(this.position().add(-radius, -radius, -radius), this.position().add(radius, radius, radius));
                    List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, hitbox, entity -> entity != this);
                    for (LivingEntity victim : victims) {
                        victim.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this), 20f);
                    }
                }
            }
            this.setAnimationState(0);
        }
        super.tick();
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.8f;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.08D, 0.0D));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void customServerAiStep() {
        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 1.5D);
        } else {
            this.setSprinting(false);
        }
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.horizontalCollision && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this) && this.isSprinting()) {
            boolean flag = false;
            AABB axisalignedbb = this.getBoundingBox().inflate(0.2D);
            for (BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(axisalignedbb.minX), Mth.floor(axisalignedbb.minY), Mth.floor(axisalignedbb.minZ), Mth.floor(axisalignedbb.maxX), Mth.floor(axisalignedbb.maxY), Mth.floor(axisalignedbb.maxZ))) {
                BlockState blockstate = this.level().getBlockState(blockpos);
                if (blockstate.is(NSSTags.CRAYFISH_BREAKABLES)) {
                    flag = this.level().destroyBlock(blockpos, true, this) || flag;
                }
            }
        }
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

    static class CrayfishMeleeAttackGoal extends Goal {
        protected final EntityCrayfish mob;
        private final double speedModifier;
        private final boolean followingTargetEvenIfNotSeen;
        private Path path;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private int rangedAttackCD;
        private long lastCanUseCheck;
        private int failedPathFindingPenalty = 0;
        private boolean canPenalize = false;
        private int animTime = 0;
        private double targetOldX;
        private double targetOldY;
        private double targetOldZ;
        private int meleeRange = 75;
        private int jumpCD = 0;
        Vec3 slamOffSet = new Vec3(0, 0, 4);
        Vec3 pokeOffSet = new Vec3(0, 0.25, 5);
        Vec3 slashOffSet = new Vec3(0, -0.3, 2);
        Vec3 pissOffSet = new Vec3(0, 2, 2);

        public CrayfishMeleeAttackGoal(EntityCrayfish theMob, double speedMod, boolean persistentMemory) {
            this.mob = theMob;
            this.speedModifier = speedMod;
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
            this.path = this.mob.getNavigation().createPath(livingentity, 0);
            if (this.path != null) {
                return true;
            }
            return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null || livingentity instanceof EntityCrayfish || !livingentity.isAlive()) {
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
            this.rangedAttackCD = 0;
            this.jumpCD = 0;
            this.animTime = 0;
            this.mob.setAnimationState(0);
        }

        public void stop() {
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity) || livingentity instanceof EntityCrayfish) {
                this.mob.setTarget(null);
            }
            this.mob.setAnimationState(0);
            this.mob.isJumping = false;
        }

        public void tick() {
            LivingEntity target = this.mob.getTarget();
            double distance = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            int animState = this.mob.getAnimationState();

            switch (animState) {
                case 21 -> tickRightClawAttack();
                case 22 -> tickLeftClawAttack();
                case 23 -> tickSlamAttack();
                case 24 -> {
                    this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
                    this.mob.yBodyRot = this.mob.yHeadRot;
                    tickPiss();
                }
                case 25 -> tickJump();
                case 26 -> tickJumpAttack();
                default -> {
                    this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                    this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                    this.rangedAttackCD = Math.max(this.rangedAttackCD - 1, 0);
                    this.jumpCD = Math.max(this.jumpCD - 1, 0);
                    this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    this.doMovement(target, distance);
                    this.checkForCloseRangeAttack(distance, meleeRange);
                    this.checkForJump(target, distance);
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

        protected void checkForCloseRangeAttack(double distance, double meleeRange) {
            if (distance <= meleeRange && this.ticksUntilNextAttack <= 0) {
                int r = this.mob.getRandom().nextInt(100) + 1;
                double heightDiff = Math.abs(this.mob.getTarget().getY() - this.mob.getY());
                if (heightDiff >= 1.0 && this.jumpCD <= 0 && distance <= 100) {
                    if (r <= 60) { // Increased probability for jump attack
                        System.out.println("Crayfish chose Jump Attack (26), distance: " + distance + ", heightDiff: " + heightDiff);
                        this.mob.setAnimationState(26); // Jump Attack
                        this.jumpCD = this.mob.getRandom().nextInt(40, 80); // Adjusted cooldown
                    } else if (r <= 80) {
                        System.out.println("Crayfish chose Slam Attack (23), distance: " + distance + ", heightDiff: " + heightDiff);
                        this.mob.setAnimationState(23); // Slam
                    } else {
                        System.out.println("Crayfish chose Piss Attack (24), distance: " + distance + ", heightDiff: " + heightDiff);
                        this.mob.setAnimationState(24); // Piss
                    }
                } else if (distance <= 50) {
                    if (r <= 50) {
                        System.out.println("Crayfish chose Stab Attack (21), distance: " + distance);
                        this.mob.setAnimationState(21); // Stab
                    } else if (r <= 75) {
                        System.out.println("Crayfish chose Slash Attack (22), distance: " + distance);
                        this.mob.setAnimationState(22); // Slash
                    } else {
                        System.out.println("Crayfish chose Slam Attack (23), distance: " + distance);
                        this.mob.setAnimationState(23); // Slam
                    }
                } else if (distance <= 100) {
                    if (r <= 40) {
                        System.out.println("Crayfish chose Stab Attack (21), distance: " + distance);
                        this.mob.setAnimationState(21); // Stab
                    } else if (r <= 60) {
                        System.out.println("Crayfish chose Slash Attack (22), distance: " + distance);
                        this.mob.setAnimationState(22); // Slash
                    } else if (r <= 80 && this.jumpCD <= 0) {
                        System.out.println("Crayfish chose Jump Attack (26), distance: " + distance);
                        this.mob.setAnimationState(26); // Jump Attack
                        this.jumpCD = this.mob.getRandom().nextInt(40, 80); // Adjusted cooldown
                    } else {
                        System.out.println("Crayfish chose Slam Attack (23), distance: " + distance);
                        this.mob.setAnimationState(23); // Slam
                    }
                }
            } else if (this.ticksUntilNextAttack <= 0 && this.rangedAttackCD <= 0) {
                System.out.println("Crayfish chose Piss Attack (24) due to distance or cooldown, distance: " + distance);
                this.mob.setAnimationState(24); // Piss if target too far
            }
        }

        protected void checkForJump(LivingEntity target, double distance) {
            if (this.jumpCD <= 0 && !this.mob.isJumping && this.mob.onGround() && Math.abs(target.getY() - this.mob.getY()) >= 0.5 && distance <= 100) {
                int r = this.mob.getRandom().nextInt(100) + 1;
                if (r <= 50) { // 50% chance for regular jump when conditions are met
                    System.out.println("Crayfish triggered Jump (25), distance: " + distance + ", heightDiff: " + Math.abs(target.getY() - this.mob.getY()));
                    this.mob.setAnimationState(25);
                    this.mob.isJumping = true;
                    this.animTime = 0;
                    this.jumpCD = this.mob.getRandom().nextInt(40, 80);
                    Vec3 targetPos = target.position();
                    Vec3 mobPos = this.mob.position();
                    double dx = targetPos.x - mobPos.x;
                    double dy = targetPos.y - mobPos.y;
                    double dz = targetPos.z - mobPos.z;
                    double flatDist = Math.sqrt(dx * dx + dz * dz);
                    double jumpHeight = Math.min(dy + 8.0, 16.0);
                    double horizontalSpeed = Math.min(flatDist / 12.0, 1.0);
                    this.mob.setDeltaMovement(dx / flatDist * horizontalSpeed, Math.sqrt(2 * 0.08 * jumpHeight), dz / flatDist * horizontalSpeed);
                }
            }
        }

        protected void tickJump() {
            animTime++;
            this.mob.getNavigation().stop();
            this.mob.setAnimationState(25);
            if (animTime >= 20 || this.mob.onGround()) {
                this.animTime = 0;
                this.mob.setAnimationState(0);
                this.mob.isJumping = false;
                this.ticksUntilNextPathRecalculation = 0;
            }
        }

        protected void tickJumpAttack() {
            animTime++;
            this.mob.getNavigation().stop();
            if (animTime <= 20) {
                this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
                this.mob.yBodyRot = this.mob.yHeadRot;
                this.mob.setAnimationState(23); // Slam animation during prep
            } else if (animTime == 21) {
                LivingEntity target = this.mob.getTarget();
                this.mob.isJumping = true;
                this.mob.setAnimationState(25); // Air animation during jump
                Vec3 targetPos = target.position();
                Vec3 mobPos = this.mob.position();
                double dx = targetPos.x - mobPos.x;
                double dy = targetPos.y - mobPos.y;
                double dz = targetPos.z - mobPos.z;
                double flatDist = Math.sqrt(dx * dx + dz * dz);
                double jumpHeight = Math.min(dy + 8.0, 16.0);
                double horizontalSpeed = Math.min(flatDist / 12.0, 1.0);
                this.mob.setDeltaMovement(dx / flatDist * horizontalSpeed, Math.sqrt(2 * 0.08 * jumpHeight), dz / flatDist * horizontalSpeed);
            } else if (animTime >= 40 || this.mob.onGround()) {
                this.animTime = 0;
                double rot = this.mob.yBodyRot * (Math.PI / 180);
                Vec3 baseOffset = new Vec3(0, 0, 4);
                Vec3 rotatedOffset = new Vec3(
                        baseOffset.x * Math.cos(rot) - baseOffset.z * Math.sin(rot),
                        baseOffset.y,
                        baseOffset.x * Math.sin(rot) + baseOffset.z * Math.cos(rot)
                );
                Vec3 attackPos = this.mob.position().add(rotatedOffset);
                // Apply damage only to entities within the slam effect range
                double radius = 5.0;
                AABB hitbox = new AABB(attackPos.add(-radius, -radius, -radius), attackPos.add(radius, radius, radius));
                List<LivingEntity> victims = this.mob.level().getEntitiesOfClass(LivingEntity.class, hitbox, entity -> entity != this.mob);
                for (LivingEntity victim : victims) {
                    victim.hurt(new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 20f);
                }
                this.mob.level().playSound(null, attackPos.x, attackPos.y, attackPos.z, NSSSounds.CRAYFISH_SMASH.get(), this.mob.getSoundSource(), 0.7F, 0.5F);
                ScreenShakeEntity.ScreenShake(this.mob.level(), attackPos, 20.0f, 0.5f, 30, 7);
                this.mob.largeSlamBlockEffects(attackPos);
                this.mob.setAnimationState(0);
                this.mob.isJumping = false;
                this.ticksUntilNextPathRecalculation = 0;
                this.jumpCD = this.mob.getRandom().nextInt(40, 80);
                this.resetAttackCooldown(20);
            }
        }

        protected boolean getRangeCheck() {
            return this.mob.distanceToSqr(this.mob.getTarget().getX(), this.mob.getTarget().getY(), this.mob.getZ())
                    <= 1.8F * this.getAttackReachSqr(this.mob.getTarget());
        }

        protected void tickLeftClawAttack() {
            this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            this.mob.getNavigation().stop();
            animTime++;
            if (animTime == 16) {
                performLeftClawAttack();
            }
            if (animTime >= 24) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown(15);
                this.ticksUntilNextPathRecalculation = 0;
            }
        }

        protected void tickRightClawAttack() {
            this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            this.mob.getNavigation().stop();
            animTime++;
            if (animTime == 6) {
                performRightClawAttack();
            }
            if (animTime >= 12) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown(5);
                this.ticksUntilNextPathRecalculation = 0;
            }
        }

        protected void tickSlamAttack() {
            this.mob.lookAt(this.mob.getTarget(), 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            this.mob.getNavigation().stop();
            animTime++;
            if (animTime == 20) {
                performSlamAttack();
            }
            if (animTime >= 30) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown(20);
                this.ticksUntilNextPathRecalculation = 0;
            }
        }

        protected void tickPiss() {
            this.mob.getNavigation().stop();
            LivingEntity target = this.mob.getTarget();
            this.mob.lookAt(target, 100000, 100000);
            this.mob.yBodyRot = this.mob.yHeadRot;
            animTime++;
            if (animTime == 10) {
                piss(target);
            } else {
                this.targetOldX = target.getX();
                this.targetOldY = target.getY();
                this.targetOldZ = target.getZ();
            }
            if (animTime >= 18) {
                animTime = 0;
                this.mob.setAnimationState(0);
                this.resetAttackCooldown(mob.random.nextInt(1, 21));
                this.ticksUntilNextPathRecalculation = 0;
                this.rangedAttackCD = this.mob.getRandom().nextInt(200);
            }
        }

        protected void piss(LivingEntity target) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0));
            this.mob.getLookControl().setLookAt(target.position());
            this.mob.yBodyRot = this.mob.yHeadRot;
            double pissspeed = 7;
            double pissspeedforcalculation = pissspeed - 1;
            Vec3 targetVelocity = new Vec3((target.getX() - targetOldX), (target.getY() - targetOldY), (target.getZ() - targetOldZ));
            Vec3 tStartPos = target.position();
            Vec3 tTempPos = tStartPos;

            for (int count = 0; count < 1; count++) {
                double flatDist = Math.sqrt((this.mob.getX() - target.getX()) * (this.mob.getX() - target.getX()) + (this.mob.getZ() - target.getZ()) * (this.mob.getZ() - target.getZ()));
                double tallDist = Math.sqrt(flatDist * flatDist + (this.mob.getY() + 2 - target.getY()) * (this.mob.getY() + 2 - target.getY()));
                double pissReachTime = tallDist / pissspeedforcalculation;
                tTempPos = tTempPos.add(targetVelocity.multiply(pissReachTime - (0.1 * target.distanceTo(this.mob)), 0, pissReachTime - (0.1 * target.distanceTo(this.mob))));
                if (tTempPos.distanceTo(target.position()) <= 1) {
                    break;
                }
            }

            Vec3 finalTargetPos = tTempPos.add(0, target.getEyeHeight() * 0.5, 0);

            if (this.mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, finalTargetPos.x, finalTargetPos.y, finalTargetPos.z, NSSSounds.CRAYFISH_BLAST.get(), this.mob.getSoundSource(), 1.0F, 0.8F + this.mob.getRandom().nextFloat() * 0.4F);
            }

            if (this.mob.getVariant() <= 14 && 10 <= this.mob.getVariant()) {
                EntityBloodWater urine = new EntityBloodWater(NSSEntities.BLOODWATER.get(), this.mob.level());
                urine.setOwner(this.mob);
                urine.moveTo(this.mob.getX(), this.mob.getY() + 2, this.mob.getZ());
                urine.setTargetPos(finalTargetPos);
                this.mob.level().addFreshEntity(urine);
            } else if (this.mob.getVariant() <= 9 && 5 <= this.mob.getVariant()) {
                EntityIceWater urine = new EntityIceWater(NSSEntities.ICEWATER.get(), this.mob.level());
                urine.setOwner(this.mob);
                urine.moveTo(this.mob.getX(), this.mob.getY() + 2, this.mob.getZ());
                urine.setTargetPos(finalTargetPos);
                this.mob.level().addFreshEntity(urine);
            } else {
                EntityToxicWater urine = new EntityToxicWater(NSSEntities.TOXICWATER.get(), this.mob.level());
                urine.setOwner(this.mob);
                urine.moveTo(this.mob.getX(), this.mob.getY() + 2, this.mob.getZ());
                urine.setTargetPos(finalTargetPos);
                this.mob.level().addFreshEntity(urine);
            }

            PisslikeHitboxes.PivotedPolyHitCheck(this.mob, this.pissOffSet, 2f, 1.5f, 2f, (ServerLevel) this.mob.level(), 10f,
                    new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 2f, true);
        }

        protected void performSlamAttack() {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0));
            this.mob.playSound(NSSSounds.CRAYFISH_SMASH.get(), 0.5F, 0.5F);
            double rot = this.mob.yBodyRot * (Math.PI / 180);
            Vec3 baseOffset = new Vec3(0, 0, 4);
            Vec3 rotatedOffset = new Vec3(
                    baseOffset.x * Math.cos(rot) - baseOffset.z * Math.sin(rot),
                    baseOffset.y,
                    baseOffset.x * Math.sin(rot) + baseOffset.z * Math.cos(rot)
            );
            Vec3 attackPos = this.mob.position().add(rotatedOffset);
            double radius = 3.0;
            AABB hitbox = new AABB(attackPos.add(-radius, -radius, -radius), attackPos.add(radius, radius, radius));
            List<LivingEntity> victims = this.mob.level().getEntitiesOfClass(LivingEntity.class, hitbox, entity -> entity != this.mob);
            for (LivingEntity victim : victims) {
                victim.hurt(new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 15f);
            }
            this.mob.level().playSound(null, attackPos.x, attackPos.y, attackPos.z, NSSSounds.CRAYFISH_SMASH.get(), this.mob.getSoundSource(), 0.5F, 0.5F);
            ScreenShakeEntity.ScreenShake(this.mob.level(), attackPos, 15.0f, 0.4f, 20, 5);
            this.mob.slamBlockEffects(attackPos);
        }

        protected void performLeftClawAttack() {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0));
            this.mob.playSound(NSSSounds.CRAYFISH_ATTACK.get(), 0.5F, 0.5F);
            PisslikeHitboxes.PivotedPolyHitCheck(this.mob, this.slashOffSet, 5.5f, 1f, 5.5f, (ServerLevel) this.mob.level(), 10f,
                    new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 3f, false);
        }

        protected void performRightClawAttack() {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0));
            this.mob.playSound(NSSSounds.CRAYFISH_ATTACK.get(), 0.5F, 0.5F);
            PisslikeHitboxes.PivotedPolyHitCheck(this.mob, this.pokeOffSet, 0.5f, 2f, 0.5f, (ServerLevel) this.mob.level(), 10f,
                    new DamageSource(this.mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), this.mob), 0.3f, false);
        }

        protected void resetAttackCooldown(int CD) {
            this.ticksUntilNextAttack = CD;
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected int getAttackReachSqr(LivingEntity entity) {
            return (int) (this.mob.getBbWidth() * 2.5F * this.mob.getBbWidth() * 1.8F + entity.getBbWidth());
        }
    }

    public void slamBlockEffects(Vec3 attackPos) {
        if (!this.level().isClientSide && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            double radius = 3.0;
            int numBlocks = Math.min(Mth.ceil(radius * radius * Math.PI / 2), 16);
            RandomSource random = this.getRandom();
            for (int i = 0; i < numBlocks; i++) {
                double r = radius * Math.sqrt(random.nextDouble());
                double theta = random.nextDouble() * 2 * Math.PI;
                double px = attackPos.x + r * Math.cos(theta);
                double pz = attackPos.z + r * Math.sin(theta);
                int hitX = Mth.floor(px);
                int hitZ = Mth.floor(pz);
                int hitY = Mth.floor(attackPos.y);
                BlockPos pos = new BlockPos(hitX, hitY, hitZ);
                BlockState block = this.level().getBlockState(pos.below());
                int maxDepth = 256;
                for (int depthCount = 0; depthCount < maxDepth; depthCount++) {
                    if (block.getRenderShape() == RenderShape.MODEL) {
                        break;
                    }
                    pos = pos.below();
                    block = this.level().getBlockState(pos);
                }
                if (block.getRenderShape() != RenderShape.MODEL) {
                    block = Blocks.AIR.defaultBlockState();
                }
                FallingBlockEntity fallingBlock = new FallingBlockEntity(this.level(), hitX + 0.5D, hitY + 1.0D, hitZ + 0.5D, block, 20);
                fallingBlock.push(0, 0.2D + random.nextGaussian() * 0.15D, 0);
                this.level().addFreshEntity(fallingBlock);
                if (block.is(NSSTags.CRAYFISH_BREAKABLES)) {
                    ((ServerLevel) this.level()).sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, block),
                            hitX + 0.5D, hitY, hitZ + 0.5D, 10, 0.5, 0.5, 0.5, 0.1);
                    this.level().destroyBlock(pos, true, this);
                }
            }
        }
    }

    protected void largeSlamBlockEffects(Vec3 attackPos) {
        if (!this.level().isClientSide && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            double radius = 5.0;
            int numBlocks = Math.min(Mth.ceil(radius * radius * Math.PI / 2), 25);
            RandomSource random = this.getRandom();
            for (int i = 0; i < numBlocks; i++) {
                double r = radius * Math.sqrt(random.nextDouble());
                double theta = random.nextDouble() * 2 * Math.PI;
                double px = attackPos.x + r * Math.cos(theta);
                double pz = attackPos.z + r * Math.sin(theta);
                int hitX = Mth.floor(px);
                int hitZ = Mth.floor(pz);
                int hitY = Mth.floor(attackPos.y);
                BlockPos pos = new BlockPos(hitX, hitY, hitZ);
                BlockState block = this.level().getBlockState(pos.below());
                int maxDepth = 256;
                for (int depthCount = 0; depthCount < maxDepth; depthCount++) {
                    if (block.getRenderShape() == RenderShape.MODEL) {
                        break;
                    }
                    pos = pos.below();
                    block = this.level().getBlockState(pos);
                }
                if (block.getRenderShape() != RenderShape.MODEL) {
                    block = Blocks.AIR.defaultBlockState();
                }
                FallingBlockEntity fallingBlock = new FallingBlockEntity(this.level(), hitX + 0.5D, hitY + 1.0D, hitZ + 0.5D, block, 30);
                fallingBlock.push(0, 0.3D + random.nextGaussian() * 0.2D, 0);
                this.level().addFreshEntity(fallingBlock);
                if (block.is(NSSTags.CRAYFISH_BREAKABLES)) {
                    ((ServerLevel) this.level()).sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, block),
                            hitX + 0.5D, hitY, hitZ + 0.5D, 15, 0.75, 0.75, 0.75, 0.15);
                    this.level().destroyBlock(pos, true, this);
                }
            }
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    protected float getDamageAfterArmorAbsorb(DamageSource pDamageSource, float pDamageAmount) {
        pDamageAmount = super.getDamageAfterArmorAbsorb(pDamageSource, pDamageAmount);
        if (pDamageSource.is(DamageTypes.HOT_FLOOR) || pDamageSource.is(DamageTypes.LAVA) || pDamageSource.is(DamageTypes.IN_FIRE) || pDamageSource.is(DamageTypes.ON_FIRE)) {
            pDamageAmount = (float) (0.25 * pDamageAmount);
        }
        return pDamageAmount;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        boolean blowthrough = false;
        if (source.getDirectEntity() instanceof AbstractArrow) {
            if (((AbstractArrow) source.getDirectEntity()).getPierceLevel() >= 1) {
                blowthrough = true;
            }
        }
        return source.is(DamageTypes.FALL) ||
                source.is(DamageTypes.IN_WALL) ||
                source.is(DamageTypes.CACTUS) ||
                source.is(DamageTypes.STALAGMITE) ||
                source.is(DamageTypes.FALLING_ANVIL) ||
                (source.is(DamageTypes.ARROW) && !blowthrough) ||
                (source.is(DamageTypes.IN_FIRE) && this.getVariant() == 2) ||
                (source.is(DamageTypes.LAVA) && this.getVariant() == 2) ||
                (source.is(DamageTypes.HOT_FLOOR) && this.getVariant() == 2) ||
                (source.is(DamageTypes.ON_FIRE) && this.getVariant() == 2) ||
                super.isInvulnerableTo(source);
    }

    @Override
    public void kill() {
        this.remove(RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
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

    private PlayState predicate(AnimationState<EntityCrayfish> state) {
        int animState = this.getAnimationState();
        AnimationController<?> controller = state.getController();
        switch (animState) {
            case 21:
                controller.setAnimation(RIGHT_CLAW_ANIM);
                controller.setAnimationSpeed(0.9F);
                return PlayState.CONTINUE;
            case 22:
                controller.setAnimation(LEFT_CLAW_ANIM);
                controller.setAnimationSpeed(0.8F);
                return PlayState.CONTINUE;
            case 23:
                controller.setAnimation(SLAM_ANIM);
                controller.setAnimationSpeed(0.8F);
                return PlayState.CONTINUE;
            case 24:
                controller.setAnimation(SNIPE_ANIM);
                controller.setAnimationSpeed(0.8F);
                return PlayState.CONTINUE;
            case 25:
                controller.setAnimation(AIR_ANIM);
                controller.setAnimationSpeed(1.0F);
                return PlayState.CONTINUE;
            case 26:
                controller.setAnimation(AIR_ANIM);
                controller.setAnimationSpeed(1.0F);
                return PlayState.CONTINUE;
            default:
                double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
                if (speed > 0.06) {
                    controller.setAnimationSpeed(1 + (this.directionlessSpeed / 0.24));
                    controller.setAnimation(WALK_ANIM);
                } else {
                    controller.setAnimationSpeed(1.0);
                    controller.setAnimation(IDLE_ANIM);
                }
                return PlayState.CONTINUE;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, this::predicate));
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

    private static boolean canSpawnBlood(LevelAccessor worldIn, BlockPos position) {
        return worldIn.getBiome(position).is(NSSTags.SPAWNS_BLOOD_CRAYFISH);
    }

    private static boolean canSpawnIce(LevelAccessor worldIn, BlockPos position) {
        return worldIn.getBiome(position).is(NSSTags.SPAWNS_ICE_CRAYFISH);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        int rarityRoll = (this.getRandom().nextInt(100) + 1);
        int i;
        if (canSpawnBlood(worldIn, this.blockPosition())) {
            if (rarityRoll >= 100) {
                i = 14;
            } else if (rarityRoll > 90) {
                i = 13;
            } else {
                i = this.random.nextIntBetweenInclusive(10, 12);
            }
            this.biomeVariant = 2;
        } else if (canSpawnIce(worldIn, this.blockPosition())) {
            if (rarityRoll >= 100) {
                i = 9;
            } else if (rarityRoll > 90) {
                i = 8;
            } else {
                i = this.random.nextIntBetweenInclusive(5, 7);
            }
            this.biomeVariant = 1;
        } else {
            if (rarityRoll >= 100) {
                i = 4;
            } else if (rarityRoll > 90) {
                i = 3;
            } else {
                i = this.random.nextIntBetweenInclusive(0, 2);
            }
            this.biomeVariant = 0;
        }
        this.setVariant(i);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    public static boolean canSpawn(EntityType type, LevelAccessor worldIn, MobSpawnType reason, BlockPos pos, RandomSource randomIn) {
        return worldIn.getBlockState(pos.below()).canOcclude();
    }

    public boolean checkSpawnRules(LevelAccessor worldIn, MobSpawnType spawnReasonIn) {
        return NSSEntities.rollSpawn(NotSoShrimpleConfig.crayfishSpawnRolls, this.getRandom(), spawnReasonIn);
    }

    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    protected PathNavigation createNavigation(Level p_33348_) {
        return new RexNavigation(this, p_33348_);
    }

    static class RexNavigation extends GroundPathNavigation {
        public RexNavigation(Mob mob, Level level) {
            super(mob, level);
        }

        protected PathFinder createPathFinder(int maxVisitedNodes) {
            this.nodeEvaluator = new EntityCrayfish.RexNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
        }
    }

    static class RexNodeEvaluator extends WalkNodeEvaluator {
        @Override
        protected BlockPathTypes evaluateBlockPathType(BlockGetter level, BlockPos pos, BlockPathTypes type) {
            return type == BlockPathTypes.LEAVES ? BlockPathTypes.OPEN : super.evaluateBlockPathType(level, pos, type);
        }
    }
}
//