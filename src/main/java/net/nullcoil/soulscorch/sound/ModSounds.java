package net.nullcoil.soulscorch.sound;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.nullcoil.soulscorch.Soulscorch;

public class ModSounds {
    public static final SoundEvent BLAZT_AMBIENT = registerSoundEvent("blazt_ambient");
    public static final SoundEvent BLAZT_BREATHE_IN = registerSoundEvent("blazt_breathe_in");
    public static final SoundEvent BLAZT_BREATHE_OUT = registerSoundEvent("blazt_breathe_out");
    public static final SoundEvent BLAZT_DEATH = registerSoundEvent("blazt_death");
    public static final SoundEvent BLAZT_HURT = registerSoundEvent("blazt_hurt");
    public static final SoundEvent BLAZT_SHOOTING = registerSoundEvent("blazt_shooting");
    public static final SoundEvent BLAZT_SOUL_CHARGE = registerSoundEvent("blazt_soul_charge");

    public static final Holder<SoundEvent> SOULLESS_WHISPER = registerForHolder( "soulless_whisper");
    public static final SoundEvent SOULLESS_AMBIENT = registerSoundEvent("soulless_ambient");
    public static final SoundEvent SOULLESS_DEATH = registerSoundEvent("soulless_death");
    public static final SoundEvent SOULLESS_HURT = registerSoundEvent("soulless_hurt");
    public static final SoundEvent SOULLESS_ANGRY = registerSoundEvent("soulless_angry");
    public static final SoundEvent SOULLESS_WAKE = registerSoundEvent("soulless_wake");

    public static final Holder<SoundEvent> HEEDLESS_WHISPER = registerForHolder("heedless_whisper");
    public static final SoundEvent HEEDLESS_DEATH = registerSoundEvent("heedless_death");
    public static final SoundEvent HEEDLESS_HURT = registerSoundEvent("heedless_hurt");
    public static final SoundEvent HEEDLESS_WAKE = registerSoundEvent("heedless_wake");

    public static final Holder<SoundEvent> RESTLESS_WHISPER = registerForHolder("restless_whisper");
    public static final SoundEvent RESTLESS_ANGER = registerSoundEvent("restless_anger");
    public static final SoundEvent RESTLESS_DEATH = registerSoundEvent("restless_death");
    public static final SoundEvent RESTLESS_HURT = registerSoundEvent("restless_hurt");

    public static final SoundEvent ETHEL_SPIN = registerSoundEvent("ethel_spin");
    public static final SoundEvent SOUL_DRIP = registerSoundEvent("soul_drip");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Soulscorch.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private static Holder.Reference<SoundEvent> registerForHolder(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, path);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Sounds for " + Soulscorch.MOD_ID);
    }
}