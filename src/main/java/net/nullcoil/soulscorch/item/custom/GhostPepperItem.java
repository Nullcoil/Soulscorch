package net.nullcoil.soulscorch.item.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.nullcoil.nidalib.attribute.Corruption;
import org.jspecify.annotations.NonNull;

public class GhostPepperItem extends BlockItem {

    public GhostPepperItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof Player player) {

            player.hurt(player.damageSources().onFire(), 1f);
            // Cleanse 1 to 3 Corruption (never below 0)
            Corruption.remove(player, player.getRandom().nextInt(3) + 1);
        }
        return result;
    }
}