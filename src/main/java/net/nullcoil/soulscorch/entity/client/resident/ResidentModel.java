package net.nullcoil.soulscorch.entity.client.resident;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.nullcoil.soulscorch.Soulscorch;

public class ResidentModel extends EntityModel<ResidentRenderState> {
    public static final ModelLayerLocation RESIDENT =
            new ModelLayerLocation(Soulscorch.id("resident"), "main");

    private final ModelPart head;
    private final ModelPart l_arm;
    private final ModelPart r_arm;
    private final ModelPart legs;
    private final ModelPart front_legs;
    private final ModelPart fl_leg;
    private final ModelPart fr_leg;
    private final ModelPart back_legs;
    private final ModelPart bl_leg;
    private final ModelPart br_leg;
    private final ModelPart torso;
    private final ModelPart upper_torso;

    public ResidentModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.l_arm = root.getChild("l_arm");
        this.r_arm = root.getChild("r_arm");
        this.legs = root.getChild("legs");
        this.front_legs = this.legs.getChild("front_legs");
        this.fl_leg = this.front_legs.getChild("fl_leg");
        this.fr_leg = this.front_legs.getChild("fr_leg");
        this.back_legs = this.legs.getChild("back_legs");
        this.bl_leg = this.back_legs.getChild("bl_leg");
        this.br_leg = this.back_legs.getChild("br_leg");
        this.torso = root.getChild("torso");
        this.upper_torso = this.torso.getChild("upper_torso");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(72, 28).addBox(-2.0F, -10.0F, -1.0F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 72).addBox(-2.0F, -10.0F, -1.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, -25.0F, -13.0F));

        root.addOrReplaceChild("l_arm", CubeListBuilder.create()
                        .texOffs(48, 28).addBox(-1.0F, -20.0F, -1.0F, 6.0F, 38.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(13.0F, -6.0F, -5.0F));

        root.addOrReplaceChild("r_arm", CubeListBuilder.create()
                        .texOffs(0, 53).addBox(0.0F, -24.0F, -1.0F, 6.0F, 43.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-17.0F, -6.0F, -5.0F));

        PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create(),
                PartPose.offset(3.0F, 24.0F, -5.0F));

        PartDefinition front_legs = legs.addOrReplaceChild("front_legs", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        front_legs.addOrReplaceChild("fl_leg", CubeListBuilder.create()
                        .texOffs(74, 0).addBox(-1.0F, -17.0F, -1.0F, 4.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        front_legs.addOrReplaceChild("fr_leg", CubeListBuilder.create()
                        .texOffs(88, 69).addBox(-1.0F, -17.0F, -1.0F, 4.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, 0.0F, 0.0F));

        PartDefinition back_legs = legs.addOrReplaceChild("back_legs", CubeListBuilder.create(),
                PartPose.offset(2.0F, 0.0F, 12.0F));

        back_legs.addOrReplaceChild("bl_leg", CubeListBuilder.create()
                        .texOffs(56, 72).addBox(-1.0F, -28.0F, -1.0F, 4.0F, 28.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        back_legs.addOrReplaceChild("br_leg", CubeListBuilder.create()
                        .texOffs(72, 69).addBox(-1.0F, -28.0F, -1.0F, 4.0F, 28.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-12.0F, 0.0F, 0.0F));

        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, 2.0F));

        torso.addOrReplaceChild("pelvis_r1", CubeListBuilder.create()
                        .texOffs(72, 49).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition upper_torso = torso.addOrReplaceChild("upper_torso", CubeListBuilder.create(),
                PartPose.offset(4.0F, -3.0F, -3.0F));

        upper_torso.addOrReplaceChild("body_r1", CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-11.0F, -15.0F, -1.0F, 14.0F, 15.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0865F, 0.0066F, 0.0093F));

        upper_torso.addOrReplaceChild("shoulders_r1", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-13.0F, 0.0F, -3.0F, 24.0F, 15.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -24.0F, -3.0F, 0.3043F, -0.0262F, 0.0832F));

        return LayerDefinition.create(mesh, 104, 104);
    }

    @Override
    public void setupAnim(ResidentRenderState state) {
        super.setupAnim(state);
    }
}
