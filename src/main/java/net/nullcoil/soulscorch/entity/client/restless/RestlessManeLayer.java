package net.nullcoil.soulscorch.entity.client.restless;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.nullcoil.soulscorch.Soulscorch;

import java.io.InputStreamReader;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class RestlessManeLayer extends RenderLayer<RestlessRenderState, RestlessModel> {

    // NOTE: Depending on your exact mappings, you might need to change Identifier 
    // to Identifier if your IDE throws a fit!
    private static final Identifier BASE_MANE = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/restless/mane.png");

    private int frames = 0;
    private int frameTime = 4;
    private boolean interpolate = false;
    private Identifier[] frameTextures;

    private final RestlessManeModel maneModel;

    public RestlessManeLayer(RenderLayerParent<RestlessRenderState, RestlessModel> parent, EntityRendererProvider.Context context) {
        super(parent);
        this.maneModel = new RestlessManeModel(context.bakeLayer(RestlessManeModel.MANE_LAYER));
        loadNcMeta();
    }

    private void loadNcMeta() {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Identifier metaLoc = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/restless/mane.png.ncmeta");

        Optional<Resource> resource = manager.getResource(metaLoc);
        if (resource.isPresent()) {
            try (InputStreamReader reader = new InputStreamReader(resource.get().open())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if (json.has("frames")) this.frames = json.get("frames").getAsInt();
                if (json.has("frame_time")) this.frameTime = Math.max(1, json.get("frame_time").getAsInt());
                if (json.has("interpolate")) this.interpolate = json.get("interpolate").getAsBoolean();

                if (this.frames > 0) {
                    this.frameTextures = new Identifier[this.frames];
                    for (int i = 0; i < this.frames; i++) {
                        this.frameTextures[i] = Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "textures/entity/restless/mane_" + i + ".png");
                    }
                }
            } catch (Exception e) {
                Soulscorch.LOGGER.error("Failed to load .ncmeta for Restless Mane!", e);
            }
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector buffer, int packedLight, RestlessRenderState state, float f, float g) {
        if (!state.awakened) return;

        // MANUALLY copy the dynamic body position so it breathes and walks with the entity.
        net.minecraft.client.model.geom.ModelPart mainBody = this.getParentModel().getBody();
        net.minecraft.client.model.geom.ModelPart dummyBody = this.maneModel.getBody();

        dummyBody.x = mainBody.x;
        dummyBody.y = mainBody.y;
        dummyBody.z = mainBody.z;
        dummyBody.xRot = mainBody.xRot;
        dummyBody.yRot = mainBody.yRot;
        dummyBody.zRot = mainBody.zRot;

        if (this.frames == 0 || this.frameTextures == null) {
            renderFrame(poseStack, buffer, BASE_MANE, 1.0f, state);
        } else {
            float totalAnimTime = this.frames * this.frameTime;
            float currentAnimTime = state.age % totalAnimTime;
            int currentFrameIndex = (int) (currentAnimTime / this.frameTime);

            if (!this.interpolate) {
                renderFrame(poseStack, buffer, this.frameTextures[currentFrameIndex], 1.0f, state);
            } else {
                int nextFrameIndex = (currentFrameIndex + 1) % this.frames;
                float lerpProgress = (currentAnimTime % this.frameTime) / (float)this.frameTime;

                renderFrame(poseStack, buffer, this.frameTextures[currentFrameIndex], 1.0f - lerpProgress, state);
                renderFrame(poseStack, buffer, this.frameTextures[nextFrameIndex], lerpProgress, state);
            }
        }
    }

    private void renderFrame(PoseStack poseStack, SubmitNodeCollector buffer, Identifier texture, float alpha, RestlessRenderState state) {
        int alphaInt = (int)(alpha * 255.0f);
        int argb = (alphaInt << 24) | 0x00FFFFFF;

        // 15728880 is Minecraft's raw LightTexture.FULL_BRIGHT constant!
        int emissiveLight = 15728880;

        // Use the native entityTranslucent so alpha crossfading works, 
        // but jam max light into it so it glows!
        buffer.submitModel(
                this.maneModel,
                state,
                poseStack,
                RenderTypes.entityTranslucent(texture),
                emissiveLight,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                argb,
                (net.minecraft.client.renderer.texture.TextureAtlasSprite) null, // Explicit cast to fix the ambiguous null
                state.outlineColor
                // Explicit cast to fix the ambiguous null
        );
    }
}