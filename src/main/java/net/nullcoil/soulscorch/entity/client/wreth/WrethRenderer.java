package net.nullcoil.soulscorch.entity.client.wreth;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.Resident;
import net.nullcoil.soulscorch.entity.ai.Wreth;
import net.nullcoil.soulscorch.entity.client.resident.ResidentModel;
import org.jspecify.annotations.NonNull;

public class WrethRenderer extends MobRenderer<Wreth, WrethRenderState, WrethModel> {
    private static final Identifier TEXTURE = Soulscorch.id("textures/entity/wreth.png");

    public WrethRenderer(EntityRendererProvider.Context context) {
        super(context, new WrethModel(context.bakeLayer(WrethModel.WRETH)), 0.0f);
    }

    @Override
    public WrethRenderState createRenderState() {
        return new WrethRenderState();
    }

    @Override
    public @NonNull Identifier getTextureLocation(WrethRenderState state) {
        return TEXTURE;
    }
}
