package net.nullcoil.soulscorch.effect.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.event.AftermathTickHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class VolatileEffect extends MobEffect {


    public VolatileEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectRemoved(MobEffectInstance instance, LivingEntity entity) {
        if (instance.getDuration() > 0 || instance.isInfiniteDuration()) return;
        if (!(entity.level() instanceof ServerLevel level) || entity.isRemoved()) return;

        // Start watching before the blast, so a totem popped by the blast itself counts
        if (entity instanceof ServerPlayer player) {
            AftermathTickHandler.AFTERMATH.put(player, new AftermathTickHandler.Aftermath(level.getServer().getTickCount() + AftermathTickHandler.AFTERMATH_TICKS, AftermathTickHandler.totemsUsed(player)));
        }

        float radius = 2f + instance.getAmplifier() + 1;
        level.explode(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                radius, Level.ExplosionInteraction.MOB
        );
    }
}
