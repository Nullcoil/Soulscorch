package net.nullcoil.soulscorch.item.foods;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class ModFoods {
    public static final FoodProperties GHOST_PEPPER;
    public static final FoodProperties CANDIED_GHOST_PEPPER;
    public static final FoodProperties VOLATILE_EXTRACT;

    static {
        GHOST_PEPPER = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).alwaysEdible().build();
        CANDIED_GHOST_PEPPER = new FoodProperties.Builder().nutrition(8).saturationModifier(0.2f).alwaysEdible().build();
        VOLATILE_EXTRACT = new FoodProperties.Builder().nutrition(0).saturationModifier(0).alwaysEdible().build();
    }
}
