package net.nullcoil.soulscorch.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import org.jspecify.annotations.Nullable;

/**
 * Whose statue this is. Stored like a player head's owner: as a profile, carried over to the
 * item (the {@code minecraft:profile} component) when mined, and back when placed.
 */
public class PlayerStatueBlockEntity extends BlockEntity {
    private static final String TAG_PROFILE = "profile";

    private @Nullable ResolvableProfile owner;

    public PlayerStatueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLAYER_STATUE, pos, state);
    }

    public @Nullable ResolvableProfile getOwnerProfile() {
        return this.owner;
    }

    public void setOwner(@Nullable ResolvableProfile owner) {
        this.owner = owner;
        this.setChanged();
        if (this.level != null) {
            // Tell clients, so they draw the right skin
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(TAG_PROFILE, ResolvableProfile.CODEC, this.owner);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.owner = input.read(TAG_PROFILE, ResolvableProfile.CODEC).orElse(null);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    // The profile moves to and from the item as a component, like a player head's
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.owner = components.get(DataComponents.PROFILE);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.PROFILE, this.owner);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(TAG_PROFILE);
    }
}
