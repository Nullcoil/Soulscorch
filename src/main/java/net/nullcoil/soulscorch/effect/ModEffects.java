package net.nullcoil.soulscorch.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.effect.custom.CompanionEffect;
import net.nullcoil.soulscorch.effect.custom.SoulRenderEffect;
import net.nullcoil.soulscorch.effect.custom.SoulscorchEffect;
import net.nullcoil.soulscorch.effect.custom.VolatileEffect;

public class ModEffects {
    public static final Holder<MobEffect> CAT_BUFF = registerEffect("cat_buff", new CompanionEffect(MobEffectCategory.BENEFICIAL, 0x000000));
    public static final Holder<MobEffect> DOG_BUFF = registerEffect("dog_buff", new CompanionEffect(MobEffectCategory.BENEFICIAL, 0x000000));
    public static final Holder<MobEffect> SOULSCORCH = registerEffect("soulscorch", new SoulscorchEffect(MobEffectCategory.HARMFUL, 0x00ffff));
    public static final Holder<MobEffect> SOUL_RENDER = registerEffect("soul_render", new SoulRenderEffect(MobEffectCategory.BENEFICIAL, 0x00ff88));
    public static final Holder<MobEffect> VOLATILE = registerEffect("volatile", new VolatileEffect(MobEffectCategory.HARMFUL, 0x4287f5));

    private static Holder<MobEffect> registerEffect(String string, MobEffect mobEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Soulscorch.id(string), mobEffect);
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Effects for " + Soulscorch.MOD_ID);
    }
}
