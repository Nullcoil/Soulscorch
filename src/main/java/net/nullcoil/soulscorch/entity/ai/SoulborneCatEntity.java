package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatSoundVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.mixin.cat.CatInvoker;
import net.nullcoil.soulscorch.util.ModTags;
import org.jspecify.annotations.Nullable;

public class SoulborneCatEntity extends Cat {
    public SoulborneCatEntity(EntityType<? extends Cat> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockPos pos = this.getBlockPosBelowThatAffectsMyMovement();
        BlockState blockState = this.level().getBlockState(pos);
        if (blockState.is(ModTags.Blocks.SOULBASED_BLOCKS)) { return 1.2F; }
        return super.getBlockSpeedFactor();
    }

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide() &&
        this.isTame() &&
        !this.isInSittingPose() &&
        this.getOwner() instanceof Player player) {
            player.addEffect(new MobEffectInstance(ModEffects.CAT_BUFF,
                    20,
                    0,
                    false,
                    false,
                    true));
            if(player.hasEffect(ModEffects.SOULSCORCH)) player.removeEffect(ModEffects.SOULSCORCH);
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        if(this.level().getBiome(this.getOnPos()).is(Biomes.SOUL_SAND_VALLEY) && this.isTame()) {
            return this.isBaby() ? ((CatSoundVariant) this.getSoundVariant().value()).babySounds().purreowSound().value() : ((CatSoundVariant) this.getSoundVariant().value()).adultSounds().purrSound().value();
        }

        return super.getAmbientSound();
    }

    @Override
    @Nullable
    public Cat getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Cat cat = (Cat) ModEntities.SOULBORNE_CAT.create(serverLevel, EntitySpawnReason.BREEDING);
        if(cat != null && ageableMob instanceof Cat cat2) {
            if (this.random.nextBoolean()) {
                ((CatInvoker) cat).invokeSetVariant(this.getVariant());
            } else {
                ((CatInvoker) cat).invokeSetVariant(cat2.getVariant());
            }

            if (this.isTame()) {
                cat.setOwner(this.getOwner());
                cat.setTame(true, true);
                DyeColor dyeColor = this.getCollarColor();
                DyeColor dyeColor2 = cat2.getCollarColor();
                ((CatInvoker) cat).invokeSetCollarColor(DyeColor.getMixedColor(serverLevel, dyeColor, dyeColor2));
            }
        }

        return cat;
    }
}
