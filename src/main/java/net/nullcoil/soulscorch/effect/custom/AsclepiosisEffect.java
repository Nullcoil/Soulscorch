package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.nullcoil.nidalib.effect.CountdownEffect;
import net.nullcoil.soulscorch.entity.MobConversions;

/**
 * Asclepiosis: the cure.
 *
 * <p><b>On a player</b> it's instant: every harmful effect is removed, every beneficial one
 * kept, and Asclepiosis itself goes away at once.
 *
 * <p><b>On other mobs</b> it's a countdown. The whole time, it keeps clearing harmful effects
 * (so a mob under it can't have Thanatosis). When it runs out on its own, the mob is cured one
 * step: Heedless → Soulless → Zombified Piglin → Piglin, Zombie Villager → Villager,
 * Restless → Zoglin → Hoglin ({@link MobConversions#cure}).
 */
public class AsclepiosisEffect extends CountdownEffect {
    public AsclepiosisEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        Cleansing.queue(entity);
        // On a player it's done after this one tick: returning false removes it (with time
        // left, so the countdown never fires)
        return !(entity instanceof Player);
    }

    @Override
    protected void onMobCountdownEnd(ServerLevel level, LivingEntity entity, int amplifier) {
        MobConversions.cure(level, entity);
    }
}
