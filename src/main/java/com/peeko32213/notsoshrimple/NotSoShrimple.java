package com.peeko32213.notsoshrimple;

import com.mojang.logging.LogUtils;
import com.peeko32213.notsoshrimple.common.entity.EntityCrayfish;
import com.peeko32213.notsoshrimple.common.entity.EntityManeaterShell;
import com.peeko32213.notsoshrimple.core.config.BiomeConfig;
import com.peeko32213.notsoshrimple.core.config.ConfigHolder;
import com.peeko32213.notsoshrimple.core.config.NotSoShrimpleConfig;
import com.peeko32213.notsoshrimple.core.registry.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import

import java.util.ArrayList;
import java.util.List;

@Mod(NotSoShrimple.MODID)
public class NotSoShrimple {


    public static final String MODID = "notsoshrimple";
    public static final List<Runnable> CALLBACKS = new ArrayList<>();
    public static final Logger LOGGER = LogManager.getLogger();

    // Register Creative Mode Tab
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NotSoShrimple.MODID);
    public static final RegistryObject<CreativeModeTab> SHRIMPLE = CREATIVE_MODE_TABS.register("shrimple_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> NSSItems.CLAW.get().getDefaultInstance())
            .build());

    public NotSoShrimple() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onModConfigEvent);
        NSSSounds.DEF_REG.register(modEventBus);
        NSSItems.ITEMS.register(modEventBus);
        NSSEntities.ENTITIES.register(modEventBus);
        NSSParticles.SHRIMPARTICLES.register(modEventBus);
        NSSRecipes.SERIALIZERS.register(modEventBus);
        NSSAttributes.ATTRIBUTEREGISTER.register(modEventBus);
        NSSWorldRegistry.STRUCTURE_MODIFIERS.register(modEventBus);
        NSSWorldRegistry.StructureModifierReg.register();
        NSSLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus); // Register the creative tab

        eventBus.register(this);

        final ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC, "notsoshrimple.toml");
        modEventBus.addListener(this::addCreativeTabContents);
    }

    @SubscribeEvent
    public void onModConfigEvent(final ModConfigEvent event) {
        final ModConfig config = event.getConfig();
        if (config.getSpec() == ConfigHolder.COMMON_SPEC) {
            NotSoShrimpleConfig.bake(config);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SpawnPlacements.register(NSSEntities.MANEATER.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityManeaterShell::canSpawn);
            SpawnPlacements.register(NSSEntities.CRAYFISH.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityCrayfish::canSpawn);
        });
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == SHRIMPLE.get()) {
            event.accept(NSSItems.CRAYFISH_SPAWN);
            event.accept(NSSItems.MANEATER_SPAWN);
            event.accept(NSSItems.CLAW);
            event.accept(NSSItems.GREAT_PRAWN_CLAWBLADE);
            event.accept(NSSItems.RAW_PRAWN);
            event.accept(NSSItems.COOKED_PRAWN);
            event.accept(NSSItems.SMITHING_STONE);
            event.accept(NSSItems.SOMBER_STONE);
            event.accept(NSSItems.PURGING_STONE);
        }
    }
}