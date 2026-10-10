package net.nullcoil.soulscorch.entity.client.companion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.feline.BabyCatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.SoulborneCatEntity;

public class SoulCatRenderer extends AgeableMobRenderer<SoulborneCatEntity, CatRenderState, AbstractFelineModel<CatRenderState>> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/midnight.png");
    private static final Identifier BABY =
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulkitten.png");

    public SoulCatRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultCatModel(context.bakeLayer(ModelLayers.CAT)), new BabyCatModel(context.bakeLayer(ModelLayers.CAT_BABY)), .4f);
        this.addLayer(new CatCollarLayer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTextureLocation(CatRenderState state) {
        return state.isBaby ? BABY : TEXTURE;
    }

    @Override
    public CatRenderState createRenderState() {
        return new CatRenderState();
    }

    @Override
    public void extractRenderState(final SoulborneCatEntity entity, final CatRenderState state, final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.texture = entity.getVariant().value().assetInfo(state.isBaby).texturePath();
        state.isCrouching = entity.isCrouching();
        state.isSprinting = entity.isSprinting();
        state.isSitting = entity.isInSittingPose();
        state.lieDownAmount = entity.getLieDownAmount(partialTicks);
        state.lieDownAmountTail = entity.getLieDownAmountTail(partialTicks);
        state.relaxStateOneAmount = entity.getRelaxStateOneAmount(partialTicks);
        state.isLyingOnTopOfSleepingPlayer = entity.isLyingOnTopOfSleepingPlayer();
        state.collarColor = entity.isTame() ? entity.getCollarColor() : null;
    }

    protected void setupRotations(final CatRenderState state, final PoseStack poseStack, final float bodyRot, final float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        float lieDownAmount = state.lieDownAmount;
        if (lieDownAmount > 0.0F) {
            poseStack.translate(0.4F * lieDownAmount, 0.15F * lieDownAmount, 0.1F * lieDownAmount);
            poseStack.rotateDegrees(Axis.ZP, Mth.rotLerp(lieDownAmount, 0.0F, 90.0F));
            if (state.isLyingOnTopOfSleepingPlayer) {
                poseStack.translate(0.15F * lieDownAmount, 0.0F, 0.0F);
            }
        }
    }

}
