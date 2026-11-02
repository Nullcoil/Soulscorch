package net.nullcoil.soulscorch.entity.client.heedless;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.heedless.HeedlessEntity;
import net.nullcoil.soulscorch.entity.ai.heedless.HeedlessProximity;
import org.jspecify.annotations.NonNull;

import static net.nullcoil.soulscorch.entity.ai.heedless.HeedlessEntity.WARN_VARIANT;

public class HeedlessRenderer extends MobRenderer<HeedlessEntity, HeedlessRenderState, HeedlessModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/heedless/heedless.png");

    public HeedlessRenderer(EntityRendererProvider.Context context) {
        super(context, new HeedlessModel(context.bakeLayer(HeedlessModel.HEEDLESS)), 0.5f);
        this.addLayer(new HeedlessEmissiveLayer(this));
    }

    @Override
    public HeedlessRenderState createRenderState() { return new HeedlessRenderState(); }

    @Override
    public void extractRenderState(HeedlessEntity entity, HeedlessRenderState state, float age) {
        super.extractRenderState(entity, state, age);
        Vec3 cam = Minecraft.getInstance().gameRenderer.mainCamera().position();
        float dist = (float) Mth.sqrt(
                (float) (Mth.square(state.x - cam.x) + Mth.square(state.y - cam.y) + Mth.square(state.z - cam.z))
        );
        state.cystGlow = HeedlessProximity.glow(dist);
        state.warnVariant = entity.getEntityData().get(WARN_VARIANT);
        state.warnAnimation.copyFrom(entity.warnAnimation);
    }

    @Override
    public @NonNull Identifier getTextureLocation(HeedlessRenderState state) {
        return TEXTURE;
    }
}
