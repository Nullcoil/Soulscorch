package net.nullcoil.soulscorch.effect.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.nidalib.effect.BuildupEffect;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.custom.PlayerStatueBlock;
import net.nullcoil.soulscorch.block.entity.PlayerStatueBlockEntity;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.entity.ai.ghaze.Ghaze;
import net.nullcoil.soulscorch.sound.ModSounds;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Witnessed: being watched by a Ghaze turns you to stone.
 *
 * <p>A {@link BuildupEffect}: while a Ghaze can see you, it builds up ({@link #witness}), faster
 * than it drains, so it climbs. Out of every Ghaze's sight it drains, and wears off at 0.
 *
 * <p>Only the <b>closest</b> Ghaze that can see you adds to it, so a crowd of them is no faster
 * than one. Looking back at that Ghaze makes it build faster ({@link #LOOKING_PER_TICK}).
 * While it climbs, your skin cracks: a stone-cracking sound that gets louder and more frequent.
 *
 * <p>The higher it is, the slower you move (jumping isn't affected), down to a standstill at
 * {@link #PETRIFY_TICKS}. At that point you're petrified: you die, and a grayscale statue of
 * you ({@link PlayerStatueBlock}) is left where you stood.
 */
public class WitnessedEffect extends BuildupEffect {
    /** Build-up at which you're fully petrified. */
    public static final int PETRIFY_TICKS = 240;
    /**
     * Added each tick the closest Ghaze sees you. The effect itself drains 1 a tick, so these net
     * +1 (12 seconds to petrify) while you look away, and +2 (6 seconds) while you look at it.
     */
    private static final int WITNESS_PER_TICK = 2;
    private static final int LOOKING_PER_TICK = 3;
    /** Cosine of how far off-center the Ghaze's head can be and still count as looked at (25 degrees). */
    private static final double LOOKING_COS = Math.cos(Math.toRadians(25.0));

    private static final Identifier SLOW_ID = Soulscorch.id("witnessed_slowness");
    public static final ResourceKey<DamageType> PETRIFIED =
            ResourceKey.create(Registries.DAMAGE_TYPE, Soulscorch.id("petrified"));
    /** How far to look for the Ghaze to credit for a petrification (death message). */
    private static final double CREDIT_RANGE = Ghaze.WATCH_RADIUS;

    public WitnessedEffect(MobEffectCategory category, int color) {
        super(category, color, PETRIFY_TICKS);
    }

    /** This tick's closest Ghaze that can see each target. Server thread only; cleared every tick. */
    private static final Map<LivingEntity, Ghaze> WATCHERS = new WeakHashMap<>();

    /** Applies the build-up once a tick, after every Ghaze has had its turn. */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (WATCHERS.isEmpty()) return;
            WitnessedEffect effect = (WitnessedEffect) ModEffects.WITNESSED.value();
            WATCHERS.forEach((target, ghaze) ->
                    effect.buildUp(target, isLookingAt(target, ghaze) ? LOOKING_PER_TICK : WITNESS_PER_TICK));
            WATCHERS.clear();
        });
    }

    /**
     * {@code ghaze} can see {@code target} this tick. Only the closest Ghaze that sees them
     * counts; the build-up itself happens at the end of the tick.
     */
    public static void witness(LivingEntity target, Ghaze ghaze) {
        if (target.level().isClientSide()) return;
        WATCHERS.merge(target, ghaze, (current, candidate) ->
                candidate.distanceToSqr(target) < current.distanceToSqr(target) ? candidate : current);
    }

    /** Whether {@code target} is looking at the Ghaze's head (within {@link #LOOKING_COS}). */
    private static boolean isLookingAt(LivingEntity target, Ghaze ghaze) {
        Vec3 toHead = ghaze.getHeadPosition(1.0F).subtract(target.getEyePosition());
        if (toHead.lengthSqr() < 1.0E-6) return true;
        return target.getViewVector(1.0F).dot(toHead.normalize()) > LOOKING_COS;
    }

    /** How petrified a build-up is: 0 (fine) to 1 (stone). Kept for callers outside the effect. */
    public static float petrification(int buildUp) {
        return Math.min(1.0F, Math.max(0.0F, buildUp / (float) PETRIFY_TICKS));
    }

    // ---- BuildupEffect hooks ----

    @Override
    protected void onBuildupTick(ServerLevel level, LivingEntity entity, int amplifier, float progress) {
        slow(entity, progress);
    }

    /**
     * Stone cracking, heard by everyone nearby: rare and quiet at first, then louder, lower and
     * more frequent as you stiffen (on average every 2 seconds at first, every 4 ticks at the end).
     * Only while it's climbing, so silence means you got away.
     */
    @Override
    protected void onBuiltUp(LivingEntity target, float stone, int amplifier) {
        if (target.getRandom().nextFloat() >= 0.025F + 0.225F * stone) return;
        float volume = 0.2F + 0.8F * stone;
        float pitch = 1.2F - 0.5F * stone + (target.getRandom().nextFloat() - 0.5F) * 0.1F;
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.WITNESSED_CRACK, SoundSource.PLAYERS, volume, pitch);
    }

    /**
     * Fully petrified: dies, and leaves a statue. The damage ignores armor and resistance, but
     * not a Totem of Undying, which saves you (and clears the effect, like any totem).
     */
    @Override
    protected void onPeak(ServerLevel level, LivingEntity entity, int amplifier) {
        Ghaze watcher = nearestWatcher(level, entity);
        DamageSource source = watcher != null
                ? entity.damageSources().source(PETRIFIED, watcher)
                : entity.damageSources().source(PETRIFIED);

        entity.hurtServer(level, source, Float.MAX_VALUE);

        if (entity.isDeadOrDying() && entity instanceof Player player) {
            placeStatue(level, player);
        }
    }

    @Override
    protected void onReset(LivingEntity entity) {
        slow(entity, 0.0F);
    }

    // ---- Slowness ----

    /**
     * Scales movement speed by (1 - stone): unaffected at 0, frozen at 1. Jumping is left
     * alone, so you can still hop up a block to break line of sight and get away.
     */
    private static void slow(LivingEntity entity, float stone) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        if (stone <= 0.0F) {
            attribute.removeModifier(SLOW_ID);
        } else {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(SLOW_ID, -stone, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    // ---- Statue ----

    /**
     * Leaves a statue of the player where they stood, facing the way they faced. Needs two
     * free (replaceable) blocks; tries their spot, then up to two blocks higher (e.g. if they
     * died standing in tall grass under a ledge). If none fits, there's no statue.
     */
    private static void placeStatue(ServerLevel level, Player player) {
        BlockPos feet = player.blockPosition();
        for (int up = 0; up <= 2; up++) {
            BlockPos lower = feet.above(up);
            BlockPos upper = lower.above();
            if (!fits(level, lower) || !fits(level, upper)) continue;

            BlockState lowerState = ModBlocks.PLAYER_STATUE.defaultBlockState()
                    .setValue(PlayerStatueBlock.FACING, player.getDirection())
                    .setValue(PlayerStatueBlock.WATERLOGGED, level.getFluidState(lower).is(Fluids.WATER));
            level.setBlock(lower, lowerState, Block.UPDATE_ALL);
            level.setBlock(upper, PlayerStatueBlock.upperHalf(lowerState, level.getFluidState(upper).is(Fluids.WATER)), Block.UPDATE_ALL);

            if (level.getBlockEntity(lower) instanceof PlayerStatueBlockEntity statue) {
                statue.setOwner(ResolvableProfile.createResolved(player.getGameProfile()));
            }
            return;
        }
    }

    private static boolean fits(ServerLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && !state.is(ModBlocks.PLAYER_STATUE);
    }

    /** The closest agitated Ghaze glaring at this entity, for the death message. */
    private static Ghaze nearestWatcher(ServerLevel level, LivingEntity entity) {
        Ghaze nearest = null;
        double best = CREDIT_RANGE * CREDIT_RANGE;
        for (Ghaze ghaze : level.getEntitiesOfClass(Ghaze.class, entity.getBoundingBox().inflate(CREDIT_RANGE))) {
            if (ghaze.getTarget() != entity) continue;
            double distance = ghaze.distanceToSqr(entity);
            if (distance < best) {
                best = distance;
                nearest = ghaze;
            }
        }
        return nearest;
    }
}