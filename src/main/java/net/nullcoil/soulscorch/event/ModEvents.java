package net.nullcoil.soulscorch.event;

import net.nullcoil.soulscorch.effect.custom.WitnessedEffect;

public class ModEvents {

    public static void register() {
        SoulbreakEventHandler.register();
        CatharsisTickHandler.register();
        AftermathTickHandler.register();
        WitnessedEffect.register();

        // Death by Corruption, the health clamp, and respawning after a Corruption death
    }
}