package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.nullcoil.nidalib.effect.CountdownEffect;
import net.nullcoil.soulscorch.entity.MobConversions;

/**
 * Thanatosis: the reverse of Asclepiosis. Harmful, so Asclepiosis clears it.
 *
 * <p><b>On other mobs</b> it's a countdown: when it runs out on its own, Villagers and Piglins
 * (Brutes too) turn into their zombified versions ({@link MobConversions#zombify}).
 *
 * <p><b>On a player</b> it's meant to spawn a Zombie Player carrying their items if they die
 * while it's active. That comes with the Zombie Player; until then it does nothing on players.
 */
public class ThanatosisEffect extends CountdownEffect {
    public ThanatosisEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    protected void onMobCountdownEnd(ServerLevel level, LivingEntity entity, int amplifier) {
        MobConversions.zombify(level, entity);
    }
}
