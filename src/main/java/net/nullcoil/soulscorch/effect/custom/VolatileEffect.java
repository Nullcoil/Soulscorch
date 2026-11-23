package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.nullcoil.nidalib.effect.CountdownEffect;
import net.nullcoil.soulscorch.event.AftermathTickHandler;

/** Explodes when it runs out. */
public class VolatileEffect extends CountdownEffect {
    public VolatileEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    protected void onPlayerCountdownEnd(ServerLevel level, ServerPlayer player, int amplifier) {
        // Start watching before the blast, so a totem popped by the blast itself counts
        AftermathTickHandler.watch(player);
    }

    @Override
    protected void onCountdownEnd(ServerLevel level, LivingEntity entity, int amplifier) {
        float radius = 2f + amplifier + 1;
        level.explode(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                radius, Level.ExplosionInteraction.MOB
        );
    }
}
