package net.nullcoil.soulscorch.block.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class PlayerStatueRenderState extends BlockEntityRenderState {
    /** The grayscale copy of the owner's skin. */
    public RenderType renderType;
    /** Slim (Alex-style) arms. */
    public boolean slim;
    public Direction facing = Direction.NORTH;
}
