package net.nullcoil.soulscorch.entity.ai.heedless;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.nullcoil.nidalib.attribute.Corruption;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.item.ModItems;
import net.nullcoil.soulscorch.particles.ModParticles;
import net.nullcoil.soulscorch.sound.ModSounds;
import net.nullcoil.soulscorch.util.ModTags;
import org.jetbrains.annotations.Nullable;

public class Heedless extends Monster implements Enemy {
    private int fuse;
    private int cooldown = 0;
    private final int maxCooldown = 10*20;
    private static final float[][] CYST_DRIPS = {
            {3.75f, -5.75f, -5.25f},  // head cyst, left
            {-4.50f,  0.50f, 1.00f}, // head cyst, snout side
            {8.75f,  3.25f,  -1.25f}, // left arm
            {6.50f,  7.00f,  -3.75f},
            { -9.25f,  9.00f,  -1.25f}, // right arm
            { -8.75f,  9.00f, 0.25f},
            { -4.50f, 16.00f,  -0.00f}, // right leg
            { -2.00f, 19.75f,  -2.00f},
            { -1.00f, 23.00f, 2.00f},
            {3.75f, 22.00f, 0.75f}  // left leg
    };

    // Entity events 100-102: "warn with twitch variant 0/1/2". An event reaches the client
    // every time, even when the same variant is picked twice in a row, and carries the
    // variant with it (a synced data value would only send changes, and could arrive a tick
    // after the event).
    private static final byte WARN_EVENT = 100;
    private static final int WARN_VARIANTS = 3;

    public final AnimationState warnAnimation = new AnimationState();
    // Client-side: the twitch the current warning plays (-1 = none yet)
    private int warnVariant = -1;

    public Heedless(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData sgd = super.finalizeSpawn(level, difficulty, reason, data);
        float yaw = this.random.nextFloat() * 360f - 180f;
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);

        return sgd;
    }

    private void dripFromCysts() {
        if (this.random.nextInt(5) != 0) return;

        float[] cyst = CYST_DRIPS[this.random.nextInt(CYST_DRIPS.length)];
        float mx = -cyst[0] / 16f;
        float my = 1.501f - cyst[1] / 16f;
        float mz = cyst[2] / 16f;

        float yaw = this.yBodyRot * ((float) Math.PI / 180f);
        float cos = Mth.cos(yaw);
        float sin = Mth.sin(yaw);
        double x = this.getX() + mx * cos - mz * sin;
        double z = this.getZ() + mx * sin + mz * cos;

        this.level().addParticle(ModParticles.SEEPING_DRIP_HANG, x, this.getY() + my, z, 0, 0, 0);
        this.level().playLocalSound(x, this.getY()+my, z, ModSounds.SOUL_DRIP, SoundSource.HOSTILE, 1, .8f,false);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide()) {
            Player local = Minecraft.getInstance().player;
            if (local != null && this.distanceTo(local) < HeedlessProximity.FADE_START) {
                this.dripFromCysts();
            }
        }

        if (this.level().isClientSide()) return;

        Player player = this.level().getNearestPlayer(this, HeedlessProximity.FADE_START);
        if (player == null || player.isSpectator() || player.isCreative() || !this.hasLineOfSight(player)) {
            this.fuse = 0;
            return;
        }

        float dist = this.distanceTo(player);
        if (dist <= HeedlessProximity.FADE_FULL) this.attemptWarn();
        if (dist > HeedlessProximity.DETONATE) {
            this.fuse = 0;
            return;
        }

        if (++this.fuse >= 20) {
            this.fuse = 0;
            this.corruptNearby();
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.5f, Level.ExplosionInteraction.MOB);
            this.discard();
        }
        if (this.cooldown > 0) this.cooldown--;
    }

    private void attemptWarn() {
        if (this.cooldown > 0) return;
        this.playSound(ModSounds.HEEDLESS_WAKE);
        // Animations only play on the client; tell every nearby client to twitch
        this.level().broadcastEntityEvent(this, (byte) (WARN_EVENT + this.random.nextInt(WARN_VARIANTS)));
        this.cooldown = this.maxCooldown;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id >= WARN_EVENT && id < WARN_EVENT + WARN_VARIANTS) {
            this.warnVariant = id - WARN_EVENT;
            // start(), not startIfStopped(): restarts the twitch from frame 0 on every warning
            this.warnAnimation.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    /** Which twitch the current warning plays (0-2), or -1 before the first warning. Client-side. */
    public int getWarnVariant() {
        return this.warnVariant;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return super.mobInteract(player, hand);
        }

        if (!this.level().isClientSide()) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(
                    stack, player, new ItemStack(ModItems.VOLATILE_EXTRACT)));
            this.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private void corruptNearby() {
        float radius = 5f;
        AABB box = this.getBoundingBox().inflate(radius);
        for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e.isAlive())) {
            if(living.is(ModTags.Entities.SOULSCORCH_ENTITIES) && !(living.hasEffect(ModEffects.CAT_BUFF) || living.hasEffect(ModEffects.DOG_BUFF))) continue;
            if(living.distanceTo(this) > radius) continue;

            Corruption.add(living, 3d);
        }
    }

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public SoundEvent getAmbientSound() {
        return ModSounds.HEEDLESS_WHISPER.value();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) { return ModSounds.HEEDLESS_HURT; }

    @Override
    public SoundEvent getDeathSound() { return ModSounds.HEEDLESS_DEATH; }
}
