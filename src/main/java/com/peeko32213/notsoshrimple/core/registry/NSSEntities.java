package com.peeko32213.notsoshrimple.core.registry;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import com.peeko32213.notsoshrimple.common.entity.*;
import com.peeko32213.notsoshrimple.common.entity.utl.FallingBlockEntity;
import com.peeko32213.notsoshrimple.common.entity.utl.ScreenShakeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = NotSoShrimple.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NSSEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
            NotSoShrimple.MODID);

    public static final RegistryObject<EntityType<EntityCrayfish>> CRAYFISH = ENTITIES.register("crayfish",
            () -> EntityType.Builder.of(EntityCrayfish::new, MobCategory.MONSTER).sized(3.4F, 2.5F).canSpawnFarFromPlayer()
                    .build(new ResourceLocation(NotSoShrimple.MODID, "crayfish").toString()));

    public static final RegistryObject<EntityType<EntityManeaterShell>> MANEATER = ENTITIES.register("maneater",
            () -> EntityType.Builder.of(EntityManeaterShell::new, MobCategory.MONSTER).sized(2.5F, 3.0F)
                    .build(new ResourceLocation(NotSoShrimple.MODID, "maneater").toString()));

    public static final RegistryObject<EntityType<EntityToxicWater>> TOXICWATER = ENTITIES.register("toxicwater",
            () -> EntityType.Builder.of(EntityToxicWater::new, MobCategory.MISC).sized(0f, 0f).noSummon()
                    .build(new ResourceLocation(NotSoShrimple.MODID, "toxicwater").toString()));

    public static final RegistryObject<EntityType<EntityIceWater>> ICEWATER = ENTITIES.register("icewater",
            () -> EntityType.Builder.of(EntityIceWater::new, MobCategory.MISC).sized(5.0F, 6.5F).noSummon()
                    .build(new ResourceLocation(NotSoShrimple.MODID, "icewater").toString()));

    public static final RegistryObject<EntityType<EntityBloodWater>> BLOODWATER = ENTITIES.register("bloodwater",
            () -> EntityType.Builder.of(EntityBloodWater::new, MobCategory.MISC).sized(5.0F, 6.5F).noSummon()
                    .build(new ResourceLocation(NotSoShrimple.MODID, "bloodwater").toString()));

    public static final RegistryObject<EntityType<ScreenShakeEntity>> SCREEN_SHAKE = ENTITIES.register("screen_shake",
            () -> EntityType.Builder.<ScreenShakeEntity>of(ScreenShakeEntity::new, MobCategory.MISC)
                    .setTrackingRange(0).setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false)
                    .build(new ResourceLocation(NotSoShrimple.MODID, "screen_shake").toString()));

    public static final RegistryObject<EntityType<FallingBlockEntity>> FALLING_BLOCK = ENTITIES.register("falling_block",
            () -> EntityType.Builder.<FallingBlockEntity>of(FallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).setTrackingRange(10).setUpdateInterval(20)
                    .build(new ResourceLocation(NotSoShrimple.MODID, "falling_block").toString()));

    public static boolean rollSpawn(int rolls, RandomSource random, MobSpawnType reason){
        if(reason == MobSpawnType.SPAWNER){
            return true;
        }else{
            return rolls <= 0 || random.nextInt(rolls) == 0;
        }
    }
}