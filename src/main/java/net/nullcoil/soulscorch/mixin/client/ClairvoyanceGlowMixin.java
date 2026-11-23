package net.nullcoil.soulscorch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.effect.custom.ClairvoyanceEffect;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Clairvoyance: mobs within range get the glowing outline, through walls. Only on the
 * drinker's own client, so nobody else sees them glow.
 */
@Mixin(Minecraft.class)
public abstract class ClairvoyanceGlowMixin {
    @Shadow public @Nullable LocalPlayer player;

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean soulscorch$clairvoyance(boolean glowing, @Local(argsOnly = true) Entity entity) {
        if (glowing || !(entity instanceof Mob) || this.player == null) return glowing;

        MobEffectInstance clairvoyance = this.player.getEffect(ModEffects.CLAIRVOYANCE);
        if (clairvoyance == null) return false;

        double radius = ClairvoyanceEffect.radius(clairvoyance.getAmplifier());
        return entity.distanceToSqr(this.player) <= radius * radius;
    }
}
