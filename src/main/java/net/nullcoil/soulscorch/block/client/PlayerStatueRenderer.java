package net.nullcoil.soulscorch.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.block.custom.PlayerStatueBlock;
import net.nullcoil.soulscorch.block.entity.PlayerStatueBlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Draws a statue: the full player model, in the owner's skin turned grayscale, standing on
 * the lower half and reaching up through the upper one. Built like vanilla's player head
 * renderer (same skin lookup), with the whole body instead of just the head.
 */
@Environment(EnvType.CLIENT)
public class PlayerStatueRenderer implements BlockEntityRenderer<PlayerStatueBlockEntity, PlayerStatueRenderState> {
    private final PlayerSkinRenderCache playerSkinRenderCache;
    private final PlayerStatueModel wideModel;
    private final PlayerStatueModel slimModel;
    /** Shows every skin layer (hat, jacket, sleeves, pants); nothing else on it matters here. */
    private final AvatarRenderState modelState = new AvatarRenderState();

    public PlayerStatueRenderer(BlockEntityRendererProvider.Context context) {
        this.playerSkinRenderCache = context.playerSkinRenderCache();
        this.wideModel = new PlayerStatueModel(context.entityModelSet().bakeLayer(ModelLayers.PLAYER), false);
        this.slimModel = new PlayerStatueModel(context.entityModelSet().bakeLayer(ModelLayers.PLAYER_SLIM), true);

        this.modelState.showHat = true;
        this.modelState.showJacket = true;
        this.modelState.showLeftSleeve = true;
        this.modelState.showRightSleeve = true;
        this.modelState.showLeftPants = true;
        this.modelState.showRightPants = true;
    }

    @Override
    public PlayerStatueRenderState createRenderState() {
        return new PlayerStatueRenderState();
    }

    @Override
    public void extractRenderState(PlayerStatueBlockEntity blockEntity, PlayerStatueRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        ResolvableProfile owner = blockEntity.getOwnerProfile();
        PlayerSkin skin = owner != null
                ? this.playerSkinRenderCache.getOrDefault(owner).playerSkin()
                : DefaultPlayerSkin.getDefaultSkin();

        state.renderType = RenderTypes.entityTranslucent(StatueSkins.gray(skin.body().texturePath()));
        state.slim = skin.model() == PlayerModelType.SLIM;
        state.facing = blockEntity.getBlockState().getValue(PlayerStatueBlock.FACING);
    }

    @Override
    public void submit(PlayerStatueRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        // Same setup as a living entity: centered, turned to face its direction, and flipped
        // into model space, with the feet (24 pixels down in the model) on the block's floor
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.facing.toYRot()));
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        PlayerStatueModel model = state.slim ? this.slimModel : this.wideModel;
        submitNodeCollector.submitModel(model, this.modelState, poseStack, state.renderType,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
    }
}
