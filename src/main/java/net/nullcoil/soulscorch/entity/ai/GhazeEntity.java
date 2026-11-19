package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GhazeEntity extends Monster implements SoulAlarm.Listener {
    public GhazeEntity(EntityType<? extends GhazeEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    /** A Soulless nearby turned hostile: if it turned on a player, the Ghaze takes notice of them too. */
    @Override
    public void onSoulAlarm(LivingEntity source, LivingEntity target) {
        if (!(target instanceof Player)) return;
        if (this.getTarget() != null && this.getTarget().isAlive()) return; // already watching someone
        if (!this.canAttack(target)) return; // creative, spectator, peaceful...
        this.setTarget(target);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}