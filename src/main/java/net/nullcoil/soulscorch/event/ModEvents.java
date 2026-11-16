package net.nullcoil.soulscorch.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nullcoil.nidalib.attribute.ModAttributes;

public class ModEvents {

    public static void register() {
        SoulbreakEventHandler.register();
        CatharsisTickHandler.register();
        AftermathTickHandler.register();

        // Death by Corruption (and the health clamp) is handled by Nidalib's Corruption API.

        // The Respawn Punishment: Set Corruption to (Max HP - 1) ONLY on Corruption Death
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                AttributeInstance oldMaxHealth = oldPlayer.getAttribute(Attributes.MAX_HEALTH);
                AttributeInstance oldCorruption = oldPlayer.getAttribute(ModAttributes.CORRUPTION);

                AttributeInstance newMaxHealth = newPlayer.getAttribute(Attributes.MAX_HEALTH);
                AttributeInstance newCorruption = newPlayer.getAttribute(ModAttributes.CORRUPTION);

                if (oldMaxHealth != null && oldCorruption != null && newMaxHealth != null && newCorruption != null) {

                    // Check if the old body specifically died to the corruption limit
                    if (oldCorruption.getValue() >= oldMaxHealth.getValue()) {
                        // Punish them
                        double punishingCorruption = newMaxHealth.getValue() - 1.0;
                        newCorruption.setBaseValue(punishingCorruption);
                        newPlayer.setHealth(1.0F);
                    }
                }
            }
        });
    }
}