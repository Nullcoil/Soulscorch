package net.nullcoil.soulscorch.entity.vehicle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class NetherBoat extends Boat {
    public NetherBoat(EntityType<? extends NetherBoat> entityType, Level level, Supplier<Item> supplier) {
        super(entityType, level, supplier);
    }

    public boolean fireImmune() {
        return true;
    }
}