package net.nullcoil.soulscorch.entity.client.ghaze;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nullcoil.soulscorch.Soulscorch;

/**
 * Draws the Ghaze's glowing parts at full brightness on top of its normal texture.
 * Only the visible pixels of the emissive textures glow; leave everything else transparent.
 * When it turns agitated, the agitated glow switches on with the agitated texture, and the
 * calm glow fades out over about a second and a half (and back in once it calms down).
 */
public class GhazeEmissiveLayer extends RenderLayer<GhazeRenderState, GhazeModel> {
    private static final Identifier EMISSIVE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/ghaze/ghaze_emissive.png");
    private static final Identifier AGITATED_EMISSIVE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/ghaze/ghaze_agitated_emissive.png");

    /** Full block and sky light, so the overlay glows in the dark. */
    private static final int FULL_BRIGHT = 15728880;

    public GhazeEmissiveLayer(RenderLayerParent<GhazeRenderState, GhazeModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       GhazeRenderState state, float yRot, float xRot) {
        if (state.isInvisible) return;

        int calmAlpha = Mth.clamp(Math.round((1.0F - state.agitation) * 255.0F), 0, 255);
        if (calmAlpha > 2) {
            this.submitGlow(poseStack, submitNodeCollector, state, EMISSIVE, (calmAlpha << 24) | 0x00FFFFFF);
        }
        if (state.isAgitated) {
            this.submitGlow(poseStack, submitNodeCollector, state, AGITATED_EMISSIVE, 0xFFFFFFFF);
        }
    }

    private void submitGlow(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, GhazeRenderState state,
                            Identifier texture, int color) {
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(texture),
                FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(state, 0f), // still flashes red when hurt
                color,                                            // alpha drives the fade
                (TextureAtlasSprite) null,
                state.outlineColor,                               // keeps the Glowing outline
                (ModelFeatureRenderer.CrumblingOverlay) null
        );
    }
}
