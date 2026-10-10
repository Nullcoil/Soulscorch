package net.nullcoil.soulscorch.entity.client.jellyfish;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

@Environment(EnvType.CLIENT)
public class JellyfishEmissiveLayer extends RenderLayer<JellyfishRenderState, JellyfishModel> {

    private static final Identifier GLOWEY_BITS =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/hytodom_emissive.png");

    public JellyfishEmissiveLayer(RenderLayerParent<JellyfishRenderState, JellyfishModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       JellyfishRenderState state, float yRot, float xRot) {

        if (state.isInvisible) {
            return;
        }

        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(GLOWEY_BITS),
                15728880,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                state.outlineColor
        );
    }
}