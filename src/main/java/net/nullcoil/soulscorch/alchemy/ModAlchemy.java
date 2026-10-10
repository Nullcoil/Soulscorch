package net.nullcoil.soulscorch.alchemy;


import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.effect.ModEffects;

public class ModAlchemy {
    public static final Holder<Potion> SOUL_RENDERING;
    public static final Holder<Potion> SOUL_RENDERING_STRONG;

    private static Holder<Potion> register(String path, Potion potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, Soulscorch.id(path), potion);
    }

    static {
        SOUL_RENDERING = register("soul_rendering",
                new Potion(
                        "soul_rendering",
                        new MobEffectInstance[]{
                                new MobEffectInstance(ModEffects.SOUL_RENDER, 1, 0)
                        }));
        SOUL_RENDERING_STRONG = register("soul_rendering_strong",
                new Potion(
                        "soul_rendering_strong",
                        new MobEffectInstance[]{
                                new MobEffectInstance(ModEffects.SOUL_RENDER, 1, 1)
                        }));
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Alchemy for " + Soulscorch.MOD_ID);
    }
}
