package net.nullcoil.soulscorch.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Like vanilla's ColumnFeatureConfiguration, plus the block the columns are made of.
 *
 * @param state  block the columns are built from
 * @param reach  how wide each column cluster spreads
 * @param height how tall the columns grow
 */
public record BlockColumnConfiguration(BlockState state, IntProvider reach, IntProvider height)
        implements FeatureConfiguration {

    public static final Codec<BlockColumnConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(BlockColumnConfiguration::state),
            IntProviders.codec(0, 3).fieldOf("reach").forGetter(BlockColumnConfiguration::reach),
            IntProviders.codec(1, 10).fieldOf("height").forGetter(BlockColumnConfiguration::height)
    ).apply(instance, BlockColumnConfiguration::new));
}
