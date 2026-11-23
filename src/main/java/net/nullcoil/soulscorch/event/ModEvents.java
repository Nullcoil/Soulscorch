package net.nullcoil.soulscorch.event;

import net.nullcoil.soulscorch.effect.custom.Cleansing;
import net.nullcoil.soulscorch.effect.custom.WitnessedEffect;
import net.nullcoil.soulscorch.entity.VillagerInspiration;

public class ModEvents {

    public static void register() {
        SoulbreakEventHandler.register();
        CatharsisTickHandler.register();
        AftermathTickHandler.register();
        WitnessedEffect.register();
        Cleansing.register();
        VillagerInspiration.register();

        // Death by Corruption, the health clamp, and respawning after a Corruption death
    }
}