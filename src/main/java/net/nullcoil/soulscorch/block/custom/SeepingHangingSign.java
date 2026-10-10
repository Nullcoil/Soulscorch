package net.nullcoil.soulscorch.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import net.nullcoil.soulscorch.block.entity.SeepingHangingSignBlockEntity;
import net.nullcoil.soulscorch.particles.ModParticles;
import org.jetbrains.annotations.Nullable;
import net.nullcoil.soulscorch.sound.ModSounds;

public class SeepingHangingSign extends CeilingHangingSignBlock {
    public SeepingHangingSign(WoodType woodType, Properties properties) {
        super(woodType, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeepingHangingSignBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SEEPING_HANGING_SIGN, SignBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            Direction direction = Direction.getRandom(random);
            if (direction != Direction.UP) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                // Only spawn if the neighbor isn't covering this face
                if (!neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {

                    VoxelShape shape = state.getShape(level, pos);
                    if (shape.isEmpty()) return;

                    double minX = shape.min(Direction.Axis.X);
                    double maxX = shape.max(Direction.Axis.X);
                    double minY = shape.min(Direction.Axis.Y);
                    double maxY = shape.max(Direction.Axis.Y);
                    double minZ = shape.min(Direction.Axis.Z);
                    double maxZ = shape.max(Direction.Axis.Z);

                    // For the axis the direction travels along, pin to the face edge.
                    // For the other two axes, randomize within the shape's bounds.
                    double d = switch (direction.getAxis()) {
                        case X -> direction.getStepX() > 0 ? maxX : minX;
                        default -> minX + random.nextDouble() * (maxX - minX);
                    };
                    double e = switch (direction.getAxis()) {
                        case Y -> direction.getStepY() > 0 ? maxY : minY;
                        default -> minY + random.nextDouble() * (maxY - minY);
                    };
                    double f = switch (direction.getAxis()) {
                        case Z -> direction.getStepZ() > 0 ? maxZ : minZ;
                        default -> minZ + random.nextDouble() * (maxZ - minZ);
                    };

                    level.addParticle(ModParticles.SEEPING_DRIP_HANG,
                            pos.getX() + d, pos.getY() + e, pos.getZ() + f,
                            0d, 0d, 0d);
                    level.playLocalSound(pos, ModSounds.SOUL_DRIP,
                            SoundSource.BLOCKS, 1.0F, 0.8F, false);
                }
            }
        }
    }
}
