package net.nullcoil.soulscorch.entity.client.ghaze;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nullcoil.soulscorch.Soulscorch;

public class GhazeModel extends EntityModel<GhazeRenderState> {
	public static final ModelLayerLocation GHAZE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghaze"), "main");

	/** Lean per pixel of a tentacle's sideways speed: how hard the bottom trails behind. */
	private static final float TRAIL = 0.6F;
	/** Outward lean per pixel of distance from the center, per (radian per tick) squared of spin. */
	private static final float FLARE = 2.0F;
	/** How far the wind sways the tentacles, in radians (about 7 degrees, up to twice that in gusts). */
	private static final float WIND = 0.12F;
	/** Most a tentacle leans while spinning, in radians (about 57 degrees). */
	private static final float MAX_LEAN = 1.0F;

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
	public void setupAnim(GhazeRenderState state) {
		// Resets every part to its default pose first
		super.setupAnim(state);

		// Looks where it's facing, like the blaze's head
		this.head.yRot = state.yRot * ((float) Math.PI / 180F);
		this.head.xRot = state.xRot * ((float) Math.PI / 180F);

		// The ghast wiggle, for blending in below
		GhastModel.animateTentacles(state, this.tentacles);

		float spin = 1.0F - state.wiggle;
		float swing = state.tentacleSwing;
		float cos = Mth.cos(state.spinAngle);
		float sin = Mth.sin(state.spinAngle);

		// Wind: gusts come and go slowly, and its direction drifts; all tentacles share both
		float time = state.ageInTicks;
		float seed = state.windSeed;
		float gust = 0.6F + 0.4F * Mth.sin(time * 0.031F + seed);
		float windX = 0.6F * WIND * Mth.cos(time * 0.013F + seed * 1.9F);
		float windZ = 0.6F * WIND * Mth.sin(time * 0.013F + seed * 1.9F);

		for (int i = 0; i < this.tentacles.length; i++) {
			ModelPart tentacle = this.tentacles[i];
			// Orbit the center: rotate the pivot (where it meets the head) around the Ghaze
			float x = tentacle.x * cos + tentacle.z * sin;
			float z = -tentacle.x * sin + tentacle.z * cos;
			tentacle.x = x;
			tentacle.z = z;

			// Rope physics, without bending: the pivot gets pulled along its circle at
			// (z, -x) * swing, so the far end trails behind it, and spinning flings the end
			// outward along (x, z). The result is how far the bottom sits off to the side.
			float leanX = -z * swing * TRAIL + x * swing * swing * FLARE;
			float leanZ = x * swing * TRAIL + z * swing * swing * FLARE;

			// Each tentacle also sways on its own, like a wind chime: two out-of-step waves per
			// direction, at different speeds per tentacle, so they never move in unison
			float s = seed + i * 1.7F;
			leanX += windX + gust * WIND * (Mth.sin(time * 0.11F + s) + 0.5F * Mth.sin(time * 0.23F + s * 2.3F));
			leanZ += windZ + gust * WIND * (Mth.sin(time * 0.09F + s * 1.3F) + 0.5F * Mth.sin(time * 0.19F + s * 3.1F));

			// Positive xRot swings the bottom toward +z; positive zRot swings it toward -x
			float idleXRot = Mth.clamp(leanZ, -MAX_LEAN, MAX_LEAN);
			float idleZRot = Mth.clamp(-leanX, -MAX_LEAN, MAX_LEAN);

			tentacle.xRot = spin * idleXRot + state.wiggle * tentacle.xRot;
			tentacle.zRot = spin * idleZRot;
		}
	}
}
