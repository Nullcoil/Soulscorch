package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.sound.ModSounds;
import net.nullcoil.soulscorch.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RestlessEntity extends Monster implements Enemy, HoglinBase {

    private static final EntityDataAccessor<Boolean> AWAKENED = SynchedEntityData.defineId(RestlessEntity.class, EntityDataSerializers.BOOLEAN);

    private static boolean aiming = false; // Changed to primitive boolean
    private Vec3 chargeDirection;

    public RestlessEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnData);

        float yaw = this.random.nextFloat() * 360f - 180f;
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        this.setBaby(false);

        return data;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AWAKENED, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zoglin.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 7.0f)
                .add(Attributes.STEP_HEIGHT, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new LookAtTargetGoal(this));
        this.goalSelector.addGoal(0, new BullrushGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockPos pos = this.getBlockPosBelowThatAffectsMyMovement();
        BlockState blockState = this.level().getBlockState(pos);
        if (blockState.is(ModTags.Blocks.SOULBASED_BLOCKS)) { return 1.2F; }
        return super.getBlockSpeedFactor();
    }

    public boolean getAwakened() { return this.entityData.get(AWAKENED); }

    public void setAwakened(boolean awakened) {
        this.entityData.set(AWAKENED, awakened);
        if(awakened) this.playSound(ModSounds.RESTLESS_ANGER);
    }

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public boolean isPushable() {
        if(!getAwakened()) return false;
        return super.isPushable();
    }

    @Override
    public SoundSource getSoundSource() { return SoundSource.HOSTILE; }

    @Override
    protected SoundEvent getAmbientSound() {
        if(!getAwakened()) return ModSounds.RESTLESS_WHISPER.value();
        return SoundEvents.FIRE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.RESTLESS_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return ModSounds.RESTLESS_DEATH; }

    @Override
    public int getAttackAnimationRemainingTicks() { return 0; } // getMovementCooldownTicks mapping

    public static class InterceptHelper {
        public static Vec3 computeIntercept(Vec3 r0, Vec3 p0, Vec3 v, double s) {
            Vec3 d = p0.subtract(r0);
            double dv = d.dot(v); // dotProduct is now just dot()
            double vv = v.dot(v);
            double dd = d.dot(d);

            double A = vv - s * s;
            double B = 2 * dv;
            double C = dd;

            double disc = B * B - 4 * A * C;
            if (disc < 0 || Math.abs(A) < 1e-6) {
                return p0.add(v.scale(70)); // multiply is now scale()
            }

            double sqrtDisc = Math.sqrt(disc);
            double t1 = (-B - sqrtDisc) / (2 * A);
            double t2 = (-B + sqrtDisc) / (2 * A);

            double t = Double.MAX_VALUE;
            if (t1 > 0) t = t1;
            if (t2 > 0 && t2 < t) t = t2;

            if (t == Double.MAX_VALUE) {
                return p0.add(v.scale(70));
            }

            return p0.add(v.scale(t));
        }
    }


    public class BullrushGoal extends Goal {
        private final RestlessEntity mob;
        private LivingEntity target;
        private Vec3 interceptPoint;
        private int timer;
        private enum State { WAITING, CHARGING, DONE }
        private State state = State.DONE;

        public BullrushGoal(RestlessEntity mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() { // canStart mapping
            return mob.getAwakened() && mob.getTarget() != null;
        }

        @Override
        public void start() {
            target = mob.getTarget();
            timer = 60; // 3 seconds wait
            state = State.WAITING;
            interceptPoint = null;
        }

        @Override
        public void tick() {
            switch (state) {
                case WAITING:
                    aiming = true;
                    mob.getNavigation().stop();

                    // Face the target during the aiming phase
                    if (target != null && !target.isRemoved()) {
                        double dx = target.getX() - mob.getX();
                        double dz = target.getZ() - mob.getZ();
                        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        mob.setYRot(yaw);
                        mob.setYBodyRot(yaw);
                        mob.setYHeadRot(yaw);
                    }

                    timer--;
                    if (timer <= 0 && target != null) {
                        Vec3 playerVel = target.getDeltaMovement(); // getVelocity mapping
                        interceptPoint = InterceptHelper.computeIntercept(
                                mob.position(),
                                target.position(),
                                playerVel,
                                0.35
                        );
                        chargeDirection = interceptPoint.subtract(mob.position()).normalize();

                        timer = 30; // 1.5 seconds charge
                        state = State.CHARGING;
                    }
                    break;

                case CHARGING:
                    aiming = false;

                    mob.move(MoverType.SELF, chargeDirection.scale(mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    mob.markHurt();

                    mob.setYRot((float) Math.toDegrees(Math.atan2(-chargeDirection.x, chargeDirection.z)));
                    mob.setYBodyRot(mob.getYRot());
                    mob.setYHeadRot(mob.getYRot());

                    mob.getNavigation().stop();
                    mob.setTarget(null);

                    // AABB.inflate replaces Box.expand
                    AABB hitBox = mob.getBoundingBox().inflate(0.5);
                    for (Entity e : mob.level().getEntities(mob, hitBox, entity -> entity instanceof LivingEntity && entity != mob)) {
                        LivingEntity living = (LivingEntity) e;
                        float strength = 1.5f;
                        double dx = living.getX() - mob.getX();
                        double dz = living.getZ() - mob.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz);

                        if (dist != 0) {
                            dx /= dist;
                            dz /= dist;

                            if (!living.is(ModTags.Entities.SOULSCORCH_ENTITIES)) {
                                // push() mapping for horizontal knockback
                                living.push(dx * strength, 0.4 * strength, dz * strength);
                                mob.markHurt();

                                living.hurtServer(
                                        (ServerLevel) mob.level(),
                                        mob.damageSources().mobAttack(mob),
                                        (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE)
                                );

                                living.addEffect(new MobEffectInstance(
                                        ModEffects.SOULSCORCH,
                                        600,
                                        0,
                                        false,
                                        false,
                                        true
                                ));
                            }
                        }
                    }

                    timer--;
                    if (timer <= 0) {
                        state = State.DONE;
                        mob.setAwakened(false);
                        chargeDirection = null;
                    }
                    break;

                case DONE:
                    mob.setAwakened(false);
                    chargeDirection = null;
                    break;
            }
        }

        @Override
        public boolean canContinueToUse() { // shouldContinue mapping
            return state != State.DONE;
        }
    }

    static class LookAtTargetGoal extends Goal {
        private final RestlessEntity restless;

        public LookAtTargetGoal(RestlessEntity restless) {
            this.restless = restless;
            this.setFlags(EnumSet.of(Flag.LOOK)); // setControls mapping
        }

        @Override
        public boolean canUse() {
            return restless.getAwakened() && aiming;
        }

        @Override
        public boolean requiresUpdateEveryTick() { // shouldRunEveryTick mapping
            return true;
        }

        @Override
        public void tick() {
            if (this.restless.getTarget() == null) {
                Vec3 vec3d = this.restless.getDeltaMovement();
                this.restless.setYRot(-((float) Mth.atan2(vec3d.x, vec3d.z)) * (180F / (float) Math.PI));
                this.restless.yBodyRot = this.restless.getYRot();
            } else {
                LivingEntity livingEntity = this.restless.getTarget();
                if (livingEntity.distanceToSqr(this.restless) < 4096.0D) { // squaredDistanceTo mapping
                    double e = livingEntity.getX() - this.restless.getX();
                    double f = livingEntity.getZ() - this.restless.getZ();
                    this.restless.setYRot(-((float) Mth.atan2(e, f)) * (180F / (float) Math.PI));
                    this.restless.yBodyRot = this.restless.getYRot();
                }
            }
        }
    }
}