package net.nullcoil.soulscorch.alchemy;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds recipes only the Soul Brewing Stand can brew. The Soul Brewing Stand's counterpart to
 * Fabric's {@code FabricPotionBrewingBuilder}: register a listener on {@link #BUILD} during mod
 * init, and add recipes to the builder it's given.
 *
 * <pre>{@code
 * SoulPotionBrewingBuilder.BUILD.register(builder -> {
 *     builder.registerItemRecipe(ModItems.VOLATILE_EXTRACT, Ingredient.of(ModItems.VISCOUS_VIOLET_CORT), ModAlchemy.PECULIAR);
 *     builder.registerRecipes(Ingredient.of(ModItems.GHAZE_TEAR), ModAlchemy.CLAIRVOYANCE);
 * });
 * }</pre>
 *
 * <p>Recipes added through Fabric's builder go in the shared list, which vanilla stands use as
 * well; the Soul Brewing Stand checks its own list first, then the shared one. A recipe whose
 * potions or items aren't enabled in the world (feature flags) is skipped, like vanilla's.
 */
public final class SoulPotionBrewingBuilder {
    public static final Event<BuildCallback> BUILD = EventFactory.createArrayBacked(BuildCallback.class,
            listeners -> builder -> {
                for (BuildCallback listener : listeners) {
                    listener.build(builder);
                }
            });

    private final FeatureFlagSet enabledFeatures;
    private final List<SoulPotionBrewing.PotionMix> potionMixes = new ArrayList<>();
    private final List<SoulPotionBrewing.ItemMix> itemMixes = new ArrayList<>();

    SoulPotionBrewingBuilder(FeatureFlagSet enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

    /**
     * An item in a bottle slot (not a potion, e.g. Volatile Extract) + {@code ingredient} →
     * a Potion of {@code output}.
     */
    public void registerItemRecipe(Item input, Ingredient ingredient, Holder<Potion> output) {
        if (input.isEnabled(this.enabledFeatures) && output.value().isEnabled(this.enabledFeatures)) {
            this.itemMixes.add(new SoulPotionBrewing.ItemMix(input, ingredient, output));
        }
    }

    /**
     * {@code input} + {@code ingredient} → {@code output}. Works for drinkable, splash and
     * lingering potions alike; the bottle type stays the same.
     */
    public void registerPotionRecipe(Holder<Potion> input, Ingredient ingredient, Holder<Potion> output) {
        if (input.value().isEnabled(this.enabledFeatures) && output.value().isEnabled(this.enabledFeatures)) {
            this.potionMixes.add(new SoulPotionBrewing.PotionMix(input, ingredient, output));
        }
    }

    /**
     * A new potion from the Peculiar Potion: Peculiar Potion + {@code ingredient} → {@code potion}.
     * The Soul Brewing Stand's version of vanilla's Awkward Potion start.
     */
    public void registerRecipes(Ingredient ingredient, Holder<Potion> potion) {
        this.registerPotionRecipe(ModAlchemy.PECULIAR, ingredient, potion);
    }

    public FeatureFlagSet getEnabledFeatures() {
        return this.enabledFeatures;
    }

    SoulPotionBrewing build() {
        return new SoulPotionBrewing(List.copyOf(this.potionMixes), List.copyOf(this.itemMixes));
    }

    @FunctionalInterface
    public interface BuildCallback {
        void build(SoulPotionBrewingBuilder builder);
    }
}
