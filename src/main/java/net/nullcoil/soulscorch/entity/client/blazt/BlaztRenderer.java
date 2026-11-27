package net.nullcoil.soulscorch.entity.client.blazt;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.Blazt;

@Environment(EnvType.CLIENT)
public class BlaztRenderer extends MobRenderer<Blazt, BlaztRenderState, BlaztModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/blazt/blazt.png");
    private static final Identifier SHOOTING_TEXTURE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/blazt/blazt_shooting.png");
    private static final Identifier BULLRUSH_TEXTURE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/blazt/blazt_bullrush.png");

    public BlaztRenderer(EntityRendererProvider.Context context) {
        // context.getPart -> context.bakeLayer
        super(context, new BlaztModel(context.bakeLayer(BlaztModel.BLAZT)), 1.5F);
        this.addLayer(new BlaztEmissiveLayer(this));
    }

    @Override
    public BlaztRenderState createRenderState() {
        return new BlaztRenderState();
    }

    @Override
    public void extractRenderState(Blazt entity, BlaztRenderState state, float tickDelta) {
        // updateRenderState -> extractRenderState
        super.extractRenderState(entity, state, tickDelta);
        state.shooting = entity.isShooting();
        state.bullrushing = entity.isBullrushing();

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.shootAnimationState.copyFrom(entity.shootAnimationState);
    }

    @Override
    public Identifier getTextureLocation(BlaztRenderState state) {
        if (state.bullrushing) return BULLRUSH_TEXTURE;

        return state.shooting ? SHOOTING_TEXTURE : TEXTURE;
    }
}