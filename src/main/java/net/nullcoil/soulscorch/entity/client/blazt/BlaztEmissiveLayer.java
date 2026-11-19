package net.nullcoil.soulscorch.entity.client.blazt;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

/**
 * Draws the Blazt's glowing parts at full brightness on top of its normal texture, with a
 * separate emissive texture for each of its looks (normal, shooting, bullrushing). Only the
 * visible pixels of the emissive texture glow; leave everything else transparent.
 */
public class BlaztEmissiveLayer extends RenderLayer<BlaztRenderState, BlaztModel> {
    private static final Identifier EMISSIVE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/blazt/blazt_emissive.png");
    private static final Identifier SHOOTING_EMISSIVE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/blazt/blazt_shooting_emissive.png");
    private static final Identifier BULLRUSH_EMISSIVE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/blazt/blazt_bullrush_emissive.png");

    /** Full block and sky light, so the overlay glows in the dark. */
    private static final int FULL_BRIGHT = 15728880;

    public BlaztEmissiveLayer(RenderLayerParent<BlaztRenderState, BlaztModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       BlaztRenderState state, float yRot, float xRot) {
        if (state.isInvisible) return;

        // Same priority as BlaztRenderer#getTextureLocation: bullrush, then shooting, then normal
        Identifier texture = state.bullrushing ? BULLRUSH_EMISSIVE : state.shooting ? SHOOTING_EMISSIVE : EMISSIVE;

        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(texture),
                FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(state, 0f), // still flashes red when hurt
                0xFFFFFFFF,                                       // no tint, fully opaque
                (TextureAtlasSprite) null,
                state.outlineColor,                               // keeps the Glowing outline
                (ModelFeatureRenderer.CrumblingOverlay) null
        );
    }
}
