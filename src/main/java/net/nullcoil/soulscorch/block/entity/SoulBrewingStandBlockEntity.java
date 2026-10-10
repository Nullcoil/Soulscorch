package net.nullcoil.soulscorch.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BrewingFuel;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import net.nullcoil.soulscorch.block.custom.SoulBrewingStandBlock;
import net.nullcoil.soulscorch.item.ModItems;
import net.nullcoil.soulscorch.screen.SoulBrewingStandMenu;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class SoulBrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int[] SLOTS_FOR_UP = new int[]{3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};

    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int DATA_TOTAL_BREW_TIME = 2;
    public static final int DATA_TOTAL_FUEL_USES = 3;
    public static final int NUM_DATA_VALUES = 4;

    /** Vanilla is 400 ticks. 200 = 2x speed. */
    public static final int BREW_TIME_TICKS = 200;
    public static final int BLAZT_FUEL_USES = 20;

    private static final Component DEFAULT_NAME = Component.translatable("container.soul_brewing_stand");

    private NonNullList<ItemStack> items;
    private int brewTime;
    private int totalBrewTime;
    private boolean[] lastPotionCount;
    private Item ingredient;
    private int fuel;
    private int totalFuel;
    protected final ContainerData dataAccess;
    private final RecipeManager.CachedCheck<BrewingInput, BrewingRecipe> quickCheck;

    public SoulBrewingStandBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.SOUL_BREWING_STAND, worldPosition, blockState);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.dataAccess = new ContainerData() {
            {
                Objects.requireNonNull(SoulBrewingStandBlockEntity.this);
            }

            @Override
            public int get(int dataId) {
                return switch (dataId) {
                    case 0 -> SoulBrewingStandBlockEntity.this.brewTime;
                    case 1 -> SoulBrewingStandBlockEntity.this.fuel;
                    case 2 -> SoulBrewingStandBlockEntity.this.totalBrewTime;
                    case 3 -> SoulBrewingStandBlockEntity.this.totalFuel;
                    default -> 0;
                };
            }

            @Override
            public void set(int dataId, int value) {
                switch (dataId) {
                    case 0 -> SoulBrewingStandBlockEntity.this.brewTime = value;
                    case 1 -> SoulBrewingStandBlockEntity.this.fuel = value;
                    case 2 -> SoulBrewingStandBlockEntity.this.totalBrewTime = value;
                    case 3 -> SoulBrewingStandBlockEntity.this.totalFuel = value;
                }
            }

            @Override
            public int getCount() {
                return NUM_DATA_VALUES;
            }
        };
        this.quickCheck = RecipeManager.createCheck(RecipeType.BREWING);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState selfState, SoulBrewingStandBlockEntity entity) {
        NonNullList<ItemStack> items = entity.getItems();
        ItemStack fuelStack = items.get(FUEL_SLOT);

        if (entity.fuel <= 0 && isSoulFuel(fuelStack)) {
            entity.fuel = getFuelUses(fuelStack);
            entity.totalFuel = entity.fuel;
            consumeFuel(level, pos, items, fuelStack);
            setChanged(level, pos, selfState);
        }

        boolean brewable = isBrewable(level, entity);
        boolean isBrewing = entity.brewTime > 0;
        ItemStack ingredientStack = entity.items.get(INGREDIENT_SLOT);

        if (isBrewing) {
            --entity.brewTime;
            if (entity.brewTime == 0 && brewable) {
                doBrew(level, pos, entity);
            } else if (!brewable || !ingredientStack.is(entity.ingredient)) {
                entity.brewTime = 0;
            }
            setChanged(level, pos, selfState);
        } else if (brewable && entity.fuel > 0) {
            --entity.fuel;
            entity.brewTime = BREW_TIME_TICKS;
            entity.totalBrewTime = entity.brewTime;
            entity.ingredient = ingredientStack.getItem();
            setChanged(level, pos, selfState);
        }

        boolean[] newCount = entity.getPotionBits();
        if (!Arrays.equals(newCount, entity.lastPotionCount)) {
            entity.lastPotionCount = newCount;
            BlockState state = selfState;
            if (!(selfState.getBlock() instanceof SoulBrewingStandBlock)) {
                return;
            }
            for (int i = 0; i < SoulBrewingStandBlock.HAS_BOTTLE.length; ++i) {
                state = state.setValue(SoulBrewingStandBlock.HAS_BOTTLE[i], newCount[i]);
            }
            level.setBlock(pos, state, 2);
        }
    }

    private static boolean isSoulFuel(ItemStack stack) {
        return stack.is(ModItems.BLAZT_POWDER) || stack.has(DataComponents.BREWING_FUEL);
    }

    private static int getFuelUses(ItemStack stack) {
        if (stack.is(ModItems.BLAZT_POWDER)) {
            return BLAZT_FUEL_USES;
        }
        BrewingFuel brewingFuel = stack.get(DataComponents.BREWING_FUEL);
        return brewingFuel != null ? 20 : 0;
    }

    private static void consumeFuel(ServerLevel level, BlockPos pos, NonNullList<ItemStack> items, ItemStack fuel) {
        ItemStackTemplate remainder = fuel.getItem().getCraftingRemainder();
        ItemStack newFuel = fuel;
        fuel.shrink(1);
        if (remainder != null) {
            if (fuel.isEmpty()) {
                newFuel = remainder.create();
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder.create());
            }
        }
        items.set(FUEL_SLOT, newFuel);
    }

    private boolean[] getPotionBits() {
        boolean[] result = new boolean[3];
        for (int potion = 0; potion < 3; ++potion) {
            if (!this.items.get(potion).isEmpty()) {
                result[potion] = true;
            }
        }
        return result;
    }

    private static boolean isBrewable(ServerLevel serverLevel, SoulBrewingStandBlockEntity entity) {
        NonNullList<ItemStack> items = entity.getItems();
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        if (ingredient.isEmpty()) {
            return false;
        }
        RecipeManager recipeManager = serverLevel.recipeAccess();
        if (!recipeManager.propertySet(RecipePropertySet.BREWING_REAGENTS).test(ingredient)) {
            return false;
        }
        for (int dest = 0; dest < 3; ++dest) {
            ItemStack bottle = items.get(dest);
            if (!bottle.isEmpty()) {
                Optional<RecipeHolder<BrewingRecipe>> recipe =
                        entity.quickCheck.getRecipeFor(new BrewingInput(bottle, ingredient), serverLevel);
                if (recipe.isPresent()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void doBrew(ServerLevel level, BlockPos pos, SoulBrewingStandBlockEntity entity) {
        NonNullList<ItemStack> items = entity.getItems();
        ItemStack ingredient = items.get(INGREDIENT_SLOT);

        for (int dest = 0; dest < 3; ++dest) {
            ItemStack container = items.get(dest);
            BrewingInput input = new BrewingInput(container, ingredient);
            Optional<RecipeHolder<BrewingRecipe>> recipe = entity.quickCheck.getRecipeFor(input, level);
            items.set(dest, recipe.isPresent()
                    ? recipe.get().value().assemble(input)
                    : container);
        }

        ItemStackTemplate remainder = ingredient.getItem().getCraftingRemainder();
        ingredient.shrink(1);
        if (remainder != null) {
            if (ingredient.isEmpty()) {
                ingredient = remainder.create();
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder.create());
            }
        }

        items.set(INGREDIENT_SLOT, ingredient);
        level.levelEvent(1035, pos, 0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.brewTime = input.getIntOr("BrewTime", 0);
        this.totalBrewTime = input.getIntOr("total_brew_time", BREW_TIME_TICKS);
        if (this.brewTime > 0) {
            this.ingredient = this.items.get(INGREDIENT_SLOT).getItem();
        }
        this.fuel = input.getIntOr("Fuel", 0);
        this.totalFuel = input.getIntOr("total_fuel", BLAZT_FUEL_USES);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BrewTime", this.brewTime);
        output.putInt("total_brew_time", this.totalBrewTime);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Fuel", this.fuel);
        output.putInt("total_fuel", this.totalFuel);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        if (slot == FUEL_SLOT) {
            return itemStack.is(ModItems.BLAZT_POWDER) || itemStack.has(DataComponents.BREWING_FUEL);
        }
        if (this.level == null) {
            return false;
        }
        RecipeAccess recipeAccess = this.level.recipeAccess();
        if (slot == INGREDIENT_SLOT) {
            return recipeAccess.propertySet(RecipePropertySet.BREWING_REAGENTS).test(itemStack);
        }
        return PotionIngredient.isPotionInput(itemStack, recipeAccess) && this.getItem(slot).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return direction == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return slot == INGREDIENT_SLOT ? itemStack.is(Items.GLASS_BOTTLE) : true;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new SoulBrewingStandMenu(containerId, inventory, this, this.dataAccess);
    }
}