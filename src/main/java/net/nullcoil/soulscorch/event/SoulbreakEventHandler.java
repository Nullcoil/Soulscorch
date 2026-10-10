package net.nullcoil.soulscorch.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.entity.ai.SoullessEntity;
import net.nullcoil.soulscorch.util.ModTags;

import java.util.*;

public class SoulbreakEventHandler {
    private static final double LISTENING_RANGE = 24.0;
    private static final double CHAIN_RANGE = 16.0;

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(SoulbreakEventHandler::onBlockBreak);
    }

    private static void onBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {

        // Server-only logic
        if (level.isClientSide()) return;

        // 1.21 uses .is() instead of .isIn()
        if (!state.is(ModTags.Blocks.SOULBASED_BLOCKS)) return;

        // 1.21 uses .hasEffect() instead of .hasStatusEffect()
        if (player.hasEffect(ModEffects.CAT_BUFF) || player.hasEffect(ModEffects.DOG_BUFF)) return;

        Set<SoullessEntity> visited = new HashSet<>();
        Queue<SoullessEntity> queue = new LinkedList<>();

        // Step 1: Find initial Soulless within LISTENING_RANGE
        Vec3 center = Vec3.atCenterOf(pos);
        AABB initialBox = new AABB(
                center.x - LISTENING_RANGE, center.y - LISTENING_RANGE, center.z - LISTENING_RANGE,
                center.x + LISTENING_RANGE, center.y + LISTENING_RANGE, center.z + LISTENING_RANGE
        );

        List<SoullessEntity> initialHits = level.getEntitiesOfClass(SoullessEntity.class, initialBox);
        for (SoullessEntity entity : initialHits) {
            if (visited.add(entity)) { // If successfully added to visited (meaning it wasn't there already)
                queue.add(entity);
            }
        }

        // Step 2: The BFS Hive-Mind Chain
        while (!queue.isEmpty()) {
            SoullessEntity soulless = queue.poll();

            // Wake the statue up!
            soulless.raiseActivity(player);

            // Find nearby Soulless within CHAIN_RANGE to relay the signal
            Vec3 soullessCenter = soulless.position();
            AABB chainBox = new AABB(
                    soullessCenter.x - CHAIN_RANGE, soullessCenter.y - CHAIN_RANGE, soullessCenter.z - CHAIN_RANGE,
                    soullessCenter.x + CHAIN_RANGE, soullessCenter.y + CHAIN_RANGE, soullessCenter.z + CHAIN_RANGE
            );

            List<SoullessEntity> chainHits = level.getEntitiesOfClass(SoullessEntity.class, chainBox);
            for (SoullessEntity entity : chainHits) {
                // By checking visited.add() BEFORE queueing, we prevent the queue from exploding
                // in size if multiple statues hear the same neighbor.
                if (visited.add(entity)) {
                    queue.add(entity);
                }
            }
        }
    }
}