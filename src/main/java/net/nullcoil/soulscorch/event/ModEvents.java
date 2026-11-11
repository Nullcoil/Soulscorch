package net.nullcoil.soulscorch.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nullcoil.nidalib.attribute.ModAttributes;
import net.nullcoil.soulscorch.damage.ModDamageTypes;

public class ModEvents {

    public static void register() {
        SoulbreakEventHandler.register();
        CatharsisTickHandler.register();

        // 1. The Death Check: Run every tick to see if Corruption >= Max HP
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                if (!player.isAlive()) continue;

                AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
                AttributeInstance corruptionAttr = player.getAttribute(ModAttributes.CORRUPTION);

                if (maxHealthAttr != null && corruptionAttr != null) {
                    if (corruptionAttr.getValue() >= maxHealthAttr.getValue()) {

                        // Grab our custom damage type!
                        DamageSource source = player.getLastHurtByMob() != null
                                ? player.damageSources().source(ModDamageTypes.CORRUPTION_DEATH, player.getLastHurtByMob())
                                : player.damageSources().source(ModDamageTypes.CORRUPTION_DEATH);

                        // Deal infinite damage to bypass totems and armor
                        player.hurt(source, Float.MAX_VALUE);
                    }
                }
            }
        });

        // 2. The Respawn Punishment: Set Corruption to (Max HP - 1) ONLY on Corruption Death
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