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
    public static final int AFTERMATH_TICKS = 40;

    public static final Map<ServerPlayer, Aftermath> AFTERMATH = new WeakHashMap<>();

    public static final class Aftermath {
        final long endTick;
        final int totemsUsedBefore;
        boolean totemPopped;

        public Aftermath(long endTick, int totemsUsedBefore) {
            this.endTick = endTick;
            this.totemsUsedBefore = totemsUsedBefore;
        }
    }

    /** Vanilla's own count of Totems of Undying this player has used. */
    public static int totemsUsed(ServerPlayer player) {
        return player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(AftermathTickHandler::tickAftermath);
    }

    private static void tickAftermath(MinecraftServer server) {
        if (AFTERMATH.isEmpty()) return;
        int now = server.getTickCount();

        Iterator<Map.Entry<ServerPlayer, Aftermath>> it = AFTERMATH.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ServerPlayer, Aftermath> entry = it.next();
            ServerPlayer player = entry.getKey();
            Aftermath aftermath = entry.getValue();

            boolean usedTotem = aftermath.totemPopped || totemsUsed(player) > aftermath.totemsUsedBefore;

            // Died, logged out, or a totem saved them: no advancement
            if (!player.isAlive() || player.isRemoved() || usedTotem) {
                if (usedTotem) {
                    Soulscorch.LOGGER.info("[time_bomb] {}: totem used (mixin={}, stat={}), no advancement",
                            player.getName().getString(), aftermath.totemPopped,
                            totemsUsed(player) > aftermath.totemsUsedBefore);
                }
                it.remove();
                continue;
            }
            if (now < aftermath.endTick) continue;

            it.remove();
            award(server, player);
        }
    }

    private static void award(MinecraftServer server, ServerPlayer player) {
        AdvancementHolder advancement = server.getAdvancements().get(Soulscorch.id("time_bomb"));
        if (advancement == null) return;
        for (String criterion : advancement.value().criteria().keySet()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
