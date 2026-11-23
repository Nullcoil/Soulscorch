package net.nullcoil.soulscorch.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.mixin.ZombieVillagerAccessor;

/**
 * The mob changes Asclepiosis and Thanatosis make when their countdowns run out.
 *
 * <p>Mobs are matched by their exact type, not their class: a Soulless is a Zombified Piglin
 * underneath, and has to take its own step instead of jumping straight to Piglin.
 */
public final class MobConversions {
    private MobConversions() {}

    /** Level event: the sound of a Zombie Villager being cured. */
    private static final int CURED_SOUND = 1027;
    /** Level event: the sound of a Villager being zombified. */
    private static final int ZOMBIFIED_SOUND = 1026;

    /**
     * One step of Asclepiosis's cure: Heedless → Soulless → Zombified Piglin → Piglin,
     * Zombie Villager → Villager, Restless → Zoglin → Hoglin. Anything else is left alone.
     */
    public static void cure(ServerLevel level, LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return;
        EntityType<?> type = mob.getType();

        if (mob instanceof ZombieVillager zombieVillager) {
            cureVillager(level, zombieVillager);
        } else if (type == ModEntities.HEEDLESS) {
            cureInto(level, mob, ModEntities.SOULLESS, true);
        } else if (type == ModEntities.SOULLESS) {
            cureInto(level, mob, EntityTypes.ZOMBIFIED_PIGLIN, true);
        } else if (type == EntityTypes.ZOMBIFIED_PIGLIN) {
            cureInto(level, mob, EntityTypes.PIGLIN, true);
        } else if (type == ModEntities.RESTLESS) {
            cureInto(level, mob, EntityTypes.ZOGLIN, false);
        } else if (type == EntityTypes.ZOGLIN) {
            cureInto(level, mob, EntityTypes.HOGLIN, false);
        }
    }

    /**
     * Thanatosis: Villagers become Zombie Villagers, Piglins (Brutes too) become Zombified
     * Piglins. Done the way vanilla does it, so a zombified villager keeps its trades and can
     * be cured back. Anything else is left alone.
     */
    public static void zombify(ServerLevel level, LivingEntity entity) {
        if (entity instanceof Villager villager) {
            // Not carried over anyway (see VillagerInspiration); cleared here to be sure
            VillagerInspiration.clear(villager);
            villager.convertTo(EntityTypes.ZOMBIE_VILLAGER, ConversionParams.single(villager, true, true), zombie -> {
                zombie.setVillagerDataFinalized(villager.getVillagerDataFinalized());
                zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()),
                        EntitySpawnReason.CONVERSION, new Zombie.ZombieGroupData(false, true));
                zombie.setVillagerData(villager.getVillagerData());
                zombie.setGossips(villager.getGossips().copy());
                zombie.setTradeOffers(villager.getOffers().copy());
                zombie.setVillagerXp(villager.getVillagerXp());
                if (!villager.isSilent()) {
                    level.levelEvent((Entity) null, ZOMBIFIED_SOUND, villager.blockPosition(), 0);
                }
            });
        } else if (entity instanceof AbstractPiglin piglin) {
            if (!piglin.isSilent()) {
                level.playSound(null, piglin.getX(), piglin.getY(), piglin.getZ(),
                        SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED, piglin.getSoundSource(), 1.0F, 1.0F);
            }
            piglin.convertTo(EntityTypes.ZOMBIFIED_PIGLIN, ConversionParams.single(piglin, true, true),
                    zombified -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)));
        }
    }

    /**
     * Vanilla's Zombie Villager cure (ZombieVillager.finishConversion), keeping its trades,
     * gossip and level, but with no player behind it: instead of one player's curing discount,
     * the villager comes out Inspired, a smaller discount for everyone.
     */
    private static void cureVillager(ServerLevel level, ZombieVillager zombie) {
        ZombieVillagerAccessor memory = (ZombieVillagerAccessor) zombie;
        GossipContainer gossips = memory.soulscorch$getGossips();
        MerchantOffers offers = memory.soulscorch$getTradeOffers();

        zombie.convertTo(EntityTypes.VILLAGER, ConversionParams.single(zombie, false, false), villager -> {
            for (EquipmentSlot slot : zombie.dropPreservedEquipment(level,
                    stack -> !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))) {
                SlotAccess offsetSlot = villager.getSlot(slot.getIndex() + 300);
                if (offsetSlot != null) offsetSlot.set(zombie.getItemBySlot(slot));
            }

            villager.setVillagerDataFinalized(zombie.getVillagerDataFinalized());
            villager.setVillagerData(zombie.getVillagerData());
            if (gossips != null) villager.setGossips(gossips);
            if (offers != null) villager.setOffers(offers.copy());
            villager.setVillagerXp(zombie.getVillagerXp());
            villager.finalizeSpawn(level, level.getCurrentDifficultyAt(villager.blockPosition()),
                    EntitySpawnReason.CONVERSION, (SpawnGroupData) null);
            villager.refreshBrain(level);

            villager.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
            villager.addEffect(new MobEffectInstance(ModEffects.INSPIRED, 1, 0));
            if (!zombie.isSilent()) {
                level.levelEvent((Entity) null, CURED_SOUND, zombie.blockPosition(), 0);
            }
        });
    }

    /** Swaps {@code mob} for a {@code next}, keeping its name, baby-ness and (optionally) gear. */
    private static <T extends Mob> void cureInto(ServerLevel level, Mob mob, EntityType<T> next, boolean keepEquipment) {
        T cured = mob.convertTo(next, ConversionParams.single(mob, keepEquipment, keepEquipment),
                newMob -> newMob.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)));
        if (cured != null && !mob.isSilent()) {
            level.levelEvent((Entity) null, CURED_SOUND, cured.blockPosition(), 0);
        }
    }
}
