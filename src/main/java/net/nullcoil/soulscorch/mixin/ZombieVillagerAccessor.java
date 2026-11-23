package net.nullcoil.soulscorch.mixin;

import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** What a Zombie Villager remembers of the villager it was, for Asclepiosis's cure. */
@Mixin(ZombieVillager.class)
public interface ZombieVillagerAccessor {
    @Accessor("gossips")
    @Nullable GossipContainer soulscorch$getGossips();

    @Accessor("tradeOffers")
    @Nullable MerchantOffers soulscorch$getTradeOffers();
}
