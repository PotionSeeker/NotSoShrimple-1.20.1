package com.peeko32213.notsoshrimple.core.recipes;

import com.google.gson.JsonObject;
import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.core.config.NotSoShrimpleConfig;
import com.peeko32213.notsoshrimple.core.registry.NSSItems;
import com.peeko32213.notsoshrimple.core.registry.NSSTags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class SmithingStoneRecipe extends SmithingTransformRecipe {
    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public SmithingStoneRecipe(ResourceLocation pId, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
        super(pId, pTemplate, pBase, pAddition, pBase.getItems().length > 0 ? pBase.getItems()[0] : ItemStack.EMPTY);
        this.id = pId;
        this.template = pTemplate;
        this.base = pBase;
        this.addition = pAddition;
    }

    @Override
    public boolean matches(Container pInv, Level pLevel) {
        if (!super.matches(pInv, pLevel)) {
            return false;
        }

        ItemStack baseStack = pInv.getItem(1); // Slot 1 is the base item in smithing table
        Item toBeSmithed = baseStack.getItem();
        CompoundTag itemTags = baseStack.getOrCreateTag().copy();

        // Check if the base item is in the whitelist
        if (baseStack.is(NSSTags.SMITHINGWHITELIST)) {
            return true;
        }

        // Check somber stone cap
        if (addition.test(NSSItems.SOMBER_STONE.get().getDefaultInstance()) &&
                (itemTags.getInt("SomberDamageBuff") >= NotSoShrimpleConfig.somberCap && NotSoShrimpleConfig.somberCap != -1)) {
            return false;
        }

        // Check smithing stone cap
        if (addition.test(NSSItems.SMITHING_STONE.get().getDefaultInstance()) &&
                (itemTags.getInt("SmithingDurabilityBuff") >= NotSoShrimpleConfig.smithingCap && NotSoShrimpleConfig.smithingCap != -1)) {
            return false;
        }

        // Allow smithing for specific item types and ensure not blacklisted
        return (toBeSmithed instanceof TieredItem ||
                toBeSmithed instanceof ShieldItem ||
                toBeSmithed instanceof ProjectileWeaponItem ||
                toBeSmithed instanceof ElytraItem ||
                toBeSmithed instanceof TridentItem) &&
                this.addition.test(pInv.getItem(2)) && // Slot 2 is the addition item
                !baseStack.is(NSSTags.SMITHINGBLACKLIST);
    }

    @Override
    public ItemStack assemble(Container pInv, RegistryAccess pRegistryAccess) {
        ItemStack itemstack = pInv.getItem(1).copy(); // Copy the base item (slot 1)

        if (addition.test(NSSItems.SOMBER_STONE.get().getDefaultInstance())) {
            CompoundTag itemTags = itemstack.getOrCreateTag().copy();
            int currentDmgBuff = itemTags.getInt("SomberDamageBuff");
            itemTags.putInt("SomberDamageBuff", currentDmgBuff + 1);
            itemstack.setTag(itemTags.copy());
        }

        if (addition.test(NSSItems.SMITHING_STONE.get().getDefaultInstance())) {
            CompoundTag itemTags = itemstack.getOrCreateTag().copy();
            int currentDurabilityBuff = itemTags.getInt("SmithingDurabilityBuff");
            itemTags.putInt("SmithingDurabilityBuff", currentDurabilityBuff + 10);
            itemstack.setTag(itemTags.copy());
        }

        return itemstack;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 3; // Template + Base + Addition
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return this.base.getItems().length > 0 ? this.base.getItems()[0].copy() : ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<SmithingStoneRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SmithingStoneRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "template"));
            Ingredient base = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "base"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "addition"));
            return new SmithingStoneRecipe(pRecipeId, template, base, addition);
        }

        @Override
        public SmithingStoneRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            Ingredient template = Ingredient.fromNetwork(pBuffer);
            Ingredient base = Ingredient.fromNetwork(pBuffer);
            Ingredient addition = Ingredient.fromNetwork(pBuffer);
            return new SmithingStoneRecipe(pRecipeId, template, base, addition);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SmithingStoneRecipe pRecipe) {
            pRecipe.template.toNetwork(pBuffer);
            pRecipe.base.toNetwork(pBuffer);
            pRecipe.addition.toNetwork(pBuffer);
        }
    }
}