package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.mixin.wolf.WolfInvoker;
import net.nullcoil.soulscorch.util.ModTags;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SoulborneWolfEntity extends Wolf {
    public SoulborneWolfEntity(EntityType<? extends Wolf> entityType, Level level) {
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
            player.addEffect(new MobEffectInstance(ModEffects.DOG_BUFF,
                    20,
                    0,
                    false,
                    false,
                    true));
            if(player.hasEffect(ModEffects.SOULSCORCH)) player.removeEffect(ModEffects.SOULSCORCH);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) {
        boolean result = super.doHurtTarget(level, entity);

        if (result && entity instanceof LivingEntity living) {
            // Apply the same effect the old soul-fire behavior used
            living.addEffect(new MobEffectInstance(
                    ModEffects.SOULSCORCH,
                    100,          // 5 seconds (5 * 20) — matches the old "5" you were passing
                    0,
                    false,
                    false,
                    true
            ));

            living.igniteForSeconds(5.0F);
        }

        return result;
    }

    @Override
    @Nullable
    public Wolf getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Wolf wolf = (Wolf) ModEntities.SOULBORNE_WOLF.create(serverLevel, EntitySpawnReason.BREEDING);
        if(wolf != null && ageableMob instanceof Wolf wolf2) {
            if (this.random.nextBoolean()) {
                ((WolfInvoker) wolf).invokeSetVariant(((WolfInvoker)this).invokeGetVariant());
            } else {
                ((WolfInvoker) wolf).invokeSetVariant(((WolfInvoker)wolf2).invokeGetVariant());
            }

            if (this.isTame()) {
                wolf.setOwner(this.getOwner());
                wolf.setTame(true, true);
                DyeColor dyeColor = this.getCollarColor();
                DyeColor dyeColor2 = wolf2.getCollarColor();
                ((WolfInvoker) wolf).invokeSetCollarColor(DyeColor.getMixedColor(serverLevel, dyeColor, dyeColor2));
            }
        }

        return wolf;
    }

    @Override
    public @NonNull Identifier getTexture() {
        Identifier TAMED = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulwolf/king.png");
        Identifier ANGRY = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulwolf/soulwolf_angry.png");
        Identifier WILD  = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulwolf/soulwolf.png");

        Identifier BABY = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulwolf/soulwolf_baby.png");
        Identifier BABY_ANGRY = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulwolf/soulwolf_angry_baby.png");
        Identifier BABY_TAMED = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulwolf/soulwolf_tame_baby.png");
        if (this.isBaby()) {
            if (this.isTame()) {
                return BABY_TAMED;
            } else {
                return this.isAngry() ? BABY_ANGRY : BABY;
            }
        } else {
            if (this.isTame()) {
                return TAMED;
            } else {
                return this.isAngry() ? ANGRY : WILD;
            }
        }
    }
}
