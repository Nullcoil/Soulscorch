package net.nullcoil.soulscorch.mixin.piglin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.entity.ai.heedless.HeedlessEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrute.class)
public class PiglinBruteSpawnMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void replaceWithHeedless(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        PiglinBrute brute = (PiglinBrute) (Object) this;
        if (spawnReason != EntitySpawnReason.STRUCTURE
                && spawnReason != EntitySpawnReason.NATURAL
                && spawnReason != EntitySpawnReason.CHUNK_GENERATION) {
            return;
        }
        if (!level.getBiome(brute.blockPosition()).is(Biomes.SOUL_SAND_VALLEY)) return;

        HeedlessEntity heedless = ModEntities.HEEDLESS.create(level.getLevel(), spawnReason);
        if (heedless == null) return;

        heedless.setPos(brute.getX(), brute.getY(), brute.getZ());
        heedless.setXRot(brute.getXRot());
        heedless.setYRot(brute.getYRot());
        heedless.finalizeSpawn(level, difficulty, spawnReason, null);
        level.addFreshEntity(heedless);
        brute.discard();
    }
}