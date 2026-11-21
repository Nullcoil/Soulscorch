package net.nullcoil.soulscorch.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
    /** A render state that shows every skin layer (hat, jacket, sleeves, pants); nothing else on it matters here. */
    public static AvatarRenderState allLayers() {
        AvatarRenderState state = new AvatarRenderState();
        state.showHat = true;
        state.showJacket = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showLeftPants = true;
        state.showRightPants = true;
        return state;
    }

    /**
     * From block space (0 to 1, facing north) into model space, the way a living entity is
     * drawn: centered and flipped, with the feet (24 pixels down in the model) on the floor.
     */
    public static void toModelSpace(PoseStack poseStack) {
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
    }

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
