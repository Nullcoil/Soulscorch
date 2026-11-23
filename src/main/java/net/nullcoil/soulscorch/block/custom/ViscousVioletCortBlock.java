package net.nullcoil.soulscorch.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.item.ModItems;
import net.nullcoil.soulscorch.particles.ModParticles;

/**
 * Viscous Violet Cort: a mushroom that hangs from the underside of Soul Stone and drips pale
 * yellow goop. Two stages: young (age 0, drops 1) and grown (age 1, drops 1 or 2).
 */
public class ViscousVioletCortBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<ViscousVioletCortBlock> CODEC = simpleCodec(ViscousVioletCortBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    /** Hanging from the ceiling: young is 8 wide and 6 tall, grown 12 wide and 11 tall. */
    private static final VoxelShape[] SHAPES = {
            Block.column(8.0, 10.0, 16.0),
            Block.column(12.0, 5.0, 16.0)
    };
    /** How far down each stage reaches, in blocks from the top: where its goop drips from. */
    private static final double[] TIP = {10.0 / 16.0, 5.0 / 16.0};

    public ViscousVioletCortBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<ViscousVioletCortBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.SOUL_STONE);
    }

    /** Hangs: needs Soul Stone above it, not below. Breaks if that's gone (VegetationBlock's updateShape). */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return this.mayPlaceOn(level.getBlockState(above), level, above);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE && random.nextInt(10) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
        }
    }

    /** Goop beads at the tip and drips off: now and then while young, often once grown. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (random.nextInt(age == MAX_AGE ? 4 : 12) != 0) return;

        double spread = age == MAX_AGE ? 0.25 : 0.12;
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0 * spread;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0 * spread;
        double y = pos.getY() + 1.0 - TIP[age] - 0.05;
        level.addParticle(ModParticles.CORT_DRIP_HANG, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ModItems.VISCOUS_VIOLET_CORT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int i = Math.min(MAX_AGE, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, i), 2);
    }
}
