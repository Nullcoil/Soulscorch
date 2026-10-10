package net.nullcoil.soulscorch.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricBrewingProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.nullcoil.soulscorch.alchemy.ModAlchemy;
import net.nullcoil.soulscorch.item.ModItems;

public class ModBrewingProvider extends FabricBrewingProvider {
    public ModBrewingProvider(RecipeOutput output) {
        super(output);
    }

    @Override
    protected void addContainers() {

    }

    @Override
    protected void addContainerTransformations() {

    }

    @Override
    protected void buildMixes() {
        buildMix(Potions.AWKWARD, ModItems.SOUL_CREAM, ModAlchemy.SOUL_RENDERING);
        buildMix(ModAlchemy.SOUL_RENDERING, Items.GLOWSTONE_DUST, ModAlchemy.SOUL_RENDERING_STRONG);
    }
}
