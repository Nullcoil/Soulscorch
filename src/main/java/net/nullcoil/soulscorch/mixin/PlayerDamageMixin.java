package net.nullcoil.soulscorch.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.nullcoil.soulscorch.entity.ai.Restless;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Player.class)
public class PlayerDamageMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void soulscorch$wakeRestlessOnDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        // 1. cir.getReturnValue() is only true if the player actually lost health.
        if (cir.getReturnValue()) {
            Player player = (Player) (Object) this;

            // 2. Draw a 32-block search box around the bleeding player
            AABB searchBox = player.getBoundingBox().inflate(32.0);

            // 3. Find all Restless entities in that box that are currently asleep
            List<Restless> sleepingRestless = level.getEntitiesOfClass(
                    Restless.class,
                    searchBox,
                    restless -> !restless.getAwakened()
            );

            // 4. If there is at least one sleeping Restless nearby...
            if (!sleepingRestless.isEmpty()) {
                // Pick exactly one at random
                Restless chosenOne = sleepingRestless.get(player.getRandom().nextInt(sleepingRestless.size()));

                // Wake it up and immediately lock it onto the injured player!
                chosenOne.setAwakened(true);
                chosenOne.setTarget(player);
            }
        }
    }
}