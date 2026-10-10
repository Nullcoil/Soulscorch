package net.nullcoil.soulscorch.entity.client.soulless;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

@Environment(EnvType.CLIENT)
public class SoullessEmissiveLayer extends RenderLayer<SoullessRenderState, SoullessModel> {
    private static final Identifier AWAKENED_EYE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulless/awakened_eye.png");
    private static final Identifier AWAKENED_EYE_BABY =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulless/awakened_baby_eye.png");

    public SoullessEmissiveLayer(RenderLayerParent<SoullessRenderState, SoullessModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SoullessRenderState state, float yRot, float xRot) {
        if (state.isInvisible || !state.active) return;

        Identifier eye = state.isBaby ? AWAKENED_EYE_BABY : AWAKENED_EYE;
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.eyes(eye),
                15728880,
                LivingEntityRenderer.getOverlayCoords(state, 0f),
                state.outlineColor
        );
    }
}