package com.peeko32213.notsoshrimple.core.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @ModifyVariable(
            method = "hurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getDamageValue()I"),
            argsOnly = true
    )
    public int modifyDamageAmount(int pAmount, int originalAmount, RandomSource pRandom, ServerPlayer pUser) {
        ItemStack itemStack = (ItemStack) (Object) this;

        if (itemStack.isDamageableItem()) {
            CompoundTag itemTags = itemStack.getOrCreateTag();
            if (itemTags.contains("SmithingDurabilityBuff")) {
                int currentDurabilityBuff = itemTags.getInt("SmithingDurabilityBuff");
                if (pAmount >= currentDurabilityBuff) {
                    itemTags.putInt("SmithingDurabilityBuff", 0);
                    itemStack.setTag(itemTags);
                    pAmount -= currentDurabilityBuff;
                } else {
                    itemTags.putInt("SmithingDurabilityBuff", currentDurabilityBuff - pAmount);
                    itemStack.setTag(itemTags);
                    pAmount = 0;
                }
            }
        }

        return pAmount;
    }
}