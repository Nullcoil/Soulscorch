package net.nullcoil.soulscorch.fire;


import it.crystalnest.prometheus.api.Fire;
import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.item.ModItems;
import org.jetbrains.annotations.ApiStatus;

public final class ModFire {
    @ApiStatus.Internal
    public static final Identifier SOUL_FIRE_TYPE = FireManager.SOUL_FIRE_TYPE;

    static {
        FireManager.registerFire(
                FireManager.fireBuilder(SOUL_FIRE_TYPE)
                        .setDefaultComponents()
                        .setComponent(Fire.Component.FLAME_PARTICLE, BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.SOUL_FIRE_FLAME))
                        .setComponent(Fire.Component.FIRE_CHARGE_ITEM, BuiltInRegistries.ITEM.getKey(ModItems.SOUL_CHARGE))
                        .setLight(10)
                        .setDamage(2)
                        .setBehavior(entity -> {
                            if (!entity.is(EntityTypeTags.UNDEAD) &&
                            entity instanceof LivingEntity livingEntity) {
                                livingEntity.addEffect(new MobEffectInstance(
                                        ModEffects.SOULSCORCH,
                                        600,
                                        0,
                                        false,
                                        false,
                                        true
                                ));
                            }
                        })
                        .build()
        );
    }

    private ModFire() {}

    public static void register() {
        Soulscorch.LOGGER.info("Cannot register Soul Fire Registry for " + Soulscorch.MOD_ID);
        Soulscorch.LOGGER.info("Prometheus does not exist for 26.2");
    }


}
