package com.peeko32213.notsoshrimple.core.registry;

import com.peeko32213.notsoshrimple.NotSoShrimple;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NSSSounds {
    public static final DeferredRegister<SoundEvent> DEF_REG = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NotSoShrimple.MODID);

    public static final RegistryObject<SoundEvent> CRAYFISH_IDLE = createSoundEvent("crayfish_idle");
    public static final RegistryObject<SoundEvent> CRAYFISH_HURT = createSoundEvent("crayfish_hurt");
    public static final RegistryObject<SoundEvent> CRAYFISH_ATTACK = createSoundEvent("crayfish_attack");
    public static final RegistryObject<SoundEvent> CRAYFISH_DEATH = createSoundEvent("crayfish_death");
    public static final RegistryObject<SoundEvent> CRAYFISH_SCUTTLE = createSoundEvent("crayfish_scuttle");
    public static final RegistryObject<SoundEvent> CRAYFISH_SMASH = createSoundEvent("crayfish_smash");
    public static final RegistryObject<SoundEvent> CRAYFISH_BLAST = createSoundEvent("crayfish_blast");

    private static RegistryObject<SoundEvent> createSoundEvent(final String soundName) {
        return DEF_REG.register(soundName, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(NotSoShrimple.MODID, soundName)));
    }
}