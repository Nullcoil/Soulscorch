// ModCriteriaTriggers.java
package net.nullcoil.soulscorch.advancement;


import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.PlayerTrigger;

public class ModCriteriaTriggers {
    public static final PlayerTrigger OIIA_OIIA =
            CriteriaTriggers.register("soulscorch:oiia_oiia", new PlayerTrigger());

    public static void register() {} // called in mod init to classload
}