package net.nullcoil.soulscorch.entity.client.soulless;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.monster.piglin.AbstractPiglinModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

@Environment(EnvType.CLIENT)
public class SoullessModel extends AbstractPiglinModel<SoullessRenderState> {
    public static final ModelLayerLocation SOULLESS = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "soulless"), "main");
    public static final ModelLayerLocation SOULLESS_BABY = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "soulless"), "baby");

    private final KeyframeAnimation passiveAnimation;
    private final KeyframeAnimation neutralHeadTwitch0;
    private final KeyframeAnimation neutralHeadTwitch1;
    private final KeyframeAnimation neutralArmTwitch;

    public SoullessModel(ModelPart root) {
        super(root);
        this.passiveAnimation = SoullessAnimations.PASSIVE.bake(root);
        this.neutralHeadTwitch0 = SoullessAnimations.NEUTRAL_HEAD_TWITCH0.bake(root);
        this.neutralHeadTwitch1 = SoullessAnimations.NEUTRAL_HEAD_TWITCH1.bake(root);
        this.neutralArmTwitch = SoullessAnimations.NEUTRAL_ARM_TWITCH.bake(root);
    }

    // Adult layer — mirrors AdultPiglinModel.createBodyLayer() but using your mesh
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO
        );
        PartDefinition head = addHead(CubeDeformation.NONE, mesh);
        head.clearChild("hat");
        return LayerDefinition.create(mesh, 64, 64);
    }

    // Baby layer — points at the adult texture for now until you make a baby texture.
    // Mirrors BabyPiglinModel.createBodyLayer() structure.
    public static LayerDefinition createBabyBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 18.0F, -0.5F)
        );
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(21, 30).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F)
                        .texOffs(0, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 6.0F, 7.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F)
        );
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition leftear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.2F, -4.0F, 0.0F));
        leftear.addOrReplaceChild(
                "left_ear_r1",
                CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(1.0F, 1.75F, 0.0F, 0.0F, 0.0F, -0.6109F)
        );
        PartDefinition rightear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.2F, -4.0F, 0.0F));
        rightear.addOrReplaceChild(
                "right_ear_r1",
                CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(-1.0F, 1.75F, 0.0F, 0.0F, 0.0F, 0.6109F)
        );
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(28, 13).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(-4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(22, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(-1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(10, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(1.5F, 20.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    // This is the abstract method you were blocked on. Adult ear angle matches AdultPiglinModel.
    public float getDefaultEarAngleInDegrees() {
        return 30.0F;
    }

    @Override
    public void setupAnim(SoullessRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float legZ = state.isBaby ? 0f : 4f;
        float nLegZ = state.isBaby ? 1f : 3f;

        switch (state.currentActivity) {
            case PASSIVE -> {
                this.body.xRot = (float) Math.toRadians(20);
                this.head.xRot = (float) Math.toRadians(40);
                this.head.yRot = (float) Math.toRadians(-12);
                this.head.zRot = (float) Math.toRadians(-4);
                this.leftLeg.z += legZ;
                this.rightLeg.z += legZ;
                this.leftLeg.xRot = (float) Math.toRadians(10);
                this.leftLeg.zRot = (float) Math.toRadians(-5);
                this.rightLeg.xRot = (float) Math.toRadians(-10);
                this.rightLeg.yRot = (float) Math.toRadians(-7);
                this.rightLeg.zRot = (float) Math.toRadians(5);

                this.passiveAnimation.apply(state.passiveAnimationState, state.ageInTicks);
            }
            case NEUTRAL -> {
                super.setupAnim(state);
                this.leftLeg.z += nLegZ;
                this.rightLeg.z += nLegZ;
                this.leftLeg.xRot = (float) Math.toRadians(10);
                this.leftLeg.zRot = (float) Math.toRadians(-5);
                this.rightLeg.xRot = (float) Math.toRadians(-10);
                this.rightLeg.yRot = (float) Math.toRadians(-7);
                this.rightLeg.zRot = (float) Math.toRadians(5);
                this.head.xRot += (float) Math.toRadians(14.6599);
                this.head.yRot += (float) Math.toRadians(3.2113);
                this.head.zRot += (float) Math.toRadians(-12.0868);
                this.body.xRot = (float) Math.toRadians(17.5);

                if (state.neutralTwitchState == 0) this.neutralHeadTwitch0.apply(state.neutralAnimationState, state.ageInTicks);
                else if (state.neutralTwitchState == 1) this.neutralHeadTwitch1.apply(state.neutralAnimationState, state.ageInTicks);
                else if (state.neutralTwitchState == 2) this.neutralArmTwitch.apply(state.neutralAnimationState, state.ageInTicks);
            }
            case HOSTILE -> {
                super.setupAnim(state);

                float attackProgress = state.swingAnimation;
                float defaultPitch = -(float) Math.PI / 1.5F;

                float swingSin1 = net.minecraft.util.Mth.sin(attackProgress * (float) Math.PI);
                float swingSin2 = net.minecraft.util.Mth.sin((1.0F - (1.0F - attackProgress) * (1.0F - attackProgress)) * (float) Math.PI);

                this.rightArm.zRot = 0.0F;
                this.rightArm.yRot = -(0.1F - swingSin1 * 0.6F);
                this.rightArm.xRot = defaultPitch + (swingSin1 * 1.2F - swingSin2 * 0.4F);

                this.leftArm.zRot = 0.0F;
                this.leftArm.yRot = 0.1F - swingSin1 * 0.6F;
                this.leftArm.xRot = defaultPitch + (swingSin1 * 1.2F - swingSin2 * 0.4F);

                AnimationUtils.bobArms(this.rightArm, this.leftArm, state.ageInTicks);
            }
        }
    }
}