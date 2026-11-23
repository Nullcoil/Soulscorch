package net.nullcoil.soulscorch.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.item.ModItems;

public class ViscousVioletCortBlock extends VegetationBlock {
    public static final MapCodec<ViscousVioletCortBlock> CODEC = simpleCodec(ViscousVioletCortBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntegerProperty AGE;
    private static final VoxelShape[] SHAPES;

    public MapCodec<ViscousVioletCortBlock> codec() {
        return CODEC;
    }

    public ViscousVioletCortBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return SHAPES[(Integer)state.getValue(AGE)];
    }

    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return state.is(ModBlocks.SOUL_STONE);
    }

    protected boolean isRandomlyTicking(final BlockState state) {
        return (Integer)state.getValue(AGE) < MAX_AGE;
    }

    protected void randomTick(BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        int age = (Integer)state.getValue(AGE);
        if (age < MAX_AGE && random.nextInt(10) == 0) {
            state = (BlockState)state.setValue(AGE, age + 1);
            level.setBlock(pos, state, 2);
        }

    }

    protected ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData) {
        return new ItemStack(ModItems.VISCOUS_VIOLET_CORT);
    }

    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{AGE});
    }

    static {
        AGE = BlockStateProperties.AGE_2;
        SHAPES = Block.boxes(MAX_AGE, (age) -> Block.column((double)16.0F, (double)0.0F, (double)(5 + age * MAX_AGE)));
    }
}
