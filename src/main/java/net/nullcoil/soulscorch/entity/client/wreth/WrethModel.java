package net.nullcoil.soulscorch.entity.client.wreth;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nullcoil.soulscorch.Soulscorch;

/**
 * The Wreth: a small six-legged crawler with a segmented, curled tail ending in a stinger.
 * Converted from {@code Wreth.bbmodel} (32x32 texture, {@code textures/entity/wreth.png}).
 *
 * <p>Uses the plain living-entity render state for now; swap it for a {@code WrethRenderState}
 * once the Wreth needs its own (latched on, clinging to the ceiling).
 */
public class WrethModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation WRETH = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "wreth"), "main");

	/** How far each leg sweeps forward and back while walking, in radians (about 23 degrees). */
	private static final float LEG_SWING = 0.4F;
	/** How far a leg lifts on its forward sweep, in radians. */
	private static final float LEG_LIFT = 0.3F;
	/** How fast the legs cycle per block walked. */
	private static final float LEG_SPEED = 1.6F;
	/** How far the tail sways side to side at rest, in radians (about 6 degrees). */
	private static final float TAIL_SWAY = 0.1F;

	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart secondSegment;
	private final ModelPart thirdSegment;
	/**
	 * Legs in the two sets that step together, like an insect's: front and back on one side
	 * with the middle on the other.
	 */
	private final ModelPart[] stepA;
	private final ModelPart[] stepB;
	/** +1 for legs on the left (they sweep the other way round), -1 for the right; same order as the sets. */
	private static final float[] SIDE_A = {1.0F, -1.0F, 1.0F};
	private static final float[] SIDE_B = {-1.0F, 1.0F, -1.0F};

	public WrethModel(ModelPart root) {
		super(root);
		ModelPart body = root.getChild("body");
		this.head = body.getChild("head");
		this.tail = body.getChild("tail");
		this.secondSegment = this.tail.getChild("second_segment");
		this.thirdSegment = this.secondSegment.getChild("third_segment");
		this.stepA = new ModelPart[]{body.getChild("left_leg_front"), body.getChild("right_leg_mid"), body.getChild("left_leg_back")};
		this.stepB = new ModelPart[]{body.getChild("right_leg_front"), body.getChild("left_leg_mid"), body.getChild("right_leg_back")};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();

		// Everything hangs off the body, which sits on the ground (its bottom at y 24)
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 24.0F, -1.0F));

		// Pivot moved to the head's center line (it was a pixel to one side in Blockbench), so it
		// turns in place instead of swinging off-center. The cube is where it was.
		body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 9).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 0.0F, -2.0F));

		// The tail curls up in three segments, each tipped further than the one before
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 2.0F), PartPose.offset(0.0F, -2.5F, 3.0F));
		PartDefinition secondSegment = tail.addOrReplaceChild("second_segment", CubeListBuilder.create().texOffs(0, 20).addBox(-1.5F, -1.25F, 0.0F, 3.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -0.25F, 1.5F, 0.2618F, 0.0F, 0.0F));
		PartDefinition thirdSegment = secondSegment.addOrReplaceChild("third_segment", CubeListBuilder.create().texOffs(0, 25).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.2618F, 0.0F, 0.0F));
		thirdSegment.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(0, 29).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -0.5F, 1.5F, 0.6109F, 0.0F, 0.0F));

		// Legs pivot where they meet the body. Left is +x in model space (the mob faces -z)
		body.addOrReplaceChild("left_leg_front", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.0F, -0.5F, -0.5F, 0.0F, 0.0F, 0.3491F));
		body.addOrReplaceChild("left_leg_mid", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.0F, -0.5F, 1.5F, 0.0F, 0.0F, 0.3491F));
		body.addOrReplaceChild("left_leg_back", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(1.75F, -0.5F, 3.25F, -0.3295F, -0.7268F, 0.4754F));
		body.addOrReplaceChild("right_leg_front", CubeListBuilder.create().texOffs(26, 0).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, -0.5F, -0.5F, 0.0F, 0.0F, -0.3491F));
		body.addOrReplaceChild("right_leg_mid", CubeListBuilder.create().texOffs(26, 0).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, -0.5F, 1.5F, 0.0F, 0.0F, -0.3491F));
		body.addOrReplaceChild("right_leg_back", CubeListBuilder.create().texOffs(26, 0).addBox(-1.8535F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-2.0F, -0.5F, 3.25F, -0.3295F, 0.7268F, -0.4754F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(LivingEntityRenderState state) {
		// Resets every part to its default pose first
		super.setupAnim(state);

		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

		// Scuttle: the two sets of legs sweep opposite ways, each lifting on its forward sweep
		float phase = state.walkAnimationPos * LEG_SPEED;
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);
		this.step(this.stepA, SIDE_A, phase, speed);
		this.step(this.stepB, SIDE_B, phase + Mth.PI, speed);

		// The tail sways a little at rest, the tip trailing behind the base
		float time = state.ageInTicks * 0.08F;
		this.tail.yRot = TAIL_SWAY * Mth.sin(time);
		this.secondSegment.yRot = TAIL_SWAY * 0.6F * Mth.sin(time - 0.6F);
		this.thirdSegment.yRot = TAIL_SWAY * 0.4F * Mth.sin(time - 1.2F);
	}

	private void step(ModelPart[] legs, float[] sides, float phase, float speed) {
		float sweep = Mth.cos(phase) * LEG_SWING * speed;
		float lift = Math.max(0.0F, Mth.sin(phase)) * LEG_LIFT * speed;
		for (int i = 0; i < legs.length; i++) {
			legs[i].yRot += sides[i] * sweep;
			legs[i].zRot += sides[i] * lift;
		}
	}
}
