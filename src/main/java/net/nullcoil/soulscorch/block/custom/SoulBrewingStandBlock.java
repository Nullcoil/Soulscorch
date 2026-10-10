package net.nullcoil.soulscorch.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import net.nullcoil.soulscorch.block.entity.SoulBrewingStandBlockEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SoulBrewingStandBlock extends BrewingStandBlock {
    public SoulBrewingStandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulBrewingStandBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level instanceof ServerLevel serverLevel) {
            return createTickerHelper(
                    type,
                    ModBlockEntities.SOUL_BREWING_STAND,
                    (ignored, pos, state, entity) ->
                            SoulBrewingStandBlockEntity.serverTick(serverLevel, pos, state, entity)
            );
        }
        return null;
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            BlockEntity var7 = level.getBlockEntity(blockPos);
            if (var7 instanceof SoulBrewingStandBlockEntity) {
                SoulBrewingStandBlockEntity brewingStandBlockEntity = (SoulBrewingStandBlockEntity)var7;
                player.openMenu(brewingStandBlockEntity);
                player.awardStat(Stats.INTERACT_WITH_BREWINGSTAND);
            }
        }

        return InteractionResult.SUCCESS;
    }
}