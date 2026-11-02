package net.nullcoil.soulscorch.enchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.nullcoil.soulscorch.Soulscorch;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> CATHARSIS;
    public static final ResourceKey<Enchantment> SOUL_FIRE_ASPECT;
    public static final ResourceKey<Enchantment> SOUL_FLAME;

    static {
        CATHARSIS = ResourceKey.create(Registries.ENCHANTMENT, Soulscorch.id("catharsis"));
        SOUL_FIRE_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace("soul_fire_aspect"));
        SOUL_FLAME = ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace("soul_flame"));
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Enchantment Keys for " + Soulscorch.MOD_ID);
    }
}