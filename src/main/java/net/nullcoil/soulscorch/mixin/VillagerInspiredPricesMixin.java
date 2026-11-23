package net.nullcoil.soulscorch.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.nullcoil.soulscorch.entity.VillagerInspiration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * An Inspired villager gives everyone a cure's worth of discount, minus any cure discount
 * they already have with it. Applied each time trading starts, like vanilla's reputation discount.
 */
@Mixin(Villager.class)
public abstract class VillagerInspiredPricesMixin {
    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void soulscorch$inspiredDiscount(Player player, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (!VillagerInspiration.isInspired(villager)) return;

        int reputation = VillagerInspiration.topUp(villager, player.getUUID());
        if (reputation <= 0) return;
        for (MerchantOffer offer : villager.getOffers()) {
            offer.addToSpecialPriceDiff(-Mth.floor(reputation * offer.getPriceMultiplier()));
        }
    }
}
