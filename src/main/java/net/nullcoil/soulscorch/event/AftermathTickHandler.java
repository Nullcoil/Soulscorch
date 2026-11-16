package net.nullcoil.soulscorch.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Items;
import net.nullcoil.soulscorch.Soulscorch;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class AftermathTickHandler {
    private static final int AFTERMATH_TICKS = 40;

    private record Aftermath(long endTick, int totemsUsedBefore) {}

    private static final Map<ServerPlayer, Aftermath> AFTERMATH = new WeakHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(AftermathTickHandler::tickAftermath);
    }

    public static void watch(ServerPlayer player) {
        int now = player.level().getServer().getTickCount();
        AFTERMATH.put(player, new Aftermath(now + AFTERMATH_TICKS, totemsUsed(player)));
    }

    private static void tickAftermath(MinecraftServer server) {
        if (AFTERMATH.isEmpty()) return;
        int now = server.getTickCount();

        Iterator<Map.Entry<ServerPlayer, Aftermath>> it = AFTERMATH.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ServerPlayer, Aftermath> entry = it.next();
            ServerPlayer player = entry.getKey();
            Aftermath aftermath = entry.getValue();

            // Died, logged out, or a totem saved them: no advancement
            if (!player.isAlive() || player.isRemoved() || totemsUsed(player) > aftermath.totemsUsedBefore()) {
                it.remove();
                continue;
            }
            if (now < aftermath.endTick()) continue;

            it.remove();
            award(server, player);
        }
    }

    private static int totemsUsed(ServerPlayer player) {
        return player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
    }

    private static void award(MinecraftServer server, ServerPlayer player) {
        AdvancementHolder advancement = server.getAdvancements().get(Soulscorch.id("time_bomb"));
        if (advancement == null) return;
        for (String criterion : advancement.value().criteria().keySet()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
