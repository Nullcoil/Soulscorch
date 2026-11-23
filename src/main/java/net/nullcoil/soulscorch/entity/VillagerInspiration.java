package net.nullcoil.soulscorch.entity;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.nullcoil.soulscorch.Soulscorch;

/**
 * Inspired: a villager cured by Asclepiosis gives every player a discount, half the size of
 * the one vanilla's cure gives the player who cured it.
 *
 * <p>The mark is saved with the villager, but not carried over when it's zombified again (a
 * Fabric attachment without {@code copyOnDeath}). So it can't be stacked with vanilla's cure:
 * zombify an Inspired villager and cure it with Weakness and a Golden Apple, and it loses
 * Inspired and gives only the curing player's discount. Cure it with Asclepiosis again and
 * it's Inspired again, which is the same one discount it had.
 */
@SuppressWarnings("UnstableApiUsage")
public final class VillagerInspiration {
    private VillagerInspiration() {}

    /**
     * How much the discount counts for, in reputation points: half of vanilla's cure (major
     * positive 20 × 5 + minor positive 25 = 125). Each offer's price drops by this times its
     * price multiplier, like reputation: 3 emeralds on most trades, instead of the cure's 6.
     */
    public static final int REPUTATION = 62;

    private static final AttachmentType<Boolean> INSPIRED = AttachmentRegistry.create(
            Soulscorch.id("inspired"),
            builder -> builder.persistent(Codec.BOOL)
    );

    /** Loads the attachment type; call during mod init. */
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
