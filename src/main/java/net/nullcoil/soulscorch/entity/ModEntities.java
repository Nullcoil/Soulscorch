package net.nullcoil.soulscorch.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.spider.Spider;
import net.nullcoil.nidalib.climbing.WallClimbing;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.*;
import net.nullcoil.soulscorch.entity.ai.ghaze.GhazeEntity;
import net.nullcoil.soulscorch.entity.ai.heedless.HeedlessEntity;
import net.nullcoil.soulscorch.entity.projectile.SoulChargeProjectile;

public class ModEntities {

    public static final EntityType<BlaztEntity> BLAZT;
    public static final EntityType<SoulChargeProjectile> SOUL_CHARGE_PROJECTILE;
    public static final EntityType<SoullessEntity> SOULLESS;
    public static final EntityType<RestlessEntity> RESTLESS;
    public static final EntityType<HytodomEntity> HYTODOM;
    public static final EntityType<SoulborneCatEntity> SOULBORNE_CAT;
    public static final EntityType<SoulborneWolfEntity> SOULBORNE_WOLF;
    public static final EntityType<HeedlessEntity> HEEDLESS;

    public static final EntityType<GhazeEntity> GHAZE;

    public static final EntityType<ResidentEntity> RESIDENT;

    /** Registers an entity type under soulscorch:{name}, using the same name for its key. */
    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Soulscorch.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    static {
        BLAZT = register("blazt",
                EntityType.Builder.of(BlaztEntity::new, MobCategory.MONSTER)
                        .sized(3.0f, 3.0f)
                        .clientTrackingRange(10));
        SOUL_CHARGE_PROJECTILE = register("soul_charge_projectile",
                EntityType.Builder.<SoulChargeProjectile>of(SoulChargeProjectile::new, MobCategory.MISC)
                        .sized(0.3125F, 0.3125F)
                        .clientTrackingRange(4)
                        .updateInterval(10));
        SOULLESS = register("soulless",
                EntityType.Builder.of(SoullessEntity::new, MobCategory.MONSTER)
                        .sized(0.6f, 1.95f)
                        .clientTrackingRange(10));
        RESTLESS = register("restless",
                EntityType.Builder.of(RestlessEntity::new, MobCategory.MONSTER)
                        .sized(1.6f, 1.4f)
                        .clientTrackingRange(10));
        HYTODOM = register("hytodom",
                EntityType.Builder.of(HytodomEntity::new, MobCategory.CREATURE)
                        .sized(0.95f, 2.25f)
                        .clientTrackingRange(10));
        SOULBORNE_CAT = register("soulborne_cat",
                EntityType.Builder.of(SoulborneCatEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.7F));
        SOULBORNE_WOLF = register("soulborne_wolf",
                EntityType.Builder.of(SoulborneWolfEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.85F));
        HEEDLESS = register("heedless",
                EntityType.Builder.of(HeedlessEntity::new, MobCategory.MONSTER)
                        .sized(0.8f, 2f));

        GHAZE = register("ghaze",
                EntityType.Builder.of(GhazeEntity::new, MobCategory.MONSTER)
                        .sized(0.65f, 1.2f));

        RESIDENT = register("resident",
                EntityType.Builder.of(ResidentEntity::new, MobCategory.MONSTER)
                        .sized(2.37f, 3.95f));
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(BLAZT, BlaztEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULLESS, SoullessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HEEDLESS, HeedlessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RESTLESS, RestlessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HYTODOM, HytodomEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULBORNE_CAT, SoulborneCatEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULBORNE_WOLF, SoulborneWolfEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GHAZE, GhazeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RESIDENT, ResidentEntity.createAttributes());
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Entities for " + Soulscorch.MOD_ID);
        registerAttributes();
        ModBoats.register();
    }
}