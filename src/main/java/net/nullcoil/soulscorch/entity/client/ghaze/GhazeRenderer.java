package net.nullcoil.soulscorch.entity.client.ghaze;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.GhazeEntity;
import org.jspecify.annotations.NonNull;

public class GhazeRenderer extends MobRenderer<GhazeEntity, GhazeRenderState, GhazeModel> {
    private static final Identifier TEXTURE = Soulscorch.id("textures/entity/ghaze.png");

    public GhazeRenderer(EntityRendererProvider.Context context) {
        super(context, new GhazeModel(context.bakeLayer(GhazeModel.GHAZE)), 0f);
    }

    @Override
    public GhazeRenderState createRenderState() {
        return new GhazeRenderState();
    }

    @Override
    public @NonNull Identifier getTextureLocation(GhazeRenderState state) {
        return TEXTURE;
    }
}
