package net.nullcoil.soulscorch.fire;


import net.nullcoil.soulscorch.Soulscorch;

public final class ModFire {
    /*
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


     */
    public static void register() {
        Soulscorch.LOGGER.info("Skipping registering ModFire for " + Soulscorch.MOD_ID);
    }


}
