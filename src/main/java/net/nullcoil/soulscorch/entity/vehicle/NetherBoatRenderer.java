package net.nullcoil.soulscorch.entity.vehicle;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import com.mojang.blaze3d.vertex.PoseStack;

public class NetherBoatRenderer extends BoatRenderer {
    private final Model.Simple lavaPatch;
    private final Identifier texture;

    public NetherBoatRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer) {
        super(ctx, layer);
        this.texture = layer.model().withPath(p -> "textures/entity/" + p + ".png");
        this.lavaPatch = new Model.Simple(
                ctx.bakeLayer(ModelLayers.BOAT_WATER_PATCH),
                RenderTypes::entityTranslucent
        );
    }

    @Override
    protected void submitTypeAdditions(
            BoatRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light
    ) {
        super.submitTypeAdditions(state, poseStack, collector, light);
        collector.submitModel(
                this.lavaPatch,
                Unit.INSTANCE,
                poseStack,
                this.lavaPatch.renderType(this.texture),
                light,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );
    }
}