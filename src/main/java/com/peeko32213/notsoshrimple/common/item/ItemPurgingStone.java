package com.peeko32213.notsoshrimple.common.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import net.minecraft.advancements.CriteriaTriggers;
//import net.minecraft.client.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.collect.Iterators.size;

public class ItemPurgingStone extends Item {
    public ItemPurgingStone(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 1;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        boolean removedAny = false;
        Iterator<MobEffectInstance> allStatus = pPlayer.getActiveEffects().iterator();
        ItemStack itemStack = pPlayer.getItemInHand(pHand);

        if (!pLevel.isClientSide) {

            while (allStatus.hasNext()) {
                MobEffectInstance effect = allStatus.next();
                //iterates through all the effect instances and removes anything that's considered negative

                if (!effect.getEffect().isBeneficial()) {
                    //removes the status
                    //pLevel.broadcastEntityEvent(pPlayer, (byte)35);
                    pPlayer.onEffectRemoved(effect);
                    allStatus.remove();

                    if (!pLevel.isClientSide) {
                        if (pPlayer instanceof ServerPlayer serverplayer) {
                            CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, itemStack);
                            serverplayer.awardStat(Stats.ITEM_USED.get(this));
                            pPlayer.getItemInHand(pHand).shrink(1);
                            //consumes the item if it has removed negative
                        }
                    }

                    break;
                }
            }

        }
        //serverside, actually uses the item

        if (pLevel.isClientSide && allStatus != null) {

            while (allStatus.hasNext()) {
                MobEffectInstance effect = allStatus.next();

                if (!effect.getEffect().isBeneficial()) {
                    removedAny = true;
                    break;
                }
            }
            //Fakes removing all status effects to trick the client into playing the sound and animation, DOES NOT ACTUALLY MODIFY STATUS

            if (removedAny == true) {
                Minecraft.getInstance().gameRenderer.displayItemActivation(pPlayer.getItemInHand(pHand));
                pPlayer.playSound(SoundEvents.BASALT_BREAK, 1.0F, 1.0F);
                //plays animation and sound if the player has negative effects
            }
        }
        //clientside


        if (removedAny == true) {
            return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
        } else {
            return InteractionResultHolder.fail(pPlayer.getMainHandItem());
        }
        //return

    }

}
