package net.nullcoil.soulscorch.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.entity.SeepingHangingSignBlockEntity;
import net.nullcoil.soulscorch.block.entity.SoulBrewingStandBlockEntity;

public class ModBlockEntities {

    public static final BlockEntityType<SoulBrewingStandBlockEntity> SOUL_BREWING_STAND =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Soulscorch.id("soul_brewing_stand"),
                    FabricBlockEntityTypeBuilder.create(SoulBrewingStandBlockEntity::new, ModBlocks.SOUL_BREWING_STAND).build()
            );

    public static final BlockEntityType<SignBlockEntity> SEEPING_SIGN =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Soulscorch.id("seeping_sign"),
                    FabricBlockEntityTypeBuilder.create(
                            (pos, state) -> new SignBlockEntity(ModBlockEntities.SEEPING_SIGN, pos, state),
                            SeepingBlocks.SEEPING_SIGN, SeepingBlocks.SEEPING_WALL_SIGN
                    ).build()
            );

    public static final BlockEntityType<SeepingHangingSignBlockEntity> SEEPING_HANGING_SIGN =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Soulscorch.id("seeping_hanging_sign"),
                    FabricBlockEntityTypeBuilder.create(
                            SeepingHangingSignBlockEntity::new,
                            SeepingBlocks.SEEPING_HANGING_SIGN, SeepingBlocks.SEEPING_WALL_HANGING_SIGN
                    ).build()
            );

    public static final BlockEntityType<SignBlockEntity> SALLOW_SIGN =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Soulscorch.id("sallow_sign"),
                    FabricBlockEntityTypeBuilder.create(
                            (pos, state) -> new SignBlockEntity(ModBlockEntities.SALLOW_SIGN, pos, state),
                            SallowBlocks.SALLOW_SIGN, SallowBlocks.SALLOW_WALL_SIGN
                    ).build()
            );

    public static final BlockEntityType<HangingSignBlockEntity> SALLOW_WALL_HANGING_SIGN =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Soulscorch.id("seeping_wall_hanging_sign"),
                    FabricBlockEntityTypeBuilder.create(
                            HangingSignBlockEntity::new,
                            SallowBlocks.SALLOW_HANGING_SIGN, SallowBlocks.SALLOW_WALL_HANGING_SIGN
                    ).build()
            );

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Block Entities for " + Soulscorch.MOD_ID);
    }
}