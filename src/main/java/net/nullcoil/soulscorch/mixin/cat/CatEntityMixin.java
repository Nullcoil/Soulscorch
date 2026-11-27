package net.nullcoil.soulscorch.mixin.cat;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.nullcoil.soulscorch.advancement.ModCriteriaTriggers;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.entity.ai.SoulborneCat;
import net.nullcoil.soulscorch.sound.ModSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cat.class)
public abstract class CatEntityMixin extends TamableAnimal {
    @Shadow protected abstract void reassessTameGoals();

    protected CatEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/feline/Cat;",
    at = @At("HEAD"), cancellable = true )
    private void soulscorch$replaceChild(ServerLevel level, AgeableMob other, CallbackInfoReturnable<Cat> cir) {
        if (!(other instanceof Cat cat2)) return;

        if (this.isTame() && cat2.isTame() && level.getBiome(this.getOnPos()).is(Biomes.SOUL_SAND_VALLEY)) {
            SoulborneCat soulCat = ModEntities.SOULBORNE_CAT.create(level, EntitySpawnReason.BREEDING);
            if (soulCat != null) {
                soulCat.setTame(true, true);
                soulCat.setOwner(this.getOwner());

                if (this.getOwner() instanceof ServerPlayer owner) {
                    ModCriteriaTriggers.OIIA_OIIA.trigger(owner);
                    owner.connection.send(new ClientboundSoundPacket(
                            Holder.direct(ModSounds.ETHEL_SPIN),
                            SoundSource.PLAYERS,
                            owner.getX(), owner.getY(), owner.getZ(),
                            1.0f,  // volume
                            1.0f,  // pitch
                            level.getRandom().nextLong()
                    ));
                }
            }

            cir.setReturnValue(soulCat);
            cir.cancel();
        }
    }
}
