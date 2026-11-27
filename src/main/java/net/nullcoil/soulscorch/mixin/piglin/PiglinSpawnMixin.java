package net.nullcoil.soulscorch.mixin.piglin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.entity.ai.soulless.SoullessEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Piglin.class)
public class PiglinSpawnMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void replaceWithSoulless(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Piglin piglin = (Piglin) (Object) this;

        // 1. Only intercept natural or structure spawns (ignores Spawn Eggs and /summon)
        if (spawnReason == EntitySpawnReason.STRUCTURE || spawnReason == EntitySpawnReason.NATURAL || spawnReason == EntitySpawnReason.CHUNK_GENERATION) {

            // 2. Check if the spawn is happening inside a Soul Sand Valley
            if (level.getBiome(piglin.blockPosition()).is(Biomes.SOUL_SAND_VALLEY)) {

                // 3. Create a Soulless to take its place (Passing the Level and SpawnReason!)
                SoullessEntity soulless = ModEntities.SOULLESS.create(level.getLevel(), spawnReason);

                if (soulless != null) {
                    // Copy exact position and rotation
                    soulless.setPos(piglin.getX(), piglin.getY(), piglin.getZ());
                    soulless.setXRot(piglin.getXRot());
                    soulless.setYRot(piglin.getYRot());

                    // Initialize its gear and health
                    soulless.finalizeSpawn(level, difficulty, spawnReason, null);

                    // Add it to the world and immediately delete the original Piglin
                    level.addFreshEntity(soulless);
                    piglin.discard();
                }
            }
        }
    }
}