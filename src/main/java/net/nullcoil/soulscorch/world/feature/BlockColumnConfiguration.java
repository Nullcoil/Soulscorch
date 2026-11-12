package net.nullcoil.soulscorch.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Like vanilla's ColumnFeatureConfiguration, plus the block the columns are made of
 * and which way they grow.
 *
 * @param state     block the columns are built from
 * @param reach     how wide each column cluster spreads
 * @param height    how long the columns grow
 * @param direction UP grows from floors (like vanilla), DOWN hangs from ceilings
 */
public record BlockColumnConfiguration(BlockState state, IntProvider reach, IntProvider height, Direction direction)
        implements FeatureConfiguration {

    public BlockColumnConfiguration(BlockState state, IntProvider reach, IntProvider height) {
        this(state, reach, height, Direction.UP);
    }

    public static final Codec<BlockColumnConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(BlockColumnConfiguration::state),
            IntProviders.codec(0, 3).fieldOf("reach").forGetter(BlockColumnConfiguration::reach),
            IntProviders.codec(1, 10).fieldOf("height").forGetter(BlockColumnConfiguration::height),
            Direction.CODEC.optionalFieldOf("direction", Direction.UP).forGetter(BlockColumnConfiguration::direction)
    ).apply(instance, BlockColumnConfiguration::new));
}
