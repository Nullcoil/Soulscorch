package net.nullcoil.soulscorch.mixin;

import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.nullcoil.soulscorch.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lava in a soul biome sets things on soul fire instead of regular fire.
 *
 * <p>Two parts:
 * <ol>
 *   <li>Whenever lava ignites this entity in a soul biome, remember the game tick it happened.</li>
 *   <li>Whenever Prometheus tries to set this entity's fire type to regular fire within a tick
 *       of that, turn it into soul fire before it's stored (and synced) instead.</li>
 * </ol>
 * Prometheus sets "regular fire" from several places while an entity is in lava, not only
 * from lavaIgnite itself, so catching the type change is the only way to beat all of them.
 * The short window means soul fire caught from lava sticks afterwards, while regular fire
 * caught anywhere else later still replaces it.
 *
 * <p>This deliberately runs on the client too. The client predicts your own movement, so it
 * runs lavaIgnite for the local player, and Prometheus then sets the client's copy of the
 * fire type to regular fire every tick. The server only syncs the type when it changes, so
 * after one blue tick the client's own "regular fire" stuck. Biomes and biome tags are
 * synced, so the check gives the same answer on both sides.
 *
 * <p>{@code setFireType} is added to Entity by Prometheus's own mixin, so this one must
 * apply after it (higher priority number).
 */
@Mixin(value = Entity.class, priority = 1500)
public abstract class SoulLavaIgniteMixin {
    @Unique
    private long soulscorch$soulLavaTick = Long.MIN_VALUE;

    @Inject(method = "lavaIgnite", at = @At("HEAD"))
    private void soulscorch$markSoulLava(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        // Runs on both sides on purpose: see the class comment
        if (self.level().getBiome(self.blockPosition()).is(ModTags.Biomes.SOUL_BIOMES)) {
            soulscorch$soulLavaTick = self.level().getGameTime();
        }
    }

    @SuppressWarnings("UnresolvedMixinReference") // added to Entity by Prometheus's mixin
    @ModifyVariable(method = "setFireType", at = @At("HEAD"), argsOnly = true, remap = false)
    private Identifier soulscorch$soulLavaFire(Identifier fireType) {
        if (!FireManager.DEFAULT_FIRE_TYPE.equals(fireType)) return fireType;

        Entity self = (Entity) (Object) this;
        long sinceSoulLava = self.level().getGameTime() - soulscorch$soulLavaTick;
        return sinceSoulLava >= 0 && sinceSoulLava <= 1 ? FireManager.SOUL_FIRE_TYPE : fireType;
    }
}