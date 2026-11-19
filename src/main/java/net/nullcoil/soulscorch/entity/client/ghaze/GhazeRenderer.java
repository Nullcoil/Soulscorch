package net.nullcoil.soulscorch.entity.client.ghaze;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.GhazeEntity;
import org.jspecify.annotations.NonNull;

public class GhazeRenderer extends MobRenderer<GhazeEntity, GhazeRenderState, GhazeModel> {
    private static final Identifier TEXTURE = Soulscorch.id("textures/entity/ghaze/ghaze.png");
    private static final Identifier AGITATED = Soulscorch.id("textures/entity/ghaze/ghaze.png");

    public GhazeRenderer(EntityRendererProvider.Context context) {
        super(context, new GhazeModel(context.bakeLayer(GhazeModel.GHAZE)), 0f);
        this.addLayer(new GhazeEmissiveLayer(this));
    }

    @Override
    public GhazeRenderState createRenderState() {
        return new GhazeRenderState();
    }

    @Override
    public void extractRenderState(GhazeEntity entity, GhazeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isAgitated = entity.isAgitated();
        state.agitation = entity.agitation;
        state.spinAngle = Mth.lerp(partialTick, entity.spinAngleO, entity.spinAngle);
        state.tentacleSwing = entity.tentacleSwing;
        state.wiggle = entity.wiggle;
        state.windSeed = (entity.getId() * 0.618034F) % 1.0F * 100.0F;
    }

    @Override
    public @NonNull Identifier getTextureLocation(GhazeRenderState state) {
        return state.isAgitated ? AGITATED : TEXTURE;
    }
}
