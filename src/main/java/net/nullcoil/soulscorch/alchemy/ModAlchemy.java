package net.nullcoil.soulscorch.alchemy;


import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.item.ModItems;

public class ModAlchemy {
    /**
     * The Soul Brewing Stand's base potion, its Awkward Potion: no effect of its own; every
     * Soul-only potion starts from it ({@link SoulPotionBrewingBuilder#registerRecipes}).
     */
    public static final Holder<Potion> PECULIAR;
    public static final Holder<Potion> SOUL_RENDERING;
    public static final Holder<Potion> SOUL_RENDERING_STRONG;

    // ---- Soul Brewing Stand only (durations in ticks; 20 a second) ----
    /** Nearby mobs glow through walls, for you only. */
    public static final Holder<Potion> CLAIRVOYANCE;
    public static final Holder<Potion> LONG_CLAIRVOYANCE;
    /** Twice the radius, half the time, like vanilla's Strong potions. */
    public static final Holder<Potion> STRONG_CLAIRVOYANCE;
    /** Vanilla's Blindness effect, as a potion. A blind Ghaze can't become agitated. */
    public static final Holder<Potion> BLINDNESS;
    public static final Holder<Potion> LONG_BLINDNESS;
    /** Instant cure on players; a 5-minute countdown to a one-step cure on mobs. */
    public static final Holder<Potion> ASCLEPIOSIS;
    public static final Holder<Potion> LONG_ASCLEPIOSIS;
    /** A 5-minute countdown to zombification on Villagers and Piglins. */
    public static final Holder<Potion> THANATOSIS;
    public static final Holder<Potion> LONG_THANATOSIS;

    private static Holder<Potion> register(String path, Potion potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, path), potion);
    }

    static {
        PECULIAR = register("peculiar", new Potion("peculiar"));
        SOUL_RENDERING = register("soul_rendering",
                new Potion(
                        "soul_rendering",
                        new MobEffectInstance[]{
                                new MobEffectInstance(ModEffects.SOUL_RENDER, 1, 0)
                        }));
        SOUL_RENDERING_STRONG = register("soul_rendering_strong",
                new Potion(
                        "soul_rendering_strong",
                        new MobEffectInstance[]{
                                new MobEffectInstance(ModEffects.SOUL_RENDER, 1, 1)
                        }));

        // Long and Strong versions share the base name, like vanilla's, so they share its lang keys
        CLAIRVOYANCE = register("clairvoyance", new Potion("clairvoyance", new MobEffectInstance(ModEffects.CLAIRVOYANCE, 3600)));
        LONG_CLAIRVOYANCE = register("long_clairvoyance", new Potion("clairvoyance", new MobEffectInstance(ModEffects.CLAIRVOYANCE, 9600)));
        STRONG_CLAIRVOYANCE = register("strong_clairvoyance", new Potion("clairvoyance", new MobEffectInstance(ModEffects.CLAIRVOYANCE, 1800, 1)));
        BLINDNESS = register("blindness", new Potion("blindness", new MobEffectInstance(MobEffects.BLINDNESS, 1800)));
        LONG_BLINDNESS = register("long_blindness", new Potion("blindness", new MobEffectInstance(MobEffects.BLINDNESS, 4800)));
        ASCLEPIOSIS = register("asclepiosis", new Potion("asclepiosis", new MobEffectInstance(ModEffects.ASCLEPIOSIS, 6000)));
        LONG_ASCLEPIOSIS = register("long_asclepiosis", new Potion("asclepiosis", new MobEffectInstance(ModEffects.ASCLEPIOSIS, 9600)));
        THANATOSIS = register("thanatosis", new Potion("thanatosis", new MobEffectInstance(ModEffects.THANATOSIS, 6000)));
        LONG_THANATOSIS = register("long_thanatosis", new Potion("thanatosis", new MobEffectInstance(ModEffects.THANATOSIS, 9600)));
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Alchemy for " + Soulscorch.MOD_ID);

        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.addMix(
                    Potions.AWKWARD,
                    ModItems.SOUL_CREAM,
                    SOUL_RENDERING
            );
        });

        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.addMix(
                    SOUL_RENDERING,
                    Items.GLOWSTONE_DUST,
                    SOUL_RENDERING_STRONG
            );
        });

        // Soul Brewing Stand only
        SoulPotionBrewingBuilder.BUILD.register(builder -> {
            Ingredient redstone = Ingredient.of(Items.REDSTONE);
            Ingredient glowstone = Ingredient.of(Items.GLOWSTONE_DUST);
            Ingredient spiderEye = Ingredient.of(Items.FERMENTED_SPIDER_EYE);

            // Volatile Extract in the bottle slots
            builder.registerItemRecipe(ModItems.VOLATILE_EXTRACT, Ingredient.of(ModItems.VISCOUS_VIOLET_CORT), PECULIAR);

            // Peculiar + Ghaze Tear: Clairvoyance, then Fermented Spider Eye: Blindness
            builder.registerRecipes(Ingredient.of(ModItems.GHAZE_TEAR), CLAIRVOYANCE);
            builder.registerPotionRecipe(CLAIRVOYANCE, redstone, LONG_CLAIRVOYANCE);
            builder.registerPotionRecipe(CLAIRVOYANCE, glowstone, STRONG_CLAIRVOYANCE);
            builder.registerPotionRecipe(CLAIRVOYANCE, spiderEye, BLINDNESS);
            builder.registerPotionRecipe(LONG_CLAIRVOYANCE, spiderEye, LONG_BLINDNESS);
            builder.registerPotionRecipe(BLINDNESS, redstone, LONG_BLINDNESS);

            // Peculiar + Avalcomb: Asclepiosis, then Fermented Spider Eye: Thanatosis
            builder.registerRecipes(Ingredient.of(ModItems.AVALCOMB), ASCLEPIOSIS);
            builder.registerPotionRecipe(ASCLEPIOSIS, redstone, LONG_ASCLEPIOSIS);
            builder.registerPotionRecipe(ASCLEPIOSIS, spiderEye, THANATOSIS);
            builder.registerPotionRecipe(LONG_ASCLEPIOSIS, spiderEye, LONG_THANATOSIS);
            builder.registerPotionRecipe(THANATOSIS, redstone, LONG_THANATOSIS);
        });
    }
}
