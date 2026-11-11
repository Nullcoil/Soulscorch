package net.nullcoil.soulscorch.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.nullcoil.nidalib.attribute.ModAttributes;
import net.nullcoil.soulscorch.enchantment.ModEnchantments;

public class CatharsisTickHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Run exactly once a second (every 20 server ticks) to save performance
            if (server.getTickCount() % 20 != 0) return;

            var enchantmentLookup = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var catharsisOpt = enchantmentLookup.get(ModEnchantments.CATHARSIS);

            // If the JSON hasn't loaded properly into the world, silently abort
            if (catharsisOpt.isEmpty()) return;

            Holder<Enchantment> catharsisHolder = catharsisOpt.get();

            // Calculate total server uptime in seconds
            long currentSecond = server.getTickCount() / 20;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestplate.isEmpty()) continue;

                int level = EnchantmentHelper.getItemEnchantmentLevel(catharsisHolder, chestplate);

                if (level > 0) {
                    // Level 1 = 60s, Level 2 = 30s
                    int secondsInterval = 60 / level;

                    if (currentSecond % secondsInterval == 0) {
                        AttributeInstance corruptionAttr = player.getAttribute(ModAttributes.CORRUPTION);

                        if (corruptionAttr != null && corruptionAttr.getValue() > 0.0D) {
                            // Alleviate exactly 1 Corruption
                            double newCorruption = Math.max(0.0D, corruptionAttr.getValue() - 1.0D);
                            corruptionAttr.setBaseValue(newCorruption);

                            // Damage the Chestplate
                            chestplate.hurtAndBreak(1, player, EquipmentSlot.CHEST);
                        }
                    }
                }
            }
        });
    }
}