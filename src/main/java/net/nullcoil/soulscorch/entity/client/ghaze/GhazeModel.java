package net.nullcoil.soulscorch.entity.client.ghaze;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

public class GhazeModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation GHAZE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghaze"), "main");

	private final ModelPart head;
	private final ModelPart[] tentacles = new ModelPart[8];

	public GhazeModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i] = root.getChild("tentacle" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 0.0F));

		// Each tentacle's pivot sits at the center of its top, like the ghast's, so it swings from where it meets the head
		partdefinition.addOrReplaceChild("tentacle0", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.75F, 14.6F, -3.0F));
		partdefinition.addOrReplaceChild("tentacle1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.25F, 14.6F, -4.0F));
		partdefinition.addOrReplaceChild("tentacle2", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.25F, 14.6F, -3.0F));
		partdefinition.addOrReplaceChild("tentacle3", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.25F, 16.6F, 0.0F));
		partdefinition.addOrReplaceChild("tentacle4", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.25F, 16.6F, 0.0F));
		partdefinition.addOrReplaceChild("tentacle5", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.75F, 15.6F, 0.0F));
		partdefinition.addOrReplaceChild("tentacle6", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.75F, 14.6F, 3.0F));
		partdefinition.addOrReplaceChild("tentacle7", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.25F, 15.6F, 3.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(LivingEntityRenderState state) {
		// Resets every part to its default pose first
		super.setupAnim(state);

		// Looks where it's facing, like the blaze's head
		this.head.yRot = state.yRot * ((float) Math.PI / 180F);
		this.head.xRot = state.xRot * ((float) Math.PI / 180F);

		// Tentacles sway like the ghast's
		GhastModel.animateTentacles(state, this.tentacles);
	}
}