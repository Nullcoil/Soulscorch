package net.nullcoil.soulscorch.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nullcoil.soulscorch.alchemy.SoulPotionBrewing;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import net.nullcoil.soulscorch.block.custom.SoulBrewingStandBlock;
import net.nullcoil.soulscorch.item.ModItems;
import net.nullcoil.soulscorch.screen.SoulBrewingStandMenu;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class SoulBrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int[] SLOTS_FOR_UP = new int[]{3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
    public static final int FUEL_USES = 20;
    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int NUM_DATA_VALUES = 2;
    private static final short DEFAULT_BREW_TIME = 0;
    private static final byte DEFAULT_FUEL = 0;
    private static final Component DEFAULT_NAME = Component.translatable("container.soul_brewing_stand");
    private NonNullList<ItemStack> items;
    int brewTime;
    private boolean[] lastPotionCount;
    private Item ingredient;
    int fuel;
    protected final ContainerData dataAccess;

    public SoulBrewingStandBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.SOUL_BREWING_STAND, blockPos, blockState);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.dataAccess = new ContainerData() {
            public int get(int i) {
                int var10000;
                switch (i) {
                    case 0 -> var10000 = SoulBrewingStandBlockEntity.this.brewTime;
                    case 1 -> var10000 = SoulBrewingStandBlockEntity.this.fuel;
                    default -> var10000 = 0;
                }

                return var10000;
            }

            public void set(int i, int j) {
                switch (i) {
                    case 0 -> SoulBrewingStandBlockEntity.this.brewTime = j;
                    case 1 -> SoulBrewingStandBlockEntity.this.fuel = j;
                }

            }

            public int getCount() {
                return 2;
            }
        };
    }

    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    public int getContainerSize() {
        return this.items.size();
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SoulBrewingStandBlockEntity brewingStandBlockEntity) {
        ItemStack itemStack = (ItemStack)brewingStandBlockEntity.items.get(4);
        if (brewingStandBlockEntity.fuel <= 0 && itemStack.is(ModItems.BLAZT_POWDER)) {
            brewingStandBlockEntity.fuel = 20;
            itemStack.shrink(1);
            setChanged(level, blockPos, blockState);
        }

        boolean bl = isBrewable(level, brewingStandBlockEntity.items);
        boolean bl2 = brewingStandBlockEntity.brewTime > 0;
        ItemStack itemStack2 = (ItemStack)brewingStandBlockEntity.items.get(3);
        if (bl2) {
            --brewingStandBlockEntity.brewTime;
            boolean bl3 = brewingStandBlockEntity.brewTime == 0;
            if (bl3 && bl) {
                doBrew(level, blockPos, brewingStandBlockEntity.items);
            } else if (!bl || !itemStack2.is(brewingStandBlockEntity.ingredient)) {
                brewingStandBlockEntity.brewTime = 0;
            }

            setChanged(level, blockPos, blockState);
        } else if (bl && brewingStandBlockEntity.fuel > 0) {
            --brewingStandBlockEntity.fuel;
            brewingStandBlockEntity.brewTime = 200;
            brewingStandBlockEntity.ingredient = itemStack2.getItem();
            setChanged(level, blockPos, blockState);
        }

        boolean[] bls = brewingStandBlockEntity.getPotionBits();
        if (!Arrays.equals(bls, brewingStandBlockEntity.lastPotionCount)) {
            brewingStandBlockEntity.lastPotionCount = bls;
            BlockState blockState2 = blockState;
            if (!(blockState.getBlock() instanceof SoulBrewingStandBlock)) {
                return;
            }

            for(int i = 0; i < SoulBrewingStandBlock.HAS_BOTTLE.length; ++i) {
                blockState2 = (BlockState)blockState2.setValue(SoulBrewingStandBlock.HAS_BOTTLE[i], bls[i]);
            }

            level.setBlock(blockPos, blockState2, 2);
        }

    }

    private boolean[] getPotionBits() {
        boolean[] bls = new boolean[3];

        for(int i = 0; i < 3; ++i) {
            if (!((ItemStack)this.items.get(i)).isEmpty()) {
                bls[i] = true;
            }
        }

        return bls;
    }

    /** Soul-only recipes first, then the shared ones a vanilla stand uses ({@link SoulPotionBrewing}). */
    private static boolean isBrewable(Level level, NonNullList<ItemStack> nonNullList) {
        ItemStack itemStack = (ItemStack)nonNullList.get(3);
        if (itemStack.isEmpty()) {
            return false;
        } else if (!SoulPotionBrewing.isIngredient(level, itemStack)) {
            return false;
        } else {
            for(int i = 0; i < 3; ++i) {
                ItemStack itemStack2 = (ItemStack)nonNullList.get(i);
                if (!itemStack2.isEmpty() && SoulPotionBrewing.hasMix(level, itemStack2, itemStack)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static void doBrew(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList) {
        ItemStack itemStack = (ItemStack)nonNullList.get(3);
        for(int i = 0; i < 3; ++i) {
            nonNullList.set(i, SoulPotionBrewing.mix(level, itemStack, (ItemStack)nonNullList.get(i)));
        }

        itemStack.shrink(1);
        ItemStack itemStack2 = Objects.requireNonNull(itemStack.getItem().getCraftingRemainder()).create();
        if (!itemStack2.isEmpty()) {
            if (itemStack.isEmpty()) {
                itemStack = itemStack2;
            } else {
                Containers.dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
            }
        }

        nonNullList.set(3, itemStack);
        level.levelEvent(1035, blockPos, 0);
    }

    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(valueInput, this.items);
        this.brewTime = valueInput.getShortOr("BrewTime", (short)0);
        if (this.brewTime > 0) {
            this.ingredient = ((ItemStack)this.items.get(3)).getItem();
        }

        this.fuel = valueInput.getByteOr("Fuel", (byte)0);
    }

    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putShort("BrewTime", (short)this.brewTime);
        ContainerHelper.saveAllItems(valueOutput, this.items);
        valueOutput.putByte("Fuel", (byte)this.fuel);
    }

    public boolean canPlaceItem(int i, ItemStack itemStack) {
        if (i == 3) {
            return this.level != null && SoulPotionBrewing.isIngredient(this.level, itemStack);
        } else if (i == 4) {
            return itemStack.is(ModItems.BLAZT_POWDER);
        } else {
            return this.level != null && SoulPotionBrewing.isBottleInput(this.level, itemStack) && this.getItem(i).isEmpty();
        }
    }

    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        } else {
            return direction == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
        }
    }

    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(i, itemStack);
    }

    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return i == 3 ? itemStack.is(Items.GLASS_BOTTLE) : true;
    }

    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new SoulBrewingStandMenu(i, inventory, this, this.dataAccess);
    }

}