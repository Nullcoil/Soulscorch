package net.nullcoil.soulscorch.enchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.nullcoil.soulscorch.Soulscorch;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> CATHARSIS = ResourceKey.create(
            Registries.ENCHANTMENT,
            Soulscorch.id("catharsis")
    );

    public static void register() {
        Soulscorch.LOGGER.info("Registering Enchantment Keys for " + Soulscorch.MOD_ID);
    }
}