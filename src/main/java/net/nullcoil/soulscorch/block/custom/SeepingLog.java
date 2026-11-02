package net.nullcoil.soulscorch.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.soulscorch.particles.ModParticles;
import net.nullcoil.soulscorch.sound.ModSounds;

public class SeepingLog extends RotatedPillarBlock {

    public SeepingLog(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if(random.nextInt(5)==0) {
            Direction direction = Direction.getRandom(random);
            if(direction != Direction.UP) {
                BlockPos pos2 = pos.relative(direction);
                BlockState state2 = level.getBlockState(pos2);
                if(!state.canOcclude() || !state2.isFaceSturdy(level, pos2, direction.getOpposite())) {
                    double d = direction.getStepX() == 0 ? random.nextDouble() : (double)0.5f + (double) direction.getStepX() * 0.6;
                    double e = direction.getStepY() == 0 ? random.nextDouble() : (double)0.5f + (double) direction.getStepY() * 0.6;
                    double f = direction.getStepZ() == 0 ? random.nextDouble() : (double)0.5f + (double) direction.getStepZ() * 0.6;
                    level.addParticle(ModParticles.SEEPING_DRIP_HANG, (double) pos.getX() + d, (double)pos.getY()+e, (double)pos.getZ()+f, 0d, 0d, 0d);
                    level.playLocalSound(pos, ModSounds.SOUL_DRIP, SoundSource.BLOCKS, 1.0F, 0.8F, false);
                }
            }
        }
    }
}
