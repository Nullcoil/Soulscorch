package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.nullcoil.soulscorch.entity.VillagerInspiration;

/**
 * Inspired, instant: makes a villager Inspired ({@link VillagerInspiration}), so it gives
 * every player a discount. Asclepiosis gives it to a Zombie Villager it cures. Does nothing
 * to anything that isn't a villager. Only ever given directly (no potion of it), so it runs
 * from its one tick rather than from a splash.
 */
public class InspiredEffect extends InstantaneousMobEffect {
    public InspiredEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        inspire(level, entity);
        return true;
    }

    private static void inspire(ServerLevel level, LivingEntity entity) {
        if (!(entity instanceof Villager villager) || VillagerInspiration.isInspired(villager)) return;
        VillagerInspiration.inspire(villager);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY(1.0), villager.getZ(),
                12, 0.4, 0.3, 0.4, 0.0);
    }
}
