package com.peeko32213.notsoshrimple.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.peeko32213.notsoshrimple.client.render.SwampBusterRenderer;
import com.peeko32213.notsoshrimple.common.entity.utl.MathHelpers;
import com.peeko32213.notsoshrimple.common.entity.utl.PisslikeHitboxes;
import com.peeko32213.notsoshrimple.common.entity.utl.FallingBlockEntity;
import com.peeko32213.notsoshrimple.common.entity.utl.ScreenShakeEntity;
import com.peeko32213.notsoshrimple.core.registry.NSSItems;
import com.peeko32213.notsoshrimple.core.registry.NSSSounds;
import com.peeko32213.notsoshrimple.core.registry.NSSTags;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class ItemClawblade extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public Vec3 slamOffset = new Vec3(0, -2, 0);
    public double arthropodBonus = 0.3;
    public int animationState = 0;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private boolean isCharging = false;
    private int chargeTicks = 0;
    private double startY = 0.0;
    private double maxY = 0.0;
    private boolean wasOnGround = true;

    public ItemClawblade(Properties properties) {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).durability(1876));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 14.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.5F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }

    public void switchAnimationState(int value) {
        this.animationState = value;
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState pState, Level pLevel, net.minecraft.core.BlockPos pPos, Player pPlayer) {
        return false;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @Override
    public AABB getSweepHitBox(ItemStack stack, Player player, Entity target) {
        return target.getBoundingBox().inflate(0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        super.onLeftClickEntity(stack, player, entity);
        if (player.getMainHandItem().getItem() instanceof ItemClawblade && !player.level().isClientSide() && !player.getCooldowns().isOnCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get())) {
            if (entity instanceof LivingEntity victim) {
                Vec2 knockVec = MathHelpers.OrizontalAimVector(
                        MathHelpers.AimVector(new Vec3(-player.position().x, -player.position().y, -player.position().z),
                                new Vec3(-victim.position().x, -victim.position().y, -victim.position().z)));
                victim.knockback(1.5, knockVec.x, knockVec.y);
            }
        }
        return player.getAttackStrengthScale(0) < 0 || player.attackAnim != 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity hurtEntity, LivingEntity player) {
        super.hurtEnemy(stack, hurtEntity, player);
        Vec2 knockVec = MathHelpers.OrizontalAimVector(
                MathHelpers.AimVector(new Vec3(-player.position().x, -player.position().y, -player.position().z),
                        new Vec3(-hurtEntity.position().x, -hurtEntity.position().y, -hurtEntity.position().z)));
        hurtEntity.knockback(1.5, knockVec.x, knockVec.y);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getCooldowns().isOnCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get())) {
            player.startUsingItem(hand);
            this.isCharging = true;
            this.chargeTicks = 0;
            this.startY = player.getY();
            this.maxY = player.getY();
            this.wasOnGround = player.onGround();
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (entity instanceof Player player && isSelected && this.isCharging) {
            if (player.isUsingItem()) {
                this.chargeTicks++;
                this.maxY = Math.max(this.maxY, player.getY());
                if (this.chargeTicks >= 120) { // Cap at 6 seconds (120 ticks at 20 ticks/second)
                    this.chargeTicks = 120;
                    this.switchAnimationState(2); // Spear pose when fully charged
                } else {
                    this.switchAnimationState(1); // Bow pose during charging
                }
            }
            // Trigger slam only if charged for at least 6 seconds and landing after a fall
            if (player.onGround() && !this.wasOnGround && this.chargeTicks >= 120 && !player.getCooldowns().isOnCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get())) {
                float fallDistance = (float)(this.maxY - player.getY());
                if (fallDistance > 0 && level instanceof ServerLevel serverLevel) {
                    performSlamAttack(player, stack, fallDistance, serverLevel);
                    resetCharge(player);
                }
            }
            this.wasOnGround = player.onGround();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player && !level.isClientSide && !player.getCooldowns().isOnCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get())) {
            if (this.chargeTicks >= 120 && level instanceof ServerLevel serverLevel) { // Only perform slam if fully charged (6 seconds)
                float fallDistance = (float)(this.maxY - player.getY());
                performSlamAttack(player, stack, Math.max(fallDistance, 0.0f), serverLevel);
            }
            resetCharge(player);
        }
    }

    private void performSlamAttack(Player player, ItemStack stack, float fallDistance, ServerLevel serverLevel) {
        float baseRadius = 5.0f;
        float baseDamage = 8.0f; // Increased base damage
        float multiplier = getFallMultiplier(fallDistance);
        float radius = baseRadius * multiplier;
        float damage = baseDamage * multiplier;
        this.switchAnimationState(3); // Slam animation
        player.playSound(NSSSounds.CRAYFISH_SMASH.get(), 0.5F, 1.0F);
        PisslikeHitboxes.PivotedPolyHitCheck(player, slamOffset, radius, 2, radius, serverLevel, 5,
                new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), player), damage, false);
        stack.hurtAndBreak(50, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        player.getCooldowns().addCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get(), 20 * 3);
        ScreenShakeEntity.ScreenShake(player.level(), player.position().add(slamOffset), radius * 2, 0.3f, 20, 5);
        this.slamBlockEffects(player, radius);
    }

    private float getFallMultiplier(float fallDistance) {
        if (fallDistance <= 1.0f) return 1.0f;
        if (fallDistance <= 2.0f) return 1.2f;
        if (fallDistance <= 3.0f) return 1.3f;
        if (fallDistance <= 4.0f) return 1.4f;
        if (fallDistance <= 5.0f) return 1.5f;
        return Mth.lerp((fallDistance - 5.0f) / 5.0f, 1.5f, 2.0f);
    }

    private void resetCharge(Player player) {
        this.isCharging = false;
        this.chargeTicks = 0;
        this.startY = 0.0;
        this.maxY = 0.0;
        this.switchAnimationState(0);
        player.stopUsingItem();
    }

    private void slamBlockEffects(Player player, float radius) {
        if (!player.level().isClientSide && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(player.level(), player)) {
            int numBlocks = Math.min(Mth.ceil(radius * radius * Math.PI), 50); // Scale blocks with radius
            RandomSource random = player.getRandom();
            Vec3 center = player.position().add(slamOffset);
            for (int i = 0; i < numBlocks; i++) {
                double r = radius * Math.sqrt(random.nextDouble());
                double theta = random.nextDouble() * 2 * Math.PI;
                double px = center.x + r * Math.cos(theta);
                double pz = center.z + r * Math.sin(theta);
                int hitX = Mth.floor(px);
                int hitZ = Mth.floor(pz);
                int hitY = Mth.floor(center.y);
                BlockPos pos = new BlockPos(hitX, hitY, hitZ);
                BlockState block = player.level().getBlockState(pos.below());
                int maxDepth = 256;
                for (int depthCount = 0; depthCount < maxDepth; depthCount++) {
                    if (block.getRenderShape() == RenderShape.MODEL) {
                        break;
                    }
                    pos = pos.below();
                    block = player.level().getBlockState(pos);
                }
                if (block.getRenderShape() != RenderShape.MODEL) {
                    block = Blocks.AIR.defaultBlockState();
                }
                FallingBlockEntity fallingBlock = new FallingBlockEntity(player.level(), hitX + 0.5D, hitY + 1.0D, hitZ + 0.5D, block, 20);
                fallingBlock.push(0, 0.2D + random.nextGaussian() * 0.15D, 0);
                player.level().addFreshEntity(fallingBlock);
                if (block.is(NSSTags.CRAYFISH_BREAKABLES)) {
                    ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                            new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, block),
                            hitX + 0.5D, hitY, hitZ + 0.5D, 15, 0.75, 0.75, 0.75, 0.15);
                    player.level().destroyBlock(pos, true, player);
                }
            }
            player.level().playSound(null, center.x, center.y, center.z, NSSSounds.CRAYFISH_SMASH.get(), player.getSoundSource(), 0.5F, 1.0F);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (this.chargeTicks >= 120) { // Spear pose when fully charged (6 seconds)
            return UseAnim.SPEAR;
        } else {
            return UseAnim.BOW;
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private final SwampBusterRenderer renderer = new SwampBusterRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (entityLiving.isUsingItem() && entityLiving.getUseItem() == itemStack) {
                    ItemClawblade clawblade = (ItemClawblade) itemStack.getItem();
                    if (clawblade.chargeTicks >= 120) { // Spear pose when fully charged
                        return net.minecraft.client.model.HumanoidModel.ArmPose.THROW_SPEAR;
                    } else {
                        return net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<ItemClawblade> state) {
        switch (animationState) {
            case 3:
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("animation.swampbuster.slam"));
                break;
            default:
                state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.swampbuster.idle"));
                break;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.getAttackStrengthScale(0) < 1 && player.attackAnim > 0) {
                return true;
            } else {
                player.swingTime = -1;
            }
        }
        return false;
    }
}