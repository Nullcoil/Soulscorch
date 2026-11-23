package net.nullcoil.soulscorch.entity;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.nullcoil.soulscorch.Soulscorch;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class VillagerInspiration {
    private VillagerInspiration() {}

    public static final int REPUTATION = 125;

    public static int topUp(Villager villager, UUID player) {
        int fromCuring = villager.getGossips().getReputation(player,
                type -> type == GossipType.MAJOR_POSITIVE || type == GossipType.MINOR_POSITIVE);
        return Math.max(0, REPUTATION - Math.max(0, fromCuring));
    }

    private static final AttachmentType<Boolean> INSPIRED = AttachmentRegistry.create(
            Soulscorch.id("inspired"),
            builder -> builder.persistent(Codec.BOOL)
    );

    public static void register() {}

    public static boolean isInspired(Villager villager) {
        return Boolean.TRUE.equals(villager.getAttached(INSPIRED));
    }

    public static void inspire(Villager villager) {
        villager.setAttached(INSPIRED, true);
    }

    public static void clear(Villager villager) {
        villager.removeAttached(INSPIRED);
    }
}
