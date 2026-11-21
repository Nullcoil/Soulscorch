package net.nullcoil.soulscorch.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Draws the Petrified Statue item: the same frozen, grayscale player as the block, using the
 * profile stored on the item. Built like vanilla's player head item renderer (skin looked up
 * from the item's profile), with the statue model in place of the head.
 */
@Environment(EnvType.CLIENT)
public class PlayerStatueSpecialRenderer implements SpecialModelRenderer<PlayerStatueSpecialRenderer.Look> {
    /** What a statue item looks like: the grayscale skin, and whether it has slim arms. */
    public record Look(RenderType renderType, boolean slim) {}

    private final PlayerSkinRenderCache playerSkinRenderCache;
    private final PlayerStatueModel wideModel;
    private final PlayerStatueModel slimModel;
    private final AvatarRenderState modelState = PlayerStatueModel.allLayers();

    public PlayerStatueSpecialRenderer(PlayerSkinRenderCache playerSkinRenderCache,
                                       PlayerStatueModel wideModel, PlayerStatueModel slimModel) {
        this.playerSkinRenderCache = playerSkinRenderCache;
        this.wideModel = wideModel;
        this.slimModel = slimModel;
    }

    @Override
    public Look extractArgument(ItemStack stack) {
        ResolvableProfile owner = stack.get(DataComponents.PROFILE);
        PlayerSkin skin = owner != null
                ? this.playerSkinRenderCache.getOrDefault(owner).playerSkin()
                : DefaultPlayerSkin.getDefaultSkin();
        return new Look(RenderTypes.entityTranslucent(StatueSkins.gray(skin.body().texturePath())),
                skin.model() == PlayerModelType.SLIM);
    }

    @Override
    public void submit(@Nullable Look look, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (look == null) look = this.extractArgument(ItemStack.EMPTY);

        poseStack.pushPose();
        PlayerStatueModel.toModelSpace(poseStack);
        PlayerStatueModel model = look.slim() ? this.slimModel : this.wideModel;
        submitNodeCollector.submitModel(model, this.modelState, poseStack, look.renderType(),
                lightCoords, overlayCoords, outlineColor, null);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        PlayerStatueModel.toModelSpace(poseStack);
        this.wideModel.setupAnim(this.modelState);
        this.wideModel.root().getExtentsForGui(poseStack, output);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Look> {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public PlayerStatueSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new PlayerStatueSpecialRenderer(context.playerSkinRenderCache(),
                    new PlayerStatueModel(context.entityModelSet().bakeLayer(ModelLayers.PLAYER), false),
                    new PlayerStatueModel(context.entityModelSet().bakeLayer(ModelLayers.PLAYER_SLIM), true));
        }
    }
}
