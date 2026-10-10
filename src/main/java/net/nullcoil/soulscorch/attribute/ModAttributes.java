package net.nullcoil.soulscorch.attribute;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.nullcoil.soulscorch.Soulscorch;

public class ModAttributes {
    public static final Holder<Attribute> CORRUPTION = Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            Soulscorch.id("corruption"),
            new RangedAttribute("attribute.name.soulscorch.corruption", 0.0D, 0.0D, 1024.0D).setSyncable(true)
    );

    public static void register() {
        Soulscorch.LOGGER.info("Registering Attributes for " + Soulscorch.MOD_ID);
    }
}