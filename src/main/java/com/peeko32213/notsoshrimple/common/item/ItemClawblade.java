package com.peeko32213.notsoshrimple.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.peeko32213.notsoshrimple.client.render.SwampBusterRenderer;
import com.peeko32213.notsoshrimple.common.entity.utl.MathHelpers;
import com.peeko32213.notsoshrimple.common.entity.utl.PisslikeHitboxes;
import com.peeko32213.notsoshrimple.core.registry.NSSItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class ItemClawblade extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public Vec3 slamOffset = new Vec3(0, -2, 0);
    public int slamDmg = 5;
    public double arthropodBonus = 0.3;
    public int animationState = 0;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ItemClawblade(Properties properties) {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).durability(1876));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 14.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.75F, AttributeModifier.Operation.ADDITION));
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
        if (player.getMainHandItem().getItem() instanceof ItemClawblade && player.fallDistance > 0 && player.isCrouching() && !player.level().isClientSide() && !player.getCooldowns().isOnCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get())) {
            this.switchAnimationState(1);
            if (entity instanceof LivingEntity victim) {
                Vec2 knockVec = MathHelpers.OrizontalAimVector(
                        MathHelpers.AimVector(new Vec3(-player.position().x, -player.position().y, -player.position().z),
                                new Vec3(-victim.position().x, -victim.position().y, -victim.position().z)));
                victim.knockback(1.5, knockVec.x, knockVec.y);
            }
            PisslikeHitboxes.PivotedPolyHitCheck(player, slamOffset, 4.0, 2, 4.0, (net.minecraft.server.level.ServerLevel) player.level(), 5,
                    new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), player), 1.5f, false);
            stack.hurtAndBreak(50, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            player.getCooldowns().addCooldown(NSSItems.GREAT_PRAWN_CLAWBLADE.get(), 20 * 3);
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
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final SwampBusterRenderer renderer = new SwampBusterRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<ItemClawblade> state) {
        if (animationState == 0) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.swampbuster.idle"));
        } else {
            state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("animation.swampbuster.slam"));
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

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean held) {
        if (entity instanceof Player player && held) {
            if (player.getAttackStrengthScale(0) < 0 && player.attackAnim > 0) {
                player.swingTime -= 0.1;
            }
        }
    }
}