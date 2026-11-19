package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.custom.PlayerStatueBlock;
import net.nullcoil.soulscorch.block.entity.PlayerStatueBlockEntity;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.entity.ai.GhazeEntity;

/**
 * Witnessed: being watched by a Ghaze turns you to stone.
 *
 * <p>Unlike other effects, its duration is a <b>build-up</b>, not a timer. While a Ghaze can
 * see you, it keeps adding to it ({@link #witness}), faster than it runs down, so it climbs.
 * Out of every Ghaze's sight it runs down like any effect, and wears off at 0.
 *
 * <p>The higher it is, the slower you move (and jump), down to a standstill at
 * {@link #PETRIFY_TICKS}. At that point you're petrified: you die, and a grayscale statue of
 * you ({@link PlayerStatueBlock}) is left where you stood.
 */
public class WitnessedEffect extends MobEffect {
    /** Build-up at which you're fully petrified: 12 seconds of one Ghaze watching. */
    public static final int PETRIFY_TICKS = 240;
    /** Each tick a Ghaze sees you it adds this much; the effect itself drains 1 a tick, so one
     *  Ghaze nets +1 a tick, and every extra Ghaze watching adds 2 more. */
    private static final int WITNESS_PER_TICK = 2;

    private static final Identifier SLOW_ID = Soulscorch.id("witnessed_slowness");
    public static final ResourceKey<DamageType> PETRIFIED =
            ResourceKey.create(Registries.DAMAGE_TYPE, Soulscorch.id("petrified"));
    /** How far to look for the Ghaze to credit for a petrification (death message). */
    private static final double CREDIT_RANGE = 48.0;

    public WitnessedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** A Ghaze sees {@code target} this tick: build up their Witnessed. */
    public static void witness(LivingEntity target) {
        if (target.level().isClientSide()) return;
        MobEffectInstance current = target.getEffect(ModEffects.WITNESSED);
        int buildUp = current != null ? current.getDuration() : 0;
        int next = Math.min(buildUp + WITNESS_PER_TICK, PETRIFY_TICKS);
        // No particles, but shows its icon so the player knows what's happening
        target.addEffect(new MobEffectInstance(ModEffects.WITNESSED, next, 0, false, false, true));
    }

    /** How petrified a build-up is: 0 (fine) to 1 (stone). */
    public static float petrification(int buildUp) {
        return Math.min(1.0F, Math.max(0.0F, buildUp / (float) PETRIFY_TICKS));
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(ModEffects.WITNESSED);
        if (instance == null) return true;

        float stone = petrification(instance.getDuration());
        slow(entity, stone);

        if (stone >= 1.0F && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            petrify(level, entity);
            return false;
        }
        return true;
    }

    @Override
    public void onEffectRemoved(MobEffectInstance instance, LivingEntity entity) {
        slow(entity, 0.0F);
    }

    /** Scales movement and jumping by (1 - stone): unaffected at 0, frozen at 1. */
    private static void slow(LivingEntity entity, float stone) {
        setModifier(entity.getAttribute(Attributes.MOVEMENT_SPEED), stone);
        setModifier(entity.getAttribute(Attributes.JUMP_STRENGTH), stone);
    }

    private static void setModifier(AttributeInstance attribute, float stone) {
        if (attribute == null) return;
        if (stone <= 0.0F) {
            attribute.removeModifier(SLOW_ID);
        } else {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(SLOW_ID, -stone, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /**
     * Fully petrified: dies, and leaves a statue. The damage ignores armor and resistance, but
     * not a Totem of Undying, which saves you (and clears the effect, like any totem).
     */
    private static void petrify(ServerLevel level, LivingEntity entity) {
        GhazeEntity watcher = nearestWatcher(level, entity);
        DamageSource source = watcher != null
                ? entity.damageSources().source(PETRIFIED, watcher)
                : entity.damageSources().source(PETRIFIED);

        entity.hurtServer(level, source, Float.MAX_VALUE);

        if (entity.isDeadOrDying() && entity instanceof Player player) {
            placeStatue(level, player);
        }
    }

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
    private static GhazeEntity nearestWatcher(ServerLevel level, LivingEntity entity) {
        GhazeEntity nearest = null;
        double best = CREDIT_RANGE * CREDIT_RANGE;
        for (GhazeEntity ghaze : level.getEntitiesOfClass(GhazeEntity.class, entity.getBoundingBox().inflate(CREDIT_RANGE))) {
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
