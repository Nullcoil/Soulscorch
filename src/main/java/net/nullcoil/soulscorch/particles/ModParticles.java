package net.nullcoil.soulscorch.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.nullcoil.soulscorch.Soulscorch;

public class ModParticles {
    public static SimpleParticleType SEEPING_DRIP_HANG = register("seeping_drip_hang");
    public static SimpleParticleType SEEPING_DRIP_FALL = register("seeping_drip_fall");
    public static SimpleParticleType SEEPING_DRIP_LAND = register("seeping_drip_land");

    public static final SimpleParticleType SEEPING_SALLOW_LEAVES = register("seeping_sallow_leaves");

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Soulscorch.id(name),
                FabricParticleTypes.simple());
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Particles for " + Soulscorch.MOD_ID);
    }
}