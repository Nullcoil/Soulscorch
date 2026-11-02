package net.nullcoil.soulscorch.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.nullcoil.soulscorch.Soulscorch;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> CORRUPTION_DEATH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Soulscorch.id("corruption")
    );
}