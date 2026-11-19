package net.nullcoil.soulscorch.block.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/**
 * The player model, frozen in the pose they were caught in: mid-step, one arm thrown up to
 * shield their face, head tilted up toward whatever was watching them.
 */
@Environment(EnvType.CLIENT)
public class PlayerStatueModel extends PlayerModel {
    public PlayerStatueModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);

        this.head.xRot = -0.25F;            // looking up
        this.head.yRot = 0.15F;

        this.rightArm.xRot = -1.75F;        // thrown up in front of the face
        this.rightArm.yRot = -0.45F;
        this.rightArm.zRot = 0.1F;
        this.leftArm.xRot = 0.35F;          // the other one trailing back
        this.leftArm.zRot = -0.1F;

        this.rightLeg.xRot = -0.35F;        // mid-step
        this.leftLeg.xRot = 0.3F;
    }
}
