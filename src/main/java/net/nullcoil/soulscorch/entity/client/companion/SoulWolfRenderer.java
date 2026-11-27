package net.nullcoil.soulscorch.entity.client.companion;

import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.animal.wolf.BabyWolfModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.entity.ai.SoulborneWolf;
import org.jspecify.annotations.NonNull;

public class SoulWolfRenderer extends AgeableMobRenderer<SoulborneWolf, WolfRenderState, WolfModel> {

    public SoulWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultWolfModel(context.bakeLayer(ModelLayers.WOLF)), new BabyWolfModel(context.bakeLayer(ModelLayers.WOLF_BABY)), .4f);

        this.addLayer(new WolfCollarLayer(this));
    }

    @Override
    public @NonNull Identifier getTextureLocation(WolfRenderState state) {
        return state.texture;
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(SoulborneWolf entity, WolfRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isAngry = entity.isAngry();
        state.isSitting = entity.isInSittingPose();
        state.tailAngle = entity.getTailAngle();
        state.headRollAngle = entity.getHeadRollAngle(partialTick);
        state.shakeAnim = entity.getShakeAnim(partialTick);
        state.texture = entity.getTexture();
        state.wetShade = entity.getWetShade(partialTick);
        state.collarColor = entity.isTame() ? entity.getCollarColor() : null;
        state.bodyArmorItem = entity.getBodyArmorItem().copy();
    }
}
