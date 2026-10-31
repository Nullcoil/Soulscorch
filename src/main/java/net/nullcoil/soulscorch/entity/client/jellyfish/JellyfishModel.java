package net.nullcoil.soulscorch.entity.client.jellyfish;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

@Environment(EnvType.CLIENT)
public class JellyfishModel extends EntityModel<JellyfishRenderState> {
    public static final ModelLayerLocation HYTODOM = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "hytodom"), "main");

    private final ModelPart root;
    private final ModelPart bell;
    private final ModelPart fringe;

    public JellyfishModel(ModelPart root) {
        super(root);
        this.root = root;
        this.bell = root.getChild("bell");
        this.fringe = this.bell.getChild("fringe");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition bell = root.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(40, 20)
                .addBox(-5.0F, -36.0F, -5.0F, 10.0F, 4.0F, 10.0F,
                        new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        bell.addOrReplaceChild("tentacle_1", CubeListBuilder.create().texOffs(20, 20)
                .addBox(0.0F, -1.0F, -5.0F, 0.0F, 32.0F, 10.0F,
                        new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F,
                0.0F, 0.7854F, 0.0F));

        bell.addOrReplaceChild("tentacle_2", CubeListBuilder.create().texOffs(0, 20)
                .addBox(0.0F, -1.0F, -5.0F, 0.0F, 32.0F, 10.0F,
                        new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F,
                0.0F, -0.7854F, 0.0F));

        bell.addOrReplaceChild("fringe", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 4.0F, 16.0F,
                        new CubeDeformation(0.0F)), PartPose.offset(0.0F, -34.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 80, 62);
    }

    @Override
    public void setupAnim(JellyfishRenderState state) {
        super.setupAnim(state);
        this.root.getAllParts().forEach(ModelPart::resetPose);
        if(state.isDying) {
            animateBell(state);
            animateTentacles(state);
        }
    }

    private void animateTentacles(JellyfishRenderState state) {
        ModelPart t1 = this.bell.getChild("tentacle_1");
        ModelPart t2 = this.bell.getChild("tentacle_2");

        float bodyYawRad = (float) Math.toRadians(state.bodyYaw);

        // 1. Project World Velocity into Local Space
        float localForwardVelocity = state.velocityX * (float)-Math.sin(bodyYawRad) + state.velocityZ * (float)Math.cos(bodyYawRad);
        float localRightVelocity   = state.velocityX * (float)Math.cos(bodyYawRad) + state.velocityZ * (float)Math.sin(bodyYawRad);

        // 2. Calculate the global drag
        float dragMultiplier = 8.0f;
        float dragPitch = localForwardVelocity * dragMultiplier;
        float dragRoll  = localRightVelocity * dragMultiplier;

        // 3. Calculate Spin Torsion
        float spinTwist = (float) Math.toRadians(-state.spinSpeed) * 1.5f;

        // --- REVERTED: Smooth, Symmetrical Breathing ---
        // A clean, continuous wave.
        // 0.05f is the speed, 0.05f is the maximum rotation angle in radians.
        float time = state.ageInTicks * 0.05f;
        float breathe = (float) Math.sin(time) * 0.05f;

        // 4. Apply to the tentacles
        applyTentacleDrag(t1, 0.7854f, dragPitch, dragRoll, spinTwist, breathe);
        applyTentacleDrag(t2, -0.7854f, dragPitch, dragRoll, spinTwist, breathe);
    }

    private void applyTentacleDrag(ModelPart tentacle, float baseYRot, float dragPitch, float dragRoll, float spinTwist, float breathe) {
        float localPitch = dragPitch * (float)Math.cos(-baseYRot) - dragRoll * (float)Math.sin(-baseYRot);
        float localRoll  = dragPitch * (float)Math.sin(-baseYRot) + dragRoll * (float)Math.cos(-baseYRot);

        // Blend the breathing animation into the pitch so they flare outward from the bell
        localPitch += breathe;

        // Safety Clamps: Prevents the tentacles from snapping their spines during high knockback!
        localPitch = Math.max(-1.2f, Math.min(1.2f, localPitch));
        localRoll  = Math.max(-1.2f, Math.min(1.2f, localRoll));

        tentacle.xRot = localPitch;
        tentacle.zRot = localRoll;
        tentacle.yRot = baseYRot + spinTwist;
    }

    private void animateBell(JellyfishRenderState state) {
        // 70.0f is 3.5s * 20 ticks/sec
        float t = (state.ageInTicks % 70.0f) / 20.0f;
        float theta = (float) (Math.PI * 2.0 * (3.5f - t) / 3.5f);
        float xzTheta = theta - 1.88495f; // Math.PI * 0.6f

        // Cache the trig calls so the CPU only does the heavy math once per tick!
        float cosTheta = (float) Math.cos(theta);
        float cosXzTheta = (float) Math.cos(xzTheta);

        // Apply Vertical
        bell.y += -0.5f * (cosTheta + 1.0f);
        fringe.yScale *= 2.0f + cosTheta;

        // Apply Horizontal
        float scaleXZ = 0.85f + 0.15f * cosXzTheta;
        fringe.xScale *= scaleXZ;
        fringe.zScale *= scaleXZ;
        fringe.y += cosXzTheta * 0.125f; // 0.125 is 2.0 / 16.0
    }
}