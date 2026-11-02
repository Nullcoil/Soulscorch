package net.nullcoil.soulscorch.entity.client.soulless;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ai.SoullessEntity;

@Environment(EnvType.CLIENT)
public class SoullessRenderer extends HumanoidMobRenderer<SoullessEntity, SoullessRenderState, SoullessModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulless/soulless.png");
    private static final Identifier AWAKENED = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulless/awakened.png");

    private static final Identifier TEXTURE_BABY = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulless/soulless_baby.png");
    private static final Identifier AWAKENED_BABY = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/soulless/awakened_baby.png");

    public SoullessRenderer(EntityRendererProvider.Context context) {
        super(context,
              new SoullessModel(context.bakeLayer(SoullessModel.SOULLESS)),
              new SoullessModel(context.bakeLayer(SoullessModel.SOULLESS_BABY)),
              0.5f,
              PiglinRenderer.PIGLIN_CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer<>(
                      this,
                      ArmorModelSet.bake(ModelLayers.ZOMBIFIED_PIGLIN_ARMOR, context.getModelSet(), SoullessModel::new),
                      ArmorModelSet.bake(ModelLayers.ZOMBIFIED_PIGLIN_BABY_ARMOR, context.getModelSet(), SoullessModel::new),
                      context.getEquipmentRenderer()));
        this.addLayer(new SoullessEmissiveLayer(this));
    }

    @Override
    public SoullessRenderState createRenderState() {
        return new SoullessRenderState();
    }

    @Override
    public void extractRenderState(SoullessEntity entity, SoullessRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.currentActivity = entity.getActivity();
        state.active = state.currentActivity != SoullessActivity.PASSIVE;

        state.neutralTwitchState = entity.neutralTwitchState;

        state.passiveAnimationState.copyFrom(entity.passiveAnimationState);
        state.neutralAnimationState.copyFrom(entity.neutralAnimationState);
        state.hostileAnimationState.copyFrom(entity.hostileAnimationState);
    }

    @Override
    public Identifier getTextureLocation(SoullessRenderState state) {
        if (state.isBaby) {
            return state.active ? AWAKENED_BABY : TEXTURE_BABY;
        }
        return state.active ? AWAKENED : TEXTURE;
    }
}