package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.sound.ModSounds;

import java.util.EnumSet;

public class Blazt extends Mob implements Enemy {
    // Synced Data so the client knows when to change textures/animations
    private static final EntityDataAccessor<Boolean> SHOOTING = SynchedEntityData.defineId(Blazt.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BULLRUSHING = SynchedEntityData.defineId(Blazt.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED = SynchedEntityData.defineId(Blazt.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();

    public Blazt(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new BlaztMoveControl(this);
        this.setNoGravity(true); // Flies by default
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 1. Explosion Immunity
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }

        // 2. Ghast Fireball Immunity & Achievement
        if (source.getDirectEntity() instanceof LargeFireball) {

            // If a player deflected it, give them the achievement!
            if (source.getEntity() instanceof ServerPlayer player) {
                AdvancementHolder advancement = level.getServer().getAdvancements()
                        .get(Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "blazt_blaster"));

                if (advancement != null) {
                    for (String criteria : advancement.value().criteria().keySet()) {
                        player.getAdvancements().award(advancement, criteria);
                    }
                }
            }

            return false; // Takes 0 damage from the fireball
        }

        return super.hurtServer(level, source, amount);
    }

    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                // The ghast move control pushes it FLYING_SPEED * 5/3 every few ticks; 0.06 is the
                // push the Blazt always had (0.1), and the same speed as a vanilla ghast
                .add(Attributes.FLYING_SPEED, 0.06D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D);
    }

    @Override
    public void travel(Vec3 movementInput) {
        // If stunned, let normal vanilla gravity and friction take over completely
        if (this.isStunned()) {
            super.travel(movementInput);
            return;
        }

        // Capture the horizontal momentum BEFORE Minecraft applies ground/ceiling friction
        double prevX = this.getDeltaMovement().x;
        double prevZ = this.getDeltaMovement().z;

        // Run the normal movement physics
        super.travel(movementInput);

        // If it bumped its head or its feet (vertical collision) but didn't hit a wall
        if (this.verticalCollision && !this.horizontalCollision) {
            // Restore the horizontal momentum so it "slides" along the surface!
            // We multiply by 0.98D to give it just a tiny bit of drag so it doesn't slide infinitely.
            this.setDeltaMovement(prevX * 0.98D, this.getDeltaMovement().y, prevZ * 0.98D);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOOTING, false);
        builder.define(BULLRUSHING, false);
        builder.define(STUNNED, false);
    }

    @Override
    protected void registerGoals() {
        // Add the look goal alongside the target selector
        this.goalSelector.addGoal(1, new BlaztLookGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));

        this.goalSelector.addGoal(1, new BlaztBullrushGoal(this));
        this.goalSelector.addGoal(2, new BlaztDashGoal(this));
        this.goalSelector.addGoal(3, new BlaztShootGoal(this));
        this.goalSelector.addGoal(4, new BlaztApproachGoal(this));
        this.goalSelector.addGoal(5, new RandomFloatAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        // Gravity affects it if stunned!
        if (!this.level().isClientSide()) {
            this.setNoGravity(!this.isStunned());
        }

        // Handle Animations Client-Side
        if (this.level().isClientSide()) {
            if (this.isShooting()) {
                this.shootAnimationState.startIfStopped(this.tickCount);
                this.idleAnimationState.stop();
            } else {
                this.idleAnimationState.startIfStopped(this.tickCount);
                this.shootAnimationState.stop();
            }
        }

        this.spawnSmokeParticles();
    }

    // --- State Getters & Setters ---
    public boolean isShooting() { return this.entityData.get(SHOOTING); }
    public void setShooting(boolean shooting) { this.entityData.set(SHOOTING, shooting); }

    public boolean isBullrushing() { return this.entityData.get(BULLRUSHING); }
    public void setBullrushing(boolean bullrushing) { this.entityData.set(BULLRUSHING, bullrushing); }

    public boolean isStunned() { return this.entityData.get(STUNNED); }
    public void setStunned(boolean stunned) { this.entityData.set(STUNNED, stunned); }

    // --- Sounds ---
    @Override
    protected SoundEvent getAmbientSound() { return ModSounds.BLAZT_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) { return ModSounds.BLAZT_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return ModSounds.BLAZT_DEATH; }

    private void spawnSmokeParticles() {
        if (this.level().isClientSide()) {
            double centerX = this.getX();
            double centerY = this.getY();
            double centerZ = this.getZ();

            RandomSource random = this.getRandom();

            for(int i = 0; i < random.nextInt(2) + 2; i++) {
                double offsetX = random.nextDouble() - .5;
                double offsetZ = random.nextDouble() - .5;

                double velocityX = (random.nextDouble() - .5) * .02;
                double velocityY = -.2 - random.nextDouble() * .05;
                double velocityZ = (random.nextDouble() - 0.5) * .02;

                if(this.isBullrushing()) {
                    this.level().addParticle(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            centerX + offsetX,
                            centerY,
                            centerZ + offsetZ,
                            velocityX,
                            velocityY,
                            velocityZ
                    );
                } else {
                    this.level().addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            centerX + offsetX,
                            centerY,
                            centerZ + offsetZ,
                            velocityX,
                            velocityY,
                            velocityZ
                    );
                }
            }
        }
    }

    // --- Move Control: vanilla's ghast movement, paused while stunned ---
    static class BlaztMoveControl extends Ghast.GhastMoveControl<Blazt> {
        public BlaztMoveControl(Blazt blazt) {
            super(blazt, false, () -> false);
        }

        @Override
        public void tick() {
            // Stunned: gravity and friction take over (see travel), so don't push it around
            if (this.mob.isStunned()) return;
            super.tick();
        }
    }

    static class BlaztBullrushGoal extends Goal {
        private final Blazt blazt;
        private int chargeTimer;
        private int stunTimer;

        public BlaztBullrushGoal(Blazt blazt) {
            this.blazt = blazt;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.blazt.getTarget() != null
                    && !this.blazt.isShooting() // <-- Prevents the wombo combo!
                    && this.blazt.distanceToSqr(this.blazt.getTarget()) < 256.0D
                    && this.blazt.getRandom().nextInt(40) == 0;
        }

        @Override
        public void start() {
            this.blazt.setBullrushing(true);
            this.chargeTimer = 40; // 2 seconds of windup
            this.stunTimer = 0;
            this.blazt.level().playSound(null, this.blazt.blockPosition(), ModSounds.BLAZT_BREATHE_IN, SoundSource.HOSTILE, 1.0f, 1.0f);
        }

        @Override
        public void tick() {
            if (this.stunTimer > 0) {
                this.stunTimer--;
                if (this.stunTimer <= 0) {
                    this.blazt.setStunned(false);
                    this.blazt.setBullrushing(false);
                    this.blazt.setDeltaMovement(0.0D, 0.75D, 0.0D); // 1. Jump up after stun ends
                }
                return;
            }

            LivingEntity target = this.blazt.getTarget();
            if (target == null) {
                this.blazt.setBullrushing(false);
                return;
            }

            if (this.chargeTimer > 0) {
                this.chargeTimer--;
                if (this.chargeTimer == 0) {
                    Vec3 dashVec = target.position().subtract(this.blazt.position()).normalize().scale(2.5D);
                    this.blazt.setDeltaMovement(dashVec);
                }
            } else {
                if (this.blazt.getBoundingBox().inflate(0.5D).intersects(target.getBoundingBox())) {
                    if (target instanceof Player player && player.isBlocking()) {
                        this.blazt.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 1f, 0.8f + this.blazt.getRandom().nextFloat() * 0.4f);
                        this.blazt.setStunned(true);
                        this.stunTimer = 15;
                        this.blazt.setDeltaMovement(this.blazt.getDeltaMovement().scale(-0.5D));
                    } else {
                        target.hurt(this.blazt.damageSources().mobAttack(this.blazt), (float) this.blazt.getAttributeValue(Attributes.ATTACK_DAMAGE));
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.nullcoil.soulscorch.effect.ModEffects.SOULSCORCH, 200, 0));

                        this.blazt.setBullrushing(false);
                        this.blazt.setDeltaMovement(0.0D, 0.75D, 0.0D); // 2. Jump up after a successful hit
                        this.blazt.level().playSound(null, this.blazt.blockPosition(), ModSounds.BLAZT_BREATHE_OUT, SoundSource.HOSTILE, 1.0f, 1.0f);
                    }
                } else if (this.blazt.getDeltaMovement().lengthSqr() < 0.05D) {
                    this.blazt.setBullrushing(false);
                    this.blazt.setDeltaMovement(0.0D, 0.75D, 0.0D); // 3. Jump up after missing and slowing down
                    this.blazt.level().playSound(null, this.blazt.blockPosition(), ModSounds.BLAZT_BREATHE_OUT, SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            // Drop out of the goal immediately if the target becomes null while not stunned
            return (this.blazt.isBullrushing() && this.blazt.getTarget() != null) || this.stunTimer > 0;
        }

        @Override
        public void stop() {
            this.blazt.setBullrushing(false);
            this.blazt.setStunned(false);
        }
    }

    // --- Default Floating ---
    static class RandomFloatAroundGoal extends Goal {
        private final Blazt blazt;

        public RandomFloatAroundGoal(Blazt blazt) {
            this.blazt = blazt;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl moveControl = this.blazt.getMoveControl();
            if (!moveControl.hasWanted() && this.blazt.getRandom().nextInt(7) == 0) {
                return true;
            } else {
                double d = moveControl.getWantedX() - this.blazt.getX();
                double e = moveControl.getWantedY() - this.blazt.getY();
                double f = moveControl.getWantedZ() - this.blazt.getZ();
                double g = d * d + e * e + f * f;
                return g < 1.0D || g > 3600.0D;
            }
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            double d = this.blazt.getX() + (double)((this.blazt.getRandom().nextFloat() * 2.0F - 1.0F) * 16.0F);
            double e = this.blazt.getY() + (double)((this.blazt.getRandom().nextFloat() * 2.0F - 1.0F) * 16.0F);
            double f = this.blazt.getZ() + (double)((this.blazt.getRandom().nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.blazt.getMoveControl().setWantedPosition(d, e, f, 1.0D);
        }
    }

    static class BlaztShootGoal extends Goal {
        private final Blazt blazt;
        public int chargeTime;

        public BlaztShootGoal(Blazt blazt) {
            this.blazt = blazt;
        }

        public boolean canUse() {
            return this.blazt.getTarget() != null
                    && !this.blazt.isBullrushing()
                    && !this.blazt.isStunned();
        }

        public void start() {
            this.chargeTime = 0;
        }

        public void stop() {
            this.blazt.setShooting(false);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.blazt.getTarget();
            if (livingEntity != null) {
                // If within 64 blocks and has line of sight
                if (livingEntity.distanceToSqr(this.blazt) < 4096.0D && this.blazt.hasLineOfSight(livingEntity)) {
                    Level level = this.blazt.level();
                    ++this.chargeTime;

                    // Windup phase (Replaced level event 1015 with your custom sound)
                    if (this.chargeTime == 10 && !this.blazt.isSilent()) {
                        level.playSound(null, this.blazt.blockPosition(), ModSounds.BLAZT_SHOOTING, SoundSource.HOSTILE, 2.0F, 1.0F);
                    }

                    // Firing phase
                    // Firing phase
                    if (this.chargeTime == 20) {
                        Vec3 vec3 = this.blazt.getViewVector(1.0F);
                        double f = livingEntity.getX() - (this.blazt.getX() + vec3.x * 4.0D);
                        double g = livingEntity.getY(0.5D) - (0.5D + this.blazt.getY(0.5D));
                        double h = livingEntity.getZ() - (this.blazt.getZ() + vec3.z * 4.0D);
                        Vec3 vec32 = new Vec3(f, g, h).normalize();

                        if (!this.blazt.isSilent()) {
                            level.playSound(null, this.blazt.blockPosition(), ModSounds.BLAZT_SOUL_CHARGE, SoundSource.HOSTILE, 2.0F, 1.0F);
                        }

                        // 50/50 Chance between Deflectable Fireball and Soul Charge
                        if (this.blazt.getRandom().nextBoolean()) {
                            // Deflectable Vanilla Ghast Fireball
                            LargeFireball largeFireball =
                                    new LargeFireball(level, this.blazt, vec32, 1);

                            largeFireball.setPos(this.blazt.getX() + vec3.x * 4.0D, this.blazt.getY(0.5D) + 0.5D, this.blazt.getZ() + vec3.z * 4.0D);
                            level.addFreshEntity(largeFireball);
                        } else {
                            // Non-deflectable Soul Charge Cloud Projectile (Now using Vec3!)
                            net.nullcoil.soulscorch.entity.projectile.SoulChargeProjectile soulCharge =
                                    new net.nullcoil.soulscorch.entity.projectile.SoulChargeProjectile(level, this.blazt, vec32);

                            // We still keep setPos to offset it 4 blocks forward so it doesn't spawn inside the Blazt's stomach!
                            soulCharge.setPos(this.blazt.getX() + vec3.x * 4.0D, this.blazt.getY(0.5D) + 0.5D, this.blazt.getZ() + vec3.z * 4.0D);
                            level.addFreshEntity(soulCharge);
                        }

                        this.chargeTime = -40; // Cooldown
                    }
                } else if (this.chargeTime > 0) {
                    --this.chargeTime; // Slowly cancel the attack if line of sight is broken
                }

                // Triggers the AGGRO animation and texture!
                this.blazt.setShooting(this.chargeTime > 10);
            }
        }
    }

    static class BlaztDashGoal extends Goal {
        private final Blazt blazt;
        private int cooldown = 100;

        public BlaztDashGoal(Blazt blazt) {
            this.blazt = blazt;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.blazt.getTarget() == null || this.blazt.isStunned() || this.blazt.isBullrushing()) return false;

            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            // 1 in 20 chance to trigger every tick the cooldown is ready
            return this.blazt.getRandom().nextInt(20) == 0;
        }

        @Override
        public void start() {
            // Pick a random direction (0 = left, 1 = right, 2 = up, 3 = down)
            int dir = this.blazt.getRandom().nextInt(4);
            Vec3 look = this.blazt.getLookAngle();
            Vec3 dashVec = Vec3.ZERO;

            switch (dir) {
                case 0 -> dashVec = look.cross(new Vec3(0, 1, 0)).normalize(); // Left
                case 1 -> dashVec = look.cross(new Vec3(0, -1, 0)).normalize(); // Right
                case 2 -> dashVec = new Vec3(0, 1, 0); // Up
                case 3 -> dashVec = new Vec3(0, -1, 0); // Down
            }

            // Apply a massive burst of speed
            this.blazt.setDeltaMovement(this.blazt.getDeltaMovement().add(dashVec.scale(1.5D)));
            this.cooldown = 60 + this.blazt.getRandom().nextInt(60); // 3 to 6 seconds before it can dash again
        }

        @Override
        public boolean canContinueToUse() { return false; } // Instant action, terminates immediately
    }

    static class BlaztApproachGoal extends Goal {
        private final Blazt blazt;

        public BlaztApproachGoal(Blazt blazt) {
            this.blazt = blazt;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.blazt.getTarget() == null || this.blazt.isStunned() || this.blazt.isBullrushing()) return false;

            double dist = this.blazt.distanceToSqr(this.blazt.getTarget());
            // Trigger if player is further than 16 blocks (256 sqr) OR if Y level difference is massive
            boolean farAway = dist > 256.0D;
            boolean badYLevel = Math.abs(this.blazt.getY() - this.blazt.getTarget().getY()) > 5.0D;

            return farAway || badYLevel;
        }

        @Override
        public void tick() {
            Player target = (Player) this.blazt.getTarget();
            if (target == null) return;

            // Move directly towards the target's eyes
            Vec3 targetPos = target.position().add(0, target.getEyeHeight(), 0);
            this.blazt.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 1.2D); // Moves 20% faster while chasing
            this.blazt.lookAt(target, 30.0F, 30.0F);
        }

        @Override
        public boolean canContinueToUse() {
            // Keep using it until we are close enough!
            return this.canUse() && !this.blazt.isShooting();
        }
    }

    static class BlaztLookGoal extends Goal {
        private final Blazt blazt;

        public BlaztLookGoal(Blazt blazt) {
            this.blazt = blazt;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true; // Always running!
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.blazt.getTarget() == null) {
                // If idling, look in the direction it's floating
                Vec3 vec3 = this.blazt.getDeltaMovement();
                this.blazt.setYRot(-((float)net.minecraft.util.Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
                this.blazt.yBodyRot = this.blazt.getYRot();
            } else {
                // If aggroed, lock onto the target!
                LivingEntity target = this.blazt.getTarget();
                if (target.distanceToSqr(this.blazt) < 4096.0D) {
                    double dX = target.getX() - this.blazt.getX();
                    double dZ = target.getZ() - this.blazt.getZ();
                    this.blazt.setYRot(-((float)net.minecraft.util.Mth.atan2(dX, dZ)) * (180F / (float)Math.PI));
                    this.blazt.yBodyRot = this.blazt.getYRot();
                }
            }
        }
    }
}