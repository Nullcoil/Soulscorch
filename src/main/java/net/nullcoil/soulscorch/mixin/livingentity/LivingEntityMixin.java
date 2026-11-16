package net.nullcoil.soulscorch.mixin.livingentity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.nullcoil.soulscorch.effect.custom.VolatileEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyReturnValue(method = "checkTotemDeathProtection", at = @At("RETURN"))
    private boolean soulscorch$noteTotem(boolean saved) {
        if (saved) VolatileEffect.noteTotem((LivingEntity)(Object)this);
        return saved;
    }
}
