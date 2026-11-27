package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.nullcoil.soulscorch.entity.ai.jellyfish.AbstractJellyfish;

public class Hytodom extends AbstractJellyfish {
    public Hytodom(EntityType<? extends AbstractJellyfish> type, Level level) {
        super(type, level);
    }
}