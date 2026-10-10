package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.entity.client.soulless.SoullessActivity;
import net.nullcoil.soulscorch.sound.ModSounds;
import net.nullcoil.soulscorch.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class SoullessEntity extends ZombifiedPiglin implements NeutralMob {
    @Nullable
    private EntityReference<LivingEntity> angryAt;
    private int angerTime = 1200;
    private int watchTime = 1800;
    private static final List<EquipmentSlot> EQUIPMENT_INIT_ORDER;
    private static final UniformInt ANGER_TIME_RANGE;

    // Client-Side Animation Trackers
    public int neutralTwitchState = -1;
    public int twitchTimer = 0;
    public final AnimationState passiveAnimationState = new AnimationState();
    public final AnimationState neutralAnimationState = new AnimationState();
    public final AnimationState hostileAnimationState = new AnimationState();

    // 1.21 SynchedEntityData (Replaces DataTracker)
    private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(SoullessEntity.class, EntityDataSerializers.INT);

    public SoullessEntity(EntityType<? extends ZombifiedPiglin> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVITY, SoullessActivity.PASSIVE.getId());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZombifiedPiglin.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockPos pos = this.getBlockPosBelowThatAffectsMyMovement();
        BlockState blockState = this.level().getBlockState(pos);
        if (blockState.is(ModTags.Blocks.SOULBASED_BLOCKS)) { return 1.2F; }
        return super.getBlockSpeedFactor();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnData) {
        if (spawnData instanceof Zombie.ZombieGroupData existing) {
            spawnData = new Zombie.ZombieGroupData(existing.isBaby, false);
        } else if (spawnData == null) {
            spawnData = new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(level.getRandom()), false);
        }
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnData);

        float yaw = this.random.nextFloat() * 360f - 180f;
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);

        return data;
    }

    public SoullessActivity getActivity() {
        return SoullessActivity.fromId(this.entityData.get(ACTIVITY));
    }

    public void setActivity(@NotNull SoullessActivity activity) {
        this.entityData.set(ACTIVITY, activity.getId());
    }

    // Update the method signature to require the Player
    public void raiseActivity(@Nullable Player player) {
        switch (getActivity()) {
            case PASSIVE -> {
                this.playSound(ModSounds.SOULLESS_WAKE);
                setActivity(SoullessActivity.NEUTRAL);

                // Immediately lock eyes with the block-breaker!
                if (player != null) {
                    this.setTarget(player);
                }
            }
            case NEUTRAL -> {
                this.playSound(ModSounds.SOULLESS_WAKE);
                setActivity(SoullessActivity.HOSTILE);

                // Lock target and trigger anger
                if (player != null) {
                    this.setTarget(player);
                    this.startPersistentAngerTimer();
                }
            }
            case HOSTILE -> {}
        }
    }

    @Override
    public void tick() {
        super.tick();

        // CLIENT-SIDE LOGIC (Animations & Random Twitching)
        if (this.level().isClientSide()) {
            SoullessActivity activity = this.getActivity();

            if (activity == SoullessActivity.PASSIVE) {
                this.neutralAnimationState.stop();
                this.hostileAnimationState.stop();
                this.neutralTwitchState = -1;

                // 1. Random Timer Logic for Passive Twitching
                if (this.twitchTimer > 0) {
                    this.twitchTimer--;
                } else {
                    // 20% chance to twitch when the timer is up
                    if (this.random.nextFloat() < 0.2f) {
                        // We use .start() to FORCE the animation to restart from frame 0
                        this.passiveAnimationState.start(this.tickCount);
                        // Wait 5 to 7 seconds before considering another twitch
                        this.twitchTimer = 100 + this.random.nextInt(40);
                    } else {
                        // Failed the chance, check again in 1 second
                        this.passiveAnimationState.stop();
                        this.twitchTimer = 20;
                    }
                }

            } else if (activity == SoullessActivity.NEUTRAL) {
                this.passiveAnimationState.stop();
                this.hostileAnimationState.stop();

                if (this.twitchTimer > 0) {
                    this.twitchTimer--;
                } else {
                    if (this.random.nextFloat() < 0.2f) {
                        this.neutralTwitchState = this.random.nextInt(3);
                        this.neutralAnimationState.start(this.tickCount);
                        this.twitchTimer = 100 + this.random.nextInt(40);
                    } else {
                        this.neutralTwitchState = -1;
                        this.neutralAnimationState.stop();
                        this.twitchTimer = 20;
                    }
                }
            } else if (activity == SoullessActivity.HOSTILE) {
                this.passiveAnimationState.stop();
                this.neutralAnimationState.stop();
                this.neutralTwitchState = -1;
                this.twitchTimer = 0;

                // Hostile animation is usually a continuous state (like arm raising),
                // so startIfStopped is correct here!
                this.hostileAnimationState.startIfStopped(this.tickCount);
            }
        }

        // SERVER-SIDE LOGIC
        else if (this.level() instanceof ServerLevel serverLevel) {
            switch(getActivity()) {
                case NEUTRAL -> {
                    if(watchTime > 0) {
                        watchTime--;
                    }
                    if(watchTime <= 0) {
                        setActivity(SoullessActivity.PASSIVE);
                        watchTime = 1800;
                    }
                    if(this.getTarget() != null && !this.hasLineOfSight(this.getTarget())) {
                        setActivity(SoullessActivity.PASSIVE);
                    }
                }
                case HOSTILE -> {
                    if(angerTime > 0) {
                        angerTime--;
                    }
                    if(angerTime <= 0) {
                        setActivity(SoullessActivity.NEUTRAL);
                        angerTime = 1200;
                    }
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean bl = super.doHurtTarget(level, target);
        if (bl && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(
                    ModEffects.SOULSCORCH,
                    600, // 30 seconds
                    0,
                    false,
                    false,
                    true
            ));
            this.angerTime = 1200;
        }
        return bl;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean wasDamaged = super.hurtServer(level, source, amount);

        if (wasDamaged) {
            // If a player caused the damage...
            if (source.getEntity() instanceof Player player) {

                // 1. Wake self up and get angry
                if (this.getActivity() != SoullessActivity.HOSTILE) {
                    this.setActivity(SoullessActivity.HOSTILE);
                    this.setTarget(player);
                    this.startPersistentAngerTimer();
                }

                // 2. The Colony Aggro Shout
                // Draw a 35-block box (matching their max follow range)
                net.minecraft.world.phys.AABB alertBox = this.getBoundingBox().inflate(35.0);

                for (SoullessEntity buddy : level.getEntitiesOfClass(SoullessEntity.class, alertBox)) {
                    // If the buddy is sleeping or just staring, snap them to HOSTILE
                    if (buddy != this && buddy.getActivity() != SoullessActivity.HOSTILE) {
                        buddy.setActivity(SoullessActivity.HOSTILE);
                        buddy.setTarget(player);
                        buddy.startPersistentAngerTimer();
                    }
                }
            }
        }
        return wasDamaged;
    }

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public boolean isPushable() {
        if(this.getActivity() == SoullessActivity.PASSIVE) return false;
        return super.isPushable();
    }

    @Override
    protected void registerGoals() {
        // NEUTRAL Goals
        this.goalSelector.addGoal(7, new LookAtTargetGoal(this, 0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override public boolean canUse() { return getActivity() == SoullessActivity.NEUTRAL && super.canUse(); }
        });

        // HOSTILE Goals
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false) {
            @Override public boolean canUse() { return getActivity() == SoullessActivity.HOSTILE && super.canUse(); }
            @Override public boolean canContinueToUse() { return getActivity() == SoullessActivity.HOSTILE && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override public boolean canUse() { return getActivity() == SoullessActivity.HOSTILE && super.canUse(); }
        });

        // TARGET Goals
        this.targetSelector.addGoal(1, (new SoullessRevengeGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt) {
            @Override public boolean canUse() { return getActivity() != SoullessActivity.PASSIVE && super.canUse(); }
            @Override public boolean canContinueToUse() { return getActivity() != SoullessActivity.PASSIVE && super.canContinueToUse(); }
        });
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if(this.getActivity() == SoullessActivity.PASSIVE) return false;
        return super.canAttack(target);
    }

    // NeutralMob Anger Methods
    public int getRemainingPersistentAngerTime() { return this.angerTime; }

    public void setRemainingPersistentAngerTime(int angerTime) { this.angerTime = angerTime; }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() { return this.angryAt; }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> angryAt) { this.angryAt = angryAt; }

    @Override
    public void startPersistentAngerTimer() { this.setRemainingPersistentAngerTime(ANGER_TIME_RANGE.sample(this.random)); }

    static class SoullessRevengeGoal extends HurtByTargetGoal {
        public SoullessRevengeGoal(SoullessEntity mob, Class<?>... noRevengeTypes) {
            super(mob, noRevengeTypes);
        }

        @Override
        public void start() {
            super.start();
            if (this.mob instanceof SoullessEntity soulless) {
                soulless.setActivity(SoullessActivity.HOSTILE);
            }
        }
    }

    @Override
    public SoundSource getSoundSource() { return SoundSource.NEUTRAL; }

    @Override
    public SoundEvent getAmbientSound() {
        switch(getActivity()) {
            case NEUTRAL -> { return ModSounds.SOULLESS_AMBIENT; }
            case HOSTILE -> { return ModSounds.SOULLESS_ANGRY; }
        }
        return ModSounds.SOULLESS_WHISPER.value();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) { return ModSounds.SOULLESS_HURT; }

    @Override
    public SoundEvent getDeathSound() { return ModSounds.SOULLESS_DEATH; }

    @Override
    public void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        if(random.nextFloat() < 0.15f * localDifficulty.getSpecialMultiplier()) {
            float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            boolean bool = true;

            for(EquipmentSlot slot : EQUIPMENT_INIT_ORDER) {
                int level = random.nextInt(2);

                if(random.nextFloat() < 0.095f) ++level;
                if(random.nextFloat() < 0.095f) ++level;
                if(random.nextFloat() < 0.095f) ++level;

                ItemStack itemStack = this.getItemBySlot(slot);
                if(!bool && random.nextFloat() < f) break;
                bool = false;

                if(itemStack.isEmpty()) {
                    Item item = getEquipment(slot, level);
                    if(item != null) {
                        this.setItemSlot(slot, new ItemStack(item));
                    }
                }
            }
        }

        EquipmentSlot hand = EquipmentSlot.MAINHAND;
        if (random.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.05f : 0.01f)) {
            int i = random.nextInt(3);
            if(i == 0) {
                this.setItemSlot(hand, new ItemStack(Items.IRON_SWORD));
            } else {
                switch(random.nextInt(4)) {
                    case 0 -> this.setItemSlot(hand, new ItemStack(Items.IRON_AXE));
                    case 1 -> this.setItemSlot(hand, new ItemStack(Items.IRON_PICKAXE));
                    case 2 -> this.setItemSlot(hand, new ItemStack(Items.IRON_HOE));
                    default -> this.setItemSlot(hand, new ItemStack(Items.IRON_SHOVEL));
                }
            }
        }
    }

    @Nullable
    public static Item getEquipment(EquipmentSlot slot, int level) {
        switch(slot) {
            case HEAD:
                if(level == 0) return Items.CHAINMAIL_HELMET;
                if(level == 1) return Items.IRON_HELMET;
                if(level == 2) return Items.DIAMOND_HELMET;
                break;
            case CHEST:
                if(level == 0) return Items.CHAINMAIL_CHESTPLATE;
                if(level == 1) return Items.IRON_CHESTPLATE;
                if(level == 2) return Items.DIAMOND_CHESTPLATE;
                break;
            case LEGS:
                if(level == 0) return Items.CHAINMAIL_LEGGINGS;
                if(level == 1) return Items.IRON_LEGGINGS;
                if(level == 2) return Items.DIAMOND_LEGGINGS;
                break;
            case FEET:
                if(level == 0) return Items.CHAINMAIL_BOOTS;
                if(level == 1) return Items.IRON_BOOTS;
                if(level == 2) return Items.DIAMOND_BOOTS;
                break;
        }
        return null;
    }

    static class LookAtTargetGoal extends ZombieAttackGoal {
        public LookAtTargetGoal(SoullessEntity soulless, double speed) {
            super(soulless, speed, false);
            this.setFlags(EnumSet.of(Flag.LOOK)); // setControls is now setFlags
        }

        @Override
        public boolean canUse() {
            return ((SoullessEntity)this.mob).getActivity() == SoullessActivity.NEUTRAL;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            this.mob.getNavigation().stop();

            if (target != null && !target.isRemoved()) {
                double dx = target.getX() - this.mob.getX();
                double dz = target.getZ() - this.mob.getZ();
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                this.mob.setYRot(yaw);
                this.mob.setYBodyRot(yaw);
                this.mob.setYHeadRot(yaw);
            }
        }
    }

    static {
        ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(20, 39);
        EQUIPMENT_INIT_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }
}