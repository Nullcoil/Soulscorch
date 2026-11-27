package net.nullcoil.soulscorch.mixin.wolf;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.entity.ai.SoulborneWolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfEntityMixin extends TamableAnimal {
    protected WolfEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;",
    at = @At("HEAD"), cancellable = true )
    private void soulscorch$replaceChild(ServerLevel level, AgeableMob other,  CallbackInfoReturnable<Wolf> cir) {
        if(!(other instanceof Wolf wolf2)) return;

        if(this.isTame() && wolf2.isTame() && level.getBiome(this.getOnPos()).is(Biomes.SOUL_SAND_VALLEY)) {
            SoulborneWolf soulWolf = ModEntities.SOULBORNE_WOLF.create(level, EntitySpawnReason.BREEDING);
            if(soulWolf != null) {
                soulWolf.setTame(true, true);
                soulWolf.setOwner(this.getOwner());
            }

            cir.setReturnValue(soulWolf);
            cir.cancel();
        }
    }
}
