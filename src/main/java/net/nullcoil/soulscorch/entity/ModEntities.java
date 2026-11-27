package net.nullcoil.soulscorch.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.nullcoil.nidalib.climbing.ClimbSettings;
import net.nullcoil.nidalib.climbing.WallClimbers;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.*;
import net.nullcoil.soulscorch.entity.ai.ghaze.Ghaze;
import net.nullcoil.soulscorch.entity.ai.heedless.Heedless;
import net.nullcoil.soulscorch.entity.ai.soulless.SoullessEntity;
import net.nullcoil.soulscorch.entity.projectile.SoulChargeProjectile;

public class ModEntities {

    public static final EntityType<Blazt> BLAZT;
    public static final EntityType<SoulChargeProjectile> SOUL_CHARGE_PROJECTILE;
    public static final EntityType<SoullessEntity> SOULLESS;
    public static final EntityType<Restless> RESTLESS;
    public static final EntityType<Hytodom> HYTODOM;
    public static final EntityType<SoulborneCat> SOULBORNE_CAT;
    public static final EntityType<SoulborneWolf> SOULBORNE_WOLF;
    public static final EntityType<Heedless> HEEDLESS;

    public static final EntityType<Ghaze> GHAZE;
    public static final EntityType<Wreth> WRETH;

    public static final EntityType<Resident> RESIDENT;

    /** Registers an entity type under soulscorch:{name}, using the same name for its key. */
    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Soulscorch.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    static {
        BLAZT = register("blazt",
                EntityType.Builder.of(Blazt::new, MobCategory.MONSTER)
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
                EntityType.Builder.of(Restless::new, MobCategory.MONSTER)
                        .sized(1.6f, 1.4f)
                        .clientTrackingRange(10));
        HYTODOM = register("hytodom",
                EntityType.Builder.of(Hytodom::new, MobCategory.CREATURE)
                        .sized(0.95f, 2.25f)
                        .clientTrackingRange(10));
        SOULBORNE_CAT = register("soulborne_cat",
                EntityType.Builder.of(SoulborneCat::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.7F));
        SOULBORNE_WOLF = register("soulborne_wolf",
                EntityType.Builder.of(SoulborneWolf::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.85F));
        HEEDLESS = register("heedless",
                EntityType.Builder.of(Heedless::new, MobCategory.MONSTER)
                        .sized(0.8f, 2f));

        GHAZE = register("ghaze",
                EntityType.Builder.of(Ghaze::new, MobCategory.MONSTER)
                        .sized(0.65f, 1.2f));
        WRETH = register("wreth",
                EntityType.Builder.of(Wreth::new, MobCategory.MONSTER)
                        .sized(0.6F, 0.3125F).eyeHeight(0.1F));

        RESIDENT = register("resident",
                EntityType.Builder.of(Resident::new, MobCategory.MONSTER)
                        .sized(2.37f, 3.95f));
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(BLAZT, Blazt.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULLESS, SoullessEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HEEDLESS, Heedless.createAttributes());
        FabricDefaultAttributeRegistry.register(RESTLESS, Restless.createAttributes());
        FabricDefaultAttributeRegistry.register(HYTODOM, Hytodom.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULBORNE_CAT, SoulborneCat.createAttributes());
        FabricDefaultAttributeRegistry.register(SOULBORNE_WOLF, SoulborneWolf.createAttributes());
        FabricDefaultAttributeRegistry.register(GHAZE, Ghaze.createAttributes());
        FabricDefaultAttributeRegistry.register(RESIDENT, Resident.createAttributes());
        FabricDefaultAttributeRegistry.register(WRETH, Wreth.createAttributes());
    }

    public static void registerClimbers() {
        WallClimbers.register(EntityTypes.CAVE_SPIDER, ClimbSettings.WALLS_AND_CEILINGS);
        WallClimbers.register(EntityTypes.SPIDER, ClimbSettings.WALLS_AND_CEILINGS);
        WallClimbers.register(EntityTypes.SILVERFISH, ClimbSettings.builder()
                .walls(true).ceilings(false).speedFactor(0.5f).build());
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Entities for " + Soulscorch.MOD_ID);
        registerAttributes();
        registerClimbers();
        ModBoats.register();
    }
}