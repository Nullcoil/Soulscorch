package net.nullcoil.soulscorch.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.soulscorch.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void soulscorch$applySoulscorch(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise,
            CallbackInfo ci
    ) {
        // Only care about soul fire
        if (!(state.getBlock() instanceof SoulFireBlock) && !state.is(Blocks.SOUL_FIRE)) {
            return;
        }

        if (level.isClientSide()) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (living.is(EntityTypeTags.UNDEAD)) return;

        // Apply (or refresh) the effect — same values you had in ModFire
        living.addEffect(new MobEffectInstance(
                ModEffects.SOULSCORCH,
                600,   // 30 seconds
                0,
                false, // ambient
                false, // visible particles?
                true   // show icon
        ));
    }
}