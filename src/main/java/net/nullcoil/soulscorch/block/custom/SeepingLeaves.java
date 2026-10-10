package net.nullcoil.soulscorch.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullcoil.soulscorch.particles.ModParticles;
import net.nullcoil.soulscorch.sound.ModSounds;

public class SeepingLeaves extends UntintedParticleLeavesBlock {
    public SeepingLeaves(float chance, ParticleOptions particle, AmbientLeavesBlockSoundPlayer sounds, Properties properties) {
        super(chance, particle, sounds, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(10) == 0) {
            Direction direction = Direction.getRandom(random);
            if (direction != Direction.UP) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (!state.canOcclude() || !neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {

                    VoxelShape shape = state.getShape(level, pos);
                    if (shape.isEmpty()) return;

                    double minX = shape.min(Direction.Axis.X);
                    double maxX = shape.max(Direction.Axis.X);
                    double minY = shape.min(Direction.Axis.Y);
                    double maxY = shape.max(Direction.Axis.Y);
                    double minZ = shape.min(Direction.Axis.Z);
                    double maxZ = shape.max(Direction.Axis.Z);

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
