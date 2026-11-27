package net.nullcoil.soulscorch.entity.ai.ghaze;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.entity.ai.Restless;
import net.nullcoil.soulscorch.entity.ai.soulless.SoullessEntity;
import net.nullcoil.soulscorch.entity.client.soulless.SoullessActivity;
import net.nullcoil.soulscorch.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The Ghaze's four eyes, one per side, each with a mind of its own. Client only, and only for
 * show: what an eye looks at never Witnesses anyone or changes what the Ghaze does.
 *
 * <ul>
 *   <li><b>Calm:</b> each eye glances at things of interest on its own side that it has a clear
 *       view of (Viscous Violet Corts, and any entity: Soulless and Restless only while
 *       they're awake, players only now and then, never a Heedless), or just looks around.</li>
 *   <li><b>Watching:</b> every eye follows its target.</li>
 *   <li><b>Searching:</b> every eye darts around.</li>
 * </ul>
 *
 * Movement is like real eyes: looking around is quick jumps (saccades) with pauses in between;
 * following something that moves is a slow, smooth track.
 *
 * <p>Pupil offsets are in pixels on the eye's face (x across, y down), as the model uses them.
 */
public class GhazeEyes {
    public static final int EYES = 4;
    /** How far a pupil slides across its face from the center, in pixels (the face is 8 wide). */
    public static final float PUPIL_RANGE = 2.0F;

    /** How far it notices things, in blocks. */
    private static final double INTEREST_RANGE = 16.0;
    /** How far around it looks for Corts, in blocks, and how often (2 seconds). */
    private static final int CORT_SCAN_RADIUS = 8;
    private static final int CORT_SCAN_INTERVAL = 40;
    /** Most an eye can look off to the side, or up and down, of straight out of its face. */
    private static final float EYE_REACH_SIDE = 80.0F * Mth.DEG_TO_RAD;
    private static final float EYE_REACH_UP = 70.0F * Mth.DEG_TO_RAD;
    /** Chance, when an eye is done with what it was doing, that it looks for something of interest. */
    private static final float INTEREST_CHANCE = 0.6F;
    /** Chance a player in view counts as interesting this time ("every so often"). */
    private static final float PLAYER_CHANCE = 0.3F;

    /** Saccade: covers this share of the remaining distance each tick (there in 2 to 3 ticks). */
    private static final float SACCADE = 0.65F;
    /** Smooth pursuit: share of the remaining distance per tick, and the fastest it goes (pixels per tick). */
    private static final float PURSUIT = 0.12F;
    private static final float PURSUIT_MAX = 0.2F;

    private static final float HALF_PI = (float) (Math.PI / 2.0);

    public final float[] pupilX = new float[EYES];
    public final float[] pupilY = new float[EYES];
    public final float[] pupilXO = new float[EYES];
    public final float[] pupilYO = new float[EYES];

    private final Eye[] eyes = new Eye[EYES];
    private final List<BlockPos> corts = new ArrayList<>();
    private int cortScanCooldown;

    public GhazeEyes() {
        for (int i = 0; i < EYES; i++) this.eyes[i] = new Eye();
    }

    private static final class Eye {
        /** Ticks until it picks something new to do. */
        int hold;
        /** What it's looking at: an entity, or a block (a Cort), or neither (looking around). */
        @Nullable Entity entity;
        @Nullable BlockPos block;
        /** Where it's looking while just looking around, as a pupil offset. */
        float wanderX;
        float wanderY;
        /** Jumping to a new spot (fast) rather than following (slow). */
        boolean saccade;
    }

    /** Called every client tick, after the head has turned and tilted. */
    public void tick(Ghaze ghaze) {
        Level level = ghaze.level();
        RandomSource random = ghaze.getRandom();
        Vec3 head = ghaze.getHeadPosition(1.0F);

        boolean agitated = ghaze.isAgitated();
        boolean searching = ghaze.isSearching();
        Entity watched = agitated && !searching ? level.getEntity(ghaze.getGazeTargetId()) : null;

        if (!agitated && --this.cortScanCooldown <= 0) {
            this.cortScanCooldown = CORT_SCAN_INTERVAL;
            this.scanCorts(level, ghaze.blockPosition());
        }

        for (int i = 0; i < EYES; i++) {
            Eye eye = this.eyes[i];
            this.pupilXO[i] = this.pupilX[i];
            this.pupilYO[i] = this.pupilY[i];

            float[] want;
            if (watched != null) {
                // Watching: every eye on the target, tracking smoothly (a quick jump to it first)
                if (eye.entity != watched) {
                    eye.entity = watched;
                    eye.block = null;
                    eye.saccade = true;
                }
                want = this.pupilFor(ghaze, i, watched.getEyePosition().subtract(head));
            } else {
                if (agitated && eye.entity != null) {
                    eye.entity = null; // searching: drop whatever it was on
                    eye.block = null;
                    eye.hold = 0;
                }
                if (--eye.hold <= 0 || !this.stillInterested(ghaze, i, eye, head)) {
                    this.choose(ghaze, i, eye, head, searching, random);
                }
                Vec3 point = eye.entity != null ? eye.entity.getEyePosition()
                        : eye.block != null ? Vec3.atCenterOf(eye.block) : null;
                want = point != null ? this.pupilFor(ghaze, i, point.subtract(head)) : new float[]{eye.wanderX, eye.wanderY};
            }

            this.move(i, eye, want[0], want[1]);
        }
    }

    /** Picks what an eye does next: something of interest on its side, or a glance somewhere. */
    private void choose(Ghaze ghaze, int i, Eye eye, Vec3 head, boolean searching, RandomSource random) {
        eye.entity = null;
        eye.block = null;
        eye.saccade = true;

        if (!searching && random.nextFloat() < INTEREST_CHANCE) {
            List<Object> options = new ArrayList<>();
            AABB area = ghaze.getBoundingBox().inflate(INTEREST_RANGE);
            for (Entity entity : ghaze.level().getEntities(ghaze, area, GhazeEyes::interesting)) {
                if (entity instanceof Player && random.nextFloat() >= PLAYER_CHANCE) continue;
                if (this.canSee(ghaze, i, head, entity.getEyePosition())) options.add(entity);
            }
            for (BlockPos pos : this.corts) {
                if (this.canSee(ghaze, i, head, Vec3.atCenterOf(pos))) options.add(pos);
            }
            if (!options.isEmpty()) {
                Object pick = options.get(random.nextInt(options.size()));
                if (pick instanceof Entity entity) {
                    eye.entity = entity;
                    eye.hold = Mth.nextInt(random, 30, 80); // 1.5 to 4 seconds
                } else {
                    eye.block = (BlockPos) pick;
                    eye.hold = Mth.nextInt(random, 20, 60); // 1 to 3 seconds
                }
                return;
            }
        }

        // Look somewhere else: a quick jump, then a pause. Searching, they dart: short pauses
        // and wide jumps.
        float reach = searching ? 1.0F : 0.75F;
        eye.wanderX = (random.nextFloat() * 2.0F - 1.0F) * reach * PUPIL_RANGE;
        eye.wanderY = (random.nextFloat() * 2.0F - 1.0F) * reach * 0.8F * PUPIL_RANGE;
        eye.hold = searching ? Mth.nextInt(random, 3, 10) : Mth.nextInt(random, 8, 35);
    }

    /** What catches its eye: any entity, except the Heedless, and Soulless and Restless only
     *  while they're awake. Corts are found separately (see scanCorts). */
    private static boolean interesting(Entity entity) {
        if (!entity.isAlive()) return false;
        if (entity.is(ModTags.Entities.UNINTERESTING)) return false;
        if (entity instanceof SoullessEntity soulless) return soulless.getActivity() != SoullessActivity.PASSIVE;
        if (entity instanceof Restless restless) return restless.getAwakened();
        return true;
    }

    /** Whether it's still worth looking at: alive, in range, and still in view of this eye. */
    private boolean stillInterested(Ghaze ghaze, int i, Eye eye, Vec3 head) {
        if (eye.entity != null) {
            if (eye.entity.isRemoved() || !interesting(eye.entity)) return false;
            if (eye.entity.distanceToSqr(ghaze) > INTEREST_RANGE * INTEREST_RANGE) return false;
            return this.inView(ghaze, i, eye.entity.getEyePosition().subtract(head));
        }
        if (eye.block != null) {
            return ghaze.level().getBlockState(eye.block).is(ModBlocks.VISCOUS_VIOLET_CORT);
        }
        return true;
    }

    /** Whether eye {@code i} can see a point: on its side of the head, and nothing in the way. */
    private boolean canSee(Ghaze ghaze, int i, Vec3 head, Vec3 point) {
        if (!this.inView(ghaze, i, point.subtract(head))) return false;
        HitResult hit = ghaze.level().clip(new ClipContext(head, point,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ghaze));
        return hit.getType() == HitResult.Type.MISS;
    }

    private boolean inView(Ghaze ghaze, int i, Vec3 toPoint) {
        float[] angles = this.anglesFor(ghaze, i, toPoint);
        return Math.abs(angles[0]) <= EYE_REACH_SIDE && Math.abs(angles[1]) <= EYE_REACH_UP;
    }

    private float[] pupilFor(Ghaze ghaze, int i, Vec3 toPoint) {
        float[] angles = this.anglesFor(ghaze, i, toPoint);
        return new float[]{
                Mth.clamp(angles[0] / HALF_PI, -1.0F, 1.0F) * PUPIL_RANGE,
                Mth.clamp(angles[1] / HALF_PI, -1.0F, 1.0F) * PUPIL_RANGE
        };
    }

    /**
     * Where a point is as eye {@code i} sees it: radians off to the side (toward the face's +x)
     * and down (toward +y), the way the model lays the eyes out. Uses the head's turn and
     * tilt (not its small wobble).
     */
    private float[] anglesFor(Ghaze ghaze, int i, Vec3 toPoint) {
        double horizontal = Math.sqrt(toPoint.x * toPoint.x + toPoint.z * toPoint.z);
        float yaw = (float) (Mth.atan2(toPoint.z, toPoint.x) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) -Mth.atan2(toPoint.y, horizontal); // radians, positive: below
        float relative = Mth.wrapDegrees(yaw - ghaze.headYaw) * Mth.DEG_TO_RAD;

        // In the head's frame (front is -z, model y points down, relative yaw +90 is -x)
        Vector3f v = new Vector3f(-Mth.sin(relative) * Mth.cos(pitch), Mth.sin(pitch), -Mth.cos(relative) * Mth.cos(pitch));
        // Undo the skull's tilt, then turn into this eye's socket (it looks out along -z)
        new Quaternionf().rotationZYX(ghaze.tiltZ, 0.0F, ghaze.tiltX).conjugate().transform(v);
        new Quaternionf().rotationY(-i * HALF_PI).transform(v);

        float side = (float) Mth.atan2(v.x, -v.z);
        float down = (float) Mth.atan2(v.y, Math.sqrt(v.x * v.x + v.z * v.z));
        return new float[]{side, down};
    }

    /** Moves a pupil toward where it wants to be: a quick jump, or a slow smooth track. */
    private void move(int i, Eye eye, float wantX, float wantY) {
        float dx = wantX - this.pupilX[i];
        float dy = wantY - this.pupilY[i];
        if (eye.saccade) {
            this.pupilX[i] += dx * SACCADE;
            this.pupilY[i] += dy * SACCADE;
            if (dx * dx + dy * dy < 0.01F) eye.saccade = false; // landed: follow from here on
        } else {
            float stepX = dx * PURSUIT;
            float stepY = dy * PURSUIT;
            float length = Mth.sqrt(stepX * stepX + stepY * stepY);
            if (length > PURSUIT_MAX) {
                stepX *= PURSUIT_MAX / length;
                stepY *= PURSUIT_MAX / length;
            }
            this.pupilX[i] += stepX;
            this.pupilY[i] += stepY;
        }
    }

    /** Finds the Corts around it (they hang from the ceiling, so it'll often glance up at them). */
    private void scanCorts(Level level, BlockPos center) {
        this.corts.clear();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-CORT_SCAN_RADIUS, -CORT_SCAN_RADIUS, -CORT_SCAN_RADIUS),
                center.offset(CORT_SCAN_RADIUS, CORT_SCAN_RADIUS, CORT_SCAN_RADIUS))) {
            if (level.getBlockState(pos).is(ModBlocks.VISCOUS_VIOLET_CORT)) {
                this.corts.add(pos.immutable());
                if (this.corts.size() >= 16) return;
            }
        }
    }
}
