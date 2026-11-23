package net.nullcoil.soulscorch.alchemy;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The recipes only the Soul Brewing Stand can brew, filled in by {@link SoulPotionBrewingBuilder#BUILD}.
 * Built the first time a world with a given set of feature flags needs it, then kept.
 *
 * <p>The stand itself goes through the static helpers ({@link #isIngredient(Level, ItemStack)}
 * and the rest), which check these recipes first and then the world's shared list, so it brews
 * everything a vanilla stand can, plus these.
 */
public final class SoulPotionBrewing {
    record PotionMix(Holder<Potion> from, Ingredient ingredient, Holder<Potion> to) {}
    record ItemMix(Item from, Ingredient ingredient, Holder<Potion> to) {}

    private static final Map<FeatureFlagSet, SoulPotionBrewing> BY_FEATURES = new ConcurrentHashMap<>();

    private final List<PotionMix> potionMixes;
    private final List<ItemMix> itemMixes;

    SoulPotionBrewing(List<PotionMix> potionMixes, List<ItemMix> itemMixes) {
        this.potionMixes = potionMixes;
        this.itemMixes = itemMixes;
    }

    /** The Soul Brewing Stand's own recipes for this world. */
    public static SoulPotionBrewing get(Level level) {
        return BY_FEATURES.computeIfAbsent(level.enabledFeatures(), features -> {
            SoulPotionBrewingBuilder builder = new SoulPotionBrewingBuilder(features);
            SoulPotionBrewingBuilder.BUILD.invoker().build(builder);
            return builder.build();
        });
    }

    // ---- This list only ----

    /** Whether this stack can go in the ingredient slot for one of these recipes. */
    public boolean isIngredient(ItemStack ingredient) {
        for (PotionMix mix : this.potionMixes) if (mix.ingredient().test(ingredient)) return true;
        for (ItemMix mix : this.itemMixes) if (mix.ingredient().test(ingredient)) return true;
        return false;
    }

    /** Whether this stack is a non-potion that can go in a bottle slot, like Volatile Extract. */
    public boolean isBottleInput(ItemStack input) {
        for (ItemMix mix : this.itemMixes) if (input.is(mix.from())) return true;
        return false;
    }

    public boolean hasMix(ItemStack input, ItemStack ingredient) {
        return this.findPotionMix(input, ingredient).isPresent() || this.findItemMix(input, ingredient).isPresent();
    }

    /** What brewing {@code ingredient} into {@code input} gives, or {@code input} itself if nothing. */
    public ItemStack mix(ItemStack ingredient, ItemStack input) {
        Optional<PotionMix> potionMix = this.findPotionMix(input, ingredient);
        if (potionMix.isPresent()) {
            return PotionContents.createItemStack(input.getItem(), potionMix.get().to());
        }
        Optional<ItemMix> itemMix = this.findItemMix(input, ingredient);
        if (itemMix.isPresent()) {
            return PotionContents.createItemStack(Items.POTION, itemMix.get().to());
        }
        return input;
    }

    private Optional<PotionMix> findPotionMix(ItemStack input, ItemStack ingredient) {
        if (!isPotion(input)) return Optional.empty();
        Optional<Holder<Potion>> potion = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        if (potion.isEmpty()) return Optional.empty();
        for (PotionMix mix : this.potionMixes) {
            if (mix.from().is(potion.get()) && mix.ingredient().test(ingredient)) return Optional.of(mix);
        }
        return Optional.empty();
    }

    private Optional<ItemMix> findItemMix(ItemStack input, ItemStack ingredient) {
        for (ItemMix mix : this.itemMixes) {
            if (input.is(mix.from()) && mix.ingredient().test(ingredient)) return Optional.of(mix);
        }
        return Optional.empty();
    }

    private static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    // ---- What the Soul Brewing Stand uses: these recipes, then the shared ones ----

    public static boolean isIngredient(Level level, ItemStack ingredient) {
        return get(level).isIngredient(ingredient) || level.potionBrewing().isIngredient(ingredient);
    }

    /** Whether this stack can go in a bottle slot: any potion, a glass bottle, or a Soul-only input. */
    public static boolean isBottleInput(Level level, ItemStack input) {
        return isPotion(input) || input.is(Items.GLASS_BOTTLE) || get(level).isBottleInput(input);
    }

    public static boolean hasMix(Level level, ItemStack input, ItemStack ingredient) {
        return get(level).hasMix(input, ingredient) || level.potionBrewing().hasMix(input, ingredient);
    }

    public static ItemStack mix(Level level, ItemStack ingredient, ItemStack input) {
        SoulPotionBrewing soul = get(level);
        if (soul.hasMix(input, ingredient)) return soul.mix(ingredient, input);
        PotionBrewing shared = level.potionBrewing();
        return shared.mix(ingredient, input);
    }
}
