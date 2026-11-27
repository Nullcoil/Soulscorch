package net.nullcoil.soulscorch.entity.client.jellyfish;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.jellyfish.AbstractJellyfish;

@Environment(EnvType.CLIENT)
public class JellyfishRenderer extends MobRenderer<AbstractJellyfish, JellyfishRenderState, JellyfishModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/hytodom.png");

    public JellyfishRenderer(EntityRendererProvider.Context context) {
        super(context, new JellyfishModel(context.bakeLayer(JellyfishModel.HYTODOM)), 0.0F);
        this.addLayer(new JellyfishEmissiveLayer(this));
    }

    @Override
    public Identifier getTextureLocation(JellyfishRenderState state) {
        return TEXTURE;
    }

    @Override
    public JellyfishRenderState createRenderState() {
        return new JellyfishRenderState();
    }

    @Override
    protected RenderType getRenderType(JellyfishRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        // Native method override to make the base texture perfectly translucent!
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    public void extractRenderState(AbstractJellyfish entity, JellyfishRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.IDLE.copyFrom(entity.IDLE);
        state.velocityX = (float) entity.getDeltaMovement().x;
        state.velocityY = (float) entity.getDeltaMovement().y;
        state.velocityZ = (float) entity.getDeltaMovement().z;
        state.bodyYaw = entity.yBodyRot;
        state.spinSpeed = entity.getSpinSpeed();
        state.isDying = !entity.isDeadOrDying();
    }
}