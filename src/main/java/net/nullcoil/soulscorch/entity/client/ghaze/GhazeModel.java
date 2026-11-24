package net.nullcoil.soulscorch.entity.client.ghaze;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.Soulscorch;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GhazeModel extends EntityModel<GhazeRenderState> {
	public static final ModelLayerLocation GHAZE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghaze"), "main");

	/** Lean per pixel of a tentacle's sideways speed: how hard the bottom trails behind. */
	private static final float TRAIL = 0.6F;
	/** Outward lean per pixel of distance from the center, per (radian per tick) squared of spin. */
	private static final float FLARE = 2.0F;
	/** How far the head wobbles, in radians (about 4 degrees; half that while agitated). */
	private static final float WOBBLE = 0.07F;
	/** How fast it wobbles: one sway about every 4 seconds. */
	private static final float WOBBLE_SPEED = 0.08F;
	/** How far the wind sways the tentacles, in radians (about 7 degrees, up to twice that in gusts). */
	private static final float WIND = 0.12F;
	/** Most a tentacle leans while spinning, in radians (about 57 degrees). */
	private static final float MAX_LEAN = 1.0F;

	/** How far a pupil slides across its face from the center, in pixels (the face is 8 wide). */
	private static final float PUPIL_RANGE = 2.0F;
	private static final float HALF_PI = (float) (Math.PI / 2.0);

	/** Turns (yaw). */
	private final ModelPart head;
	/** Tilts toward whichever side faces the target, and wobbles. Holds the head cube and eyes. */
	private final ModelPart skull;
	/** One eye per side: front, then counterclockwise seen from above (each socket turned 90 degrees more). */
	private final ModelPart[] eyes = new ModelPart[4];
	private final ModelPart[] tentacles = new ModelPart[8];

	public GhazeModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.skull = this.head.getChild("skull");
		for (int i = 0; i < this.eyes.length; i++) {
			this.eyes[i] = this.skull.getChild("eye_socket" + i).getChild("eye");
		}
		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i] = root.getChild("tentacle" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// The head turns, the skull inside it tilts: tilting a separate part after the turn means
		// it can tip toward any side, not just the front
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));
		PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

		// An eye on each side, like the guardian's: a 2x2 cube poking a quarter pixel out of the
		// face. Each sits in a socket at the head's center turned to face its side, so moving
		// the eye along its socket's x and y slides it across that face.
		for (int i = 0; i < 4; i++) {
			PartDefinition socket = skull.addOrReplaceChild("eye_socket" + i, CubeListBuilder.create(), PartPose.rotation(0.0F, i * HALF_PI, 0.0F));
			socket.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, -4.25F));
		}

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

		// Turns only as far as its nearest eye needs to see (worked out in GhazeEntity.tickHead),
		// and tilts toward whichever side the target is on
		this.head.yRot = state.yRot * ((float) Math.PI / 180F);
		this.skull.xRot = state.tiltX;
		this.skull.zRot = state.tiltZ;

		// A gentle wobble on top of the look: slow, out-of-step sways on all three axes, so it
		// bobs like it's floating rather than nodding in a pattern. Each Ghaze is out of step
		// with the others (windSeed), and it holds steadier while it's glaring (agitated).
		float wobbleTime = state.ageInTicks * WOBBLE_SPEED + state.windSeed;
		float wobble = WOBBLE * (1.0F - 0.5F * state.agitation);
		this.skull.zRot += wobble * Mth.sin(wobbleTime);
		this.skull.xRot += wobble * 0.6F * Mth.sin(wobbleTime * 0.77F + 1.3F);
		this.head.yRot += wobble * 0.5F * Mth.sin(wobbleTime * 0.53F + 2.6F);

		this.setupEyes(state);

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

			float leanX = -z * swing * TRAIL + x * swing * swing * FLARE;
			float leanZ = x * swing * TRAIL + z * swing * swing * FLARE;

			float s = seed + i * 1.7F;
			leanX += windX + gust * WIND * (Mth.sin(time * 0.11F + s) + 0.5F * Mth.sin(time * 0.23F + s * 2.3F));
			leanZ += windZ + gust * WIND * (Mth.sin(time * 0.09F + s * 1.3F) + 0.5F * Mth.sin(time * 0.19F + s * 3.1F));

			float idleXRot = Mth.clamp(leanZ, -MAX_LEAN, MAX_LEAN);
			float idleZRot = Mth.clamp(-leanX, -MAX_LEAN, MAX_LEAN);

			tentacle.xRot = spin * idleXRot + state.wiggle * tentacle.xRot;
			tentacle.zRot = spin * idleZRot;
		}
	}

	/**
	 * Each eye moves on its own. While it can see its target, every eye looks toward them from
	 * its own side (the ones facing away roll to the edge nearest them). Calm, they drift
	 * around lazily, each in its own direction; searching, they dart about.
	 */
	private void setupEyes(GhazeRenderState state) {
		Vec3 look = state.lookTarget;
		if (look != null) {
			// The target, seen from inside the skull. The renderer turns the model by
			// 180 - bodyRot around Y and flips x and y (scale -1, -1, 1), so undo that, then
			// undo the head's turn and the skull's tilt, exactly as the parts are posed.
			Vector3f v = new Vector3f((float) look.x, (float) look.y, (float) look.z);
			new Quaternionf().rotationY(-(180.0F - state.bodyRot) * Mth.DEG_TO_RAD).transform(v);
			v.set(-v.x, -v.y, v.z);
			partRotation(this.head).conjugate().transform(v);
			partRotation(this.skull).conjugate().transform(v);

			for (int i = 0; i < this.eyes.length; i++) {
				// Into this eye's socket: it looks out along -z
				Vector3f e = new Quaternionf().rotationY(-i * HALF_PI).transform(new Vector3f(v));
				float side = (float) Mth.atan2(e.x, -e.z) / HALF_PI;
				float up = (float) Mth.atan2(e.y, Math.sqrt(e.x * e.x + e.z * e.z)) / HALF_PI;
				this.eyes[i].x = Mth.clamp(side, -1.0F, 1.0F) * PUPIL_RANGE;
				this.eyes[i].y = Mth.clamp(up, -1.0F, 1.0F) * PUPIL_RANGE;
			}
		} else {
			float speed = state.searching ? 0.35F : 0.05F;
			float reach = state.searching ? 1.0F : 0.6F;
			float t = state.ageInTicks * speed + state.windSeed;
			for (int i = 0; i < this.eyes.length; i++) {
				float s = i * 2.39F; // out of step with each other
				float side = 0.7F * Mth.sin(t + s) + 0.3F * Mth.sin(t * 2.3F + s * 1.7F);
				float up = 0.6F * Mth.sin(t * 0.8F + s * 1.3F + 1.0F) + 0.4F * Mth.sin(t * 1.9F + s * 0.7F);
				this.eyes[i].x = side * reach * PUPIL_RANGE;
				this.eyes[i].y = up * reach * PUPIL_RANGE;
			}
		}
	}

	/** A part's rotation, built the same way ModelPart applies it. */
	private static Quaternionf partRotation(ModelPart part) {
		return new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot);
	}
}
