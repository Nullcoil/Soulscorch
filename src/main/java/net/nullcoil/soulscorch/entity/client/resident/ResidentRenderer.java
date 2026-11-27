package net.nullcoil.soulscorch.entity.client.resident;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.Resident;
import org.jspecify.annotations.NonNull;

public class ResidentRenderer extends MobRenderer<Resident, ResidentRenderState, ResidentModel> {
    private static final Identifier TEXTURE = Soulscorch.id("textures/entity/resident.png");

    public ResidentRenderer(EntityRendererProvider.Context context) {
        super(context, new ResidentModel(context.bakeLayer(ResidentModel.RESIDENT)), 0.0f);
    }

    @Override
    public ResidentRenderState createRenderState() {
        return new ResidentRenderState();
    }

    @Override
    public @NonNull Identifier getTextureLocation(ResidentRenderState state) {
        return TEXTURE;
    }
}
