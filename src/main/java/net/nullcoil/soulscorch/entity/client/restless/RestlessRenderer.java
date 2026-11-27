package net.nullcoil.soulscorch.entity.client.restless;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.entity.ai.Restless;

@Environment(EnvType.CLIENT)
public class RestlessRenderer extends MobRenderer<Restless, RestlessRenderState, RestlessModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("soulscorch", "textures/entity/restless/restless.png");
    private static final Identifier AWAKENED =
            Identifier.fromNamespaceAndPath("soulscorch", "textures/entity/restless/restless_awakened.png");

    public RestlessRenderer(EntityRendererProvider.Context context) {
        super(context, new RestlessModel(context.bakeLayer(RestlessModel.RESTLESS)), 0.7f);

        // Add our new .ncmeta Layer
        this.addLayer(new RestlessManeLayer(this, context));
    }

    @Override
    public void extractRenderState(Restless entity, RestlessRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.awakened = entity.getAwakened();
        state.age = entity.tickCount + partialTick;
    }

    @Override
    public Identifier getTextureLocation(RestlessRenderState state) {
        return state.awakened ? AWAKENED : TEXTURE;
    }

    @Override
    public RestlessRenderState createRenderState() {
        return new RestlessRenderState();
    }
}