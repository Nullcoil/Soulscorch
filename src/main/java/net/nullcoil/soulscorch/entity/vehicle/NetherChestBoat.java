package net.nullcoil.soulscorch.entity.vehicle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class NetherChestBoat extends ChestBoat {
    public NetherChestBoat(EntityType<? extends NetherChestBoat> entityType, Level level, Supplier<Item> supplier) {
        super(entityType, level, supplier);
    }

    public boolean fireImmune() {
        return true;
    }
}