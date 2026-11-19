package net.nullcoil.soulscorch.entity.ai;

import it.crystalnest.prometheus.api.FireManager;
import it.crystalnest.prometheus.api.type.FireTyped;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GhazeEntity extends Monster implements SoulAlarm.Listener, FireTyped {
    /** Whether it has a target. Synced so the client can swap the texture and stop the idle spin. */
    private static final EntityDataAccessor<Boolean> DATA_AGITATED =
            SynchedEntityData.defineId(GhazeEntity.class, EntityDataSerializers.BOOLEAN);
    /** Entity id of who it's glaring at (-1: nobody). Synced so that player's camera gets pulled. */
    private static final EntityDataAccessor<Integer> DATA_GAZE_TARGET =
            SynchedEntityData.defineId(GhazeEntity.class, EntityDataSerializers.INT);

    /** Height of the head's pivot (its center) above the Ghaze's feet: 15 pixels. */
    public static final double HEAD_HEIGHT = 15.0 / 16.0;
    /** Loses interest in a target farther away than this many blocks. */
    private static final double FORGET_DISTANCE = 48.0;

    // ---- Tentacles (client only; see tickTentacles) ----
    /** How long one full turn takes, in ticks (2 seconds). */
    private static final int SPIN_DURATION = 40;
    /** Wait between turns while idle, in ticks (5 to 15 seconds). */
    private static final int SPIN_COOLDOWN_MIN = 100;
    private static final int SPIN_COOLDOWN_MAX = 300;
    /** The tentacles' lag behind the spin is a damped spring, so they overshoot a little when it stops. */
    private static final float SWING_STIFFNESS = 0.2F;
    private static final float SWING_DAMPING = 0.75F;
    /** Horizontal movement per tick (squared) that counts as moving, about 0.01 blocks a tick. */
    private static final double MOVING_THRESHOLD = 1.0E-4;
    private static final float TWO_PI = (float) (Math.PI * 2.0);
    /** Agitated: the tentacles spin nonstop, winding up to this speed (radians per tick, about
     *  a turn every half second)... */
    private static final float HOSTILE_MAX_SPIN = 0.4F;
    /** ...gaining this much speed a tick (6 seconds from rest to full speed)... */
    private static final float HOSTILE_SPIN_ACCEL = 0.005F;
    /** ...and losing this much a tick once it calms down (1.5 seconds from full speed). */
    private static final float HOSTILE_SPIN_DECEL = 0.02F;
    /** How fast the agitation fade (the calm glow fading out) follows, per tick. */
    private static final float AGITATION_FADE = 0.04F;

    public float spinAngle;
    public float spinAngleO;
    /** The spin speed as the tentacles feel it: trails the real speed and swings past it. Drives the lean. */
    public float tentacleSwing;
    private float tentacleSwingVelocity;
    /** 0 while idle (wind and turns), 1 while moving or agitated (ghast wiggle), eased between. */
    public float wiggle;
    /** 0 calm, 1 agitated, eased between (about 1.5 seconds). Fades out the calm glow. */
    public float agitation;
    private float hostileSpin;
    private float hostileDirection = 1.0F;
    private boolean wasAgitated;
    /** Where the current idle turn started, so it can start from wherever the tentacles are. */
    private float spinStart;
    /** Ticks into the current turn, or -1 when not turning. */
    private int spinTicks = -1;
    private float spinDirection = 1.0F;
    private int spinCooldown = SPIN_COOLDOWN_MIN;

    public GhazeEntity(EntityType<? extends GhazeEntity> type, Level level) {
        super(type, level);
        // Floats around like a ghast (pushed toward where it wants to go every few ticks)
        this.moveControl = new GhazeMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.FLYING_SPEED, 0.035D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AGITATED, false);
        builder.define(DATA_GAZE_TARGET, -1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new WatchGoal(this));
        this.goalSelector.addGoal(5, new DriftGoal(this));
    }

    /** Flies the way a ghast does: no gravity, air drag only. */
    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.02F);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    public boolean isAgitated() {
        return this.entityData.get(DATA_AGITATED);
    }

    /** Entity id of who it's glaring at, or -1. */
    public int getGazeTargetId() {
        return this.entityData.get(DATA_GAZE_TARGET);
    }

    /** Where its eyes look out from: the center of its head. */
    public Vec3 getHeadPosition(float partialTick) {
        return this.getPosition(partialTick).add(0.0, HEAD_HEIGHT, 0.0);
    }

    /** Burns while agitated, like a blaze while it attacks (fire-immune, so it's only for show). */
    @Override
    public boolean isOnFire() {
        return this.isAgitated();
    }

    /**
     * Its flames are always soul fire. Prometheus adds this method to every entity and asks it
     * which flames to draw; overriding it here means nothing has to be set or synced. (Setting
     * the type through Prometheus wouldn't work anyway: it ignores fire-immune entities.)
     */
    @Override
    public Identifier getFireType() {
        return FireManager.SOUL_FIRE_TYPE;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            // Lets go of targets that died, went into creative, or got away
            if (target != null && (!target.isAlive() || !this.canAttack(target)
                    || this.distanceToSqr(target) > FORGET_DISTANCE * FORGET_DISTANCE)) {
                this.setTarget(null);
                target = null;
            }

            boolean agitated = target != null;
            this.entityData.set(DATA_AGITATED, agitated);
            this.entityData.set(DATA_GAZE_TARGET, agitated ? target.getId() : -1);

        } else {
            this.tickTentacles();
        }
    }

    /**
     * Calm and still: the tentacles dangle in the wind (that part is in the model), and every
     * 5 to 15 seconds make one full turn around the Ghaze, left or right at random.
     * Calm and moving: they wiggle like a ghast's.
     * Agitated: they spin nonstop in one direction, winding up faster and faster, and the
     * rope lean flings them outward. Once calm again they wind down and dangle.
     */
    private void tickTentacles() {
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        boolean moving = dx * dx + dz * dz > MOVING_THRESHOLD;
        boolean agitated = this.isAgitated();
        boolean idle = !moving && !agitated;

        this.spinAngleO = this.spinAngle;

        // ---- Agitated spin: winds up, and winds down once calm ----
        if (agitated && !this.wasAgitated) {
            this.hostileDirection = this.random.nextBoolean() ? 1.0F : -1.0F;
            this.spinTicks = -1; // drop any idle turn; the spin takes over from wherever it is
        }
        this.wasAgitated = agitated;
        this.hostileSpin = agitated
                ? Math.min(HOSTILE_MAX_SPIN, this.hostileSpin + HOSTILE_SPIN_ACCEL)
                : Math.max(0.0F, this.hostileSpin - HOSTILE_SPIN_DECEL);
        this.spinAngle += this.hostileDirection * this.hostileSpin;

        // ---- Idle turns: one full turn every so often, only once fully wound down ----
        if (this.spinTicks >= 0) {
            this.spinTicks++;
            float t = Math.min(this.spinTicks / (float) SPIN_DURATION, 1.0F);
            float eased = (1.0F - Mth.cos(t * (float) Math.PI)) / 2.0F;
            this.spinAngle = this.spinStart + this.spinDirection * TWO_PI * eased;
            if (t >= 1.0F) {
                this.spinTicks = -1;
                this.spinCooldown = Mth.nextInt(this.random, SPIN_COOLDOWN_MIN, SPIN_COOLDOWN_MAX);
            }
        } else if (idle && this.hostileSpin <= 0.0F && --this.spinCooldown <= 0) {
            this.spinTicks = 0;
            this.spinStart = this.spinAngle;
            this.spinDirection = this.random.nextBoolean() ? 1.0F : -1.0F;
        }

        float spinSpeed = this.spinAngle - this.spinAngleO;

        // Keep the angle small so it never loses float precision; shift everything that
        // remembers an angle together, so nothing visibly jumps
        float wrap = this.spinAngle > TWO_PI ? -TWO_PI : this.spinAngle < -TWO_PI ? TWO_PI : 0.0F;
        this.spinAngle += wrap;
        this.spinAngleO += wrap;
        this.spinStart += wrap;

        this.tentacleSwingVelocity += (spinSpeed - this.tentacleSwing) * SWING_STIFFNESS;
        this.tentacleSwingVelocity *= SWING_DAMPING;
        this.tentacleSwing += this.tentacleSwingVelocity;

        // The ghast wiggle is only for moving while calm; agitated, the spin takes over
        this.wiggle += ((moving && !agitated ? 1.0F : 0.0F) - this.wiggle) * 0.1F;
        this.agitation += ((agitated ? 1.0F : 0.0F) - this.agitation) * AGITATION_FADE;
    }

    /** A Soulless nearby turned hostile: if it turned on a player, the Ghaze takes notice of them too. */
    @Override
    public void onSoulAlarm(LivingEntity source, LivingEntity target) {
        if (!(target instanceof Player)) return;
        if (this.getTarget() != null && this.getTarget().isAlive()) return; // already watching someone
        if (!this.canAttack(target)) return; // creative, spectator, peaceful...
        this.setTarget(target);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    /**
     * Vanilla's ghast movement, copied and adjusted: the same push (FLYING_SPEED * 5/3 every 2
     * to 6 ticks), but it slows down as it arrives and stops cleanly.
     *
     * <ul>
     *   <li>Vanilla's path check sweeps the hitbox from where it is to where it's going, and a
     *       hitbox resting on a block counts as already hitting it, so every trip failed before
     *       it started (ghasts never touch the ground, so vanilla never notices). This one
     *       shrinks the hitbox a hair first and steps along the way a block at a time.</li>
     *   <li>Vanilla never stops pushing toward its wanted spot, so asking it to stop where it is
     *       while still moving makes it overshoot, get pushed back, and rock back and forth.
     *       Here {@link #stop()} stops pushing entirely, and it brakes to a halt.</li>
     *   <li>Within {@link #ARRIVE_DISTANCE} of the spot, the push shrinks and it brakes, so it
     *       eases in instead of sailing past.</li>
     * </ul>
     */
    static class GhazeMoveControl extends MoveControl<GhazeEntity> {
        /** Starts easing in this many blocks from where it's going. */
        private static final double ARRIVE_DISTANCE = 2.5;
        /** Share of its speed kept each tick while braking (air drag alone keeps 0.91). */
        private static final double BRAKE = 0.85;
        /** How long it brakes after being told to stop (1 second). */
        private static final int BRAKE_TICKS = 20;

        private int floatDuration;
        private int brakeTicks;

        GhazeMoveControl(GhazeEntity ghaze) {
            super(ghaze);
        }

        /** Stop pushing and brake to a halt where it is. */
        public void stop() {
            this.operation = Operation.WAIT;
            this.brakeTicks = BRAKE_TICKS;
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speed) {
            super.setWantedPosition(x, y, z, speed);
            this.brakeTicks = 0;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO) {
                if (this.brakeTicks > 0) {
                    this.brakeTicks--;
                    this.brake();
                }
                return;
            }

            Vec3 travel = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            double distance = travel.length();
            if (distance < ARRIVE_DISTANCE) this.brake();

            if (this.floatDuration-- > 0) return;
            this.floatDuration += this.mob.getRandom().nextInt(5) + 2;

            if (this.canReach(travel, distance)) {
                // Full push until it's close, then less and less the closer it gets
                double push = this.mob.getAttributeValue(Attributes.FLYING_SPEED) * 5.0 / 3.0
                        * Math.min(1.0, distance / ARRIVE_DISTANCE);
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(travel.normalize().scale(push)));
            } else {
                this.stop();
            }
        }

        private void brake() {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(BRAKE));
        }

        private boolean canReach(Vec3 travel, double distance) {
            if (distance < 1.0E-4) return true;

            Vec3 step = travel.scale(1.0 / distance);
            AABB start = this.mob.getBoundingBox().deflate(1.0E-3);
            AABB box = start;
            for (int i = 1; i < distance; i++) {
                box = box.move(step);
                if (!this.mob.level().noCollision(this.mob, box)) return false;
            }
            return this.mob.level().noCollision(this.mob, start.move(travel));
        }
    }

    /**
     * Agitated: watch the target. While it can see them it stays put and stares (and their
     * camera gets pulled toward its face, client side). Once they break line of sight, it moves
     * to get it back: to a nearby spot with a clear view of them, or straight toward them if it
     * can't find one.
     */
    static class WatchGoal extends Goal {
        /** How often it picks a new spot while it can't see them (1 second). */
        private static final int REPOSITION_INTERVAL = 20;
        /** How far around itself it looks for a spot with a view, in blocks. */
        private static final double SEARCH_RADIUS = 8.0;
        private static final int ATTEMPTS = 24;

        private final GhazeEntity ghaze;
        private int repositionCooldown;
        private boolean repositioning;

        WatchGoal(GhazeEntity ghaze) {
            this.ghaze = ghaze;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.ghaze.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.repositionCooldown = 0;
            this.repositioning = false;
        }

        @Override
        public void stop() {
            ((GhazeMoveControl) this.ghaze.getMoveControl()).stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.ghaze.getTarget();
            if (target == null) return;

            // Stares at them (head tilts up or down too), body turned to face them
            this.ghaze.getLookControl().setLookAt(target, 30.0F, 30.0F);
            Ghast.faceMovementDirection(this.ghaze);

            if (this.ghaze.getSensing().hasLineOfSight(target)) {
                if (this.repositioning) {
                    ((GhazeMoveControl) this.ghaze.getMoveControl()).stop();
                    this.repositioning = false;
                }
                this.repositionCooldown = 0;
                return;
            }

            if (this.repositionCooldown-- > 0) return;
            this.repositionCooldown = REPOSITION_INTERVAL;

            Vec3 spot = this.findSpotWithView(target);
            if (spot == null) {
                // No spot with a view nearby: head toward them
                spot = target.position().add(0.0, 1.0, 0.0);
            }
            this.ghaze.getMoveControl().setWantedPosition(spot.x, spot.y, spot.z, 1.0);
            this.repositioning = true;
        }

        /** The closest open spot nearby that it fits in and can see the target from; or null. */
        private Vec3 findSpotWithView(LivingEntity target) {
            Vec3 here = this.ghaze.position();
            Vec3 targetEyes = target.getEyePosition();
            AABB box = this.ghaze.getBoundingBox().deflate(1.0E-3);
            Vec3 best = null;
            double bestDistance = Double.MAX_VALUE;

            for (int i = 0; i < ATTEMPTS; i++) {
                Vec3 spot = here.add(
                        (this.ghaze.getRandom().nextDouble() * 2 - 1) * SEARCH_RADIUS,
                        (this.ghaze.getRandom().nextDouble() * 2 - 1) * SEARCH_RADIUS / 2,
                        (this.ghaze.getRandom().nextDouble() * 2 - 1) * SEARCH_RADIUS);
                double distance = spot.distanceToSqr(here);
                if (distance >= bestDistance) continue;
                if (!this.ghaze.level().noCollision(this.ghaze, box.move(spot.subtract(here)))) continue;

                Vec3 head = spot.add(0.0, HEAD_HEIGHT, 0.0);
                HitResult hit = this.ghaze.level().clip(new ClipContext(head, targetEyes,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.ghaze));
                if (hit.getType() != HitResult.Type.MISS) continue;

                best = spot;
                bestDistance = distance;
            }
            return best;
        }
    }

    /**
     * Idle travel: pick a nearby open spot, look toward it (up or down too) while floating
     * there, then rest a while before picking the next one. Once it arrives, the look goal
     * ends and the head's tilt settles back to level.
     */
    static class DriftGoal extends Goal {
        /** How far a new spot can be, in blocks: sideways, and up or down. */
        private static final double RANGE_HORIZONTAL = 10.0;
        private static final double RANGE_VERTICAL = 4.0;
        /** Spots closer than this aren't worth the trip. */
        private static final double MIN_DISTANCE = 3.0;
        /** Stays within this many blocks above solid ground, so it hovers near the terrain. */
        private static final int MAX_HEIGHT_ABOVE_GROUND = 6;
        /** Close enough to count as there (squared blocks). */
        private static final double ARRIVED_DISTANCE_SQR = 1.0;
        /** Gives up on a trip after this long (15 seconds), e.g. if something blocks it. */
        private static final int MAX_TRAVEL_TICKS = 300;
        /** How long it rests between trips (4 to 10 seconds). */
        private static final int REST_MIN = 80;
        private static final int REST_MAX = 200;
        private static final int ATTEMPTS = 32;

        private final GhazeEntity ghaze;
        private Vec3 destination;
        private int travelTicks;
        private int restTicks;

        DriftGoal(GhazeEntity ghaze) {
            this.ghaze = ghaze;
            this.restTicks = Mth.nextInt(ghaze.getRandom(), REST_MIN, REST_MAX);
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.ghaze.getTarget() != null) return false;
            if (this.restTicks > 0) {
                this.restTicks--;
                return false;
            }
            this.destination = this.findDestination();
            if (this.destination == null) {
                // Nowhere to go from here; try again after another rest
                this.restTicks = Mth.nextInt(this.ghaze.getRandom(), REST_MIN, REST_MAX);
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ghaze.getTarget() == null
                    && this.ghaze.getMoveControl().hasWanted()
                    && this.travelTicks < MAX_TRAVEL_TICKS
                    && this.ghaze.position().distanceToSqr(this.destination) > ARRIVED_DISTANCE_SQR;
        }

        @Override
        public void start() {
            this.travelTicks = 0;
            this.ghaze.getMoveControl().setWantedPosition(this.destination.x, this.destination.y, this.destination.z, 1.0);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.travelTicks++;

            // Head looks at where it's going, tilting up or down for it
            this.ghaze.getLookControl().setLookAt(this.destination.x, this.destination.y, this.destination.z, 10.0F, 30.0F);

            // Body turns to face the way it's drifting (only while actually moving, or it would
            // snap to face south)
            if (this.ghaze.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) {
                Ghast.faceMovementDirection(this.ghaze);
            }
        }

        @Override
        public void stop() {
            // Stop pushing and brake to a halt (no more pushes toward anywhere, so no rocking)
            ((GhazeMoveControl) this.ghaze.getMoveControl()).stop();
            this.restTicks = Mth.nextInt(this.ghaze.getRandom(), REST_MIN, REST_MAX);
        }

        /** A random open spot nearby, near the ground, that it fits in; or null. */
        private Vec3 findDestination() {
            Vec3 here = this.ghaze.position();
            for (int i = 0; i < ATTEMPTS; i++) {
                Vec3 spot = here.add(
                        (this.ghaze.getRandom().nextDouble() * 2 - 1) * RANGE_HORIZONTAL,
                        (this.ghaze.getRandom().nextDouble() * 2 - 1) * RANGE_VERTICAL,
                        (this.ghaze.getRandom().nextDouble() * 2 - 1) * RANGE_HORIZONTAL);
                if (spot.distanceToSqr(here) < MIN_DISTANCE * MIN_DISTANCE) continue;
                if (!this.ghaze.level().noCollision(this.ghaze, this.ghaze.getBoundingBox().move(spot.subtract(here)))) continue;
                if (!this.hasGroundBelow(spot)) continue;
                return spot;
            }
            return null;
        }

        private boolean hasGroundBelow(Vec3 spot) {
            BlockPos.MutableBlockPos pos = BlockPos.containing(spot).mutable();
            for (int i = 0; i <= MAX_HEIGHT_ABOVE_GROUND; i++) {
                if (!this.ghaze.level().getBlockState(pos).getCollisionShape(this.ghaze.level(), pos).isEmpty()) return true;
                pos.move(0, -1, 0);
            }
            return false;
        }
    }
}
