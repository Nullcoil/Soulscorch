package net.nullcoil.soulscorch.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.attribute.ModAttributes;
import net.nullcoil.soulscorch.entity.ai.*;
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

    private static ResourceKey<EntityType<?>> key(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Soulscorch.id(path));
    }

    static {
        BLAZT = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("blazt"),
                EntityType.Builder.of(BlaztEntity::new, MobCategory.MONSTER)
                        .sized(3.0f, 3.0f)
                        .clientTrackingRange(10)
                        .build(key("blazt"))
        );
        SOUL_CHARGE_PROJECTILE = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("soul_charge_projectile"),
                EntityType.Builder.<SoulChargeProjectile>of(SoulChargeProjectile::new, MobCategory.MISC)
                        .sized(0.3125F, 0.3125F)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .build(key("soul_charge"))
        );
        SOULLESS = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("soulless"),
                EntityType.Builder.of(SoullessEntity::new, MobCategory.MONSTER)
                        .sized(0.6f, 1.95f)
                        .clientTrackingRange(10)
                        .build(key("soulless"))
        );
        RESTLESS = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("restless"),
                EntityType.Builder.of(RestlessEntity::new, MobCategory.MONSTER)
                        .sized(1.6f, 1.4f)
                        .clientTrackingRange(10)
                        .build(key("restless"))
        );
        HYTODOM = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("hytodom"),
                EntityType.Builder.of(HytodomEntity::new, MobCategory.CREATURE)
                        .sized(0.95f, 2.25f)
                        .clientTrackingRange(10)
                        .build(key("hytodom"))
        );
        SOULBORNE_CAT = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("soulborne_cat"),
                EntityType.Builder.of(SoulborneCatEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.7F)
                        .build(key("soulborne_cat"))
        );
        SOULBORNE_WOLF = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("soulborne_wolf"),
                EntityType.Builder.of(SoulborneWolfEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.85F)
                        .build(key("soulborne_wolf"))
        );
        HEEDLESS = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Soulscorch.id("heedless"),
                EntityType.Builder.of(HeedlessEntity::new, MobCategory.MONSTER)
                        .sized(0.8f, 2f)
                        .build(key("heedless"))
        );
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(BLAZT, BlaztEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULLESS, SoullessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HEEDLESS, HeedlessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RESTLESS, RestlessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HYTODOM, HytodomEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULBORNE_CAT, SoulborneCatEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULBORNE_WOLF, SoulborneWolfEntity.createAttributes());
        FabricDefaultAttributeRegistry.MODIFY.register(context -> context.modifyAll((type, builder) ->
                builder.add(ModAttributes.CORRUPTION)
        ));
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Entities for " + Soulscorch.MOD_ID);
        registerAttributes();
        ModBoats.register();
    }
}