package net.nullcoil.soulscorch.entity.client.heedless;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nullcoil.soulscorch.Soulscorch;

public class HeedlessEmissiveLayer extends RenderLayer<HeedlessRenderState, HeedlessModel> {
    private static final Identifier CYSTS =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/heedless/heedless_cysts.png");

    public HeedlessEmissiveLayer(RenderLayerParent<HeedlessRenderState, HeedlessModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       HeedlessRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.cystGlow <= 0.01f) return;

        int alpha = Mth.clamp((int) (state.cystGlow * 255f), 0, 255);
        int color = (alpha << 24) | 0x00FFFFFF;

        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(CYSTS),
                15728880,
                LivingEntityRenderer.getOverlayCoords(state, 0f),
                color,
                null,
                state.outlineColor
        );
    }
}
