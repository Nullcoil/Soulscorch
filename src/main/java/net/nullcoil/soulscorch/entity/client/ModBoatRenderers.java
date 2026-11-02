package net.nullcoil.soulscorch.entity.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ModBoats;

public final class ModBoatRenderers {
    public static final ModelLayerLocation SEEPING_BOAT = layer("boat/seeping");
    public static final ModelLayerLocation SEEPING_CHEST_BOAT = layer("chest_boat/seeping");
    public static final ModelLayerLocation SALLOW_BOAT = layer("boat/sallow");
    public static final ModelLayerLocation SALLOW_CHEST_BOAT = layer("chest_boat/sallow");
    public static final ModelLayerLocation CRIMSON_BOAT = layer("boat/crimson");
    public static final ModelLayerLocation CRIMSON_CHEST_BOAT = layer("chest_boat/crimson");
    public static final ModelLayerLocation WARPED_BOAT = layer("boat/warped");
    public static final ModelLayerLocation WARPED_CHEST_BOAT = layer("chest_boat/warped");

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(Soulscorch.id(path), "main");
    }

    public static void register() {
        ModelLayerRegistry.registerModelLayer(SEEPING_BOAT, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(SALLOW_BOAT, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(CRIMSON_BOAT, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(WARPED_BOAT, BoatModel::createBoatModel);

        ModelLayerRegistry.registerModelLayer(SEEPING_CHEST_BOAT, BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(SALLOW_CHEST_BOAT, BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(CRIMSON_CHEST_BOAT, BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(WARPED_CHEST_BOAT, BoatModel::createChestBoatModel);

        boat(ModBoats.SEEPING_BOAT.entityType(), SEEPING_BOAT);
        boat(ModBoats.SALLOW_BOAT.entityType(), SALLOW_BOAT);
        boat(ModBoats.CRIMSON_BOAT.entityType(), CRIMSON_BOAT);
        boat(ModBoats.WARPED_BOAT.entityType(), WARPED_BOAT);

        boat(ModBoats.SEEPING_CHEST_BOAT.entityType(), SEEPING_CHEST_BOAT);
        boat(ModBoats.SALLOW_CHEST_BOAT.entityType(), SALLOW_CHEST_BOAT);
        boat(ModBoats.CRIMSON_CHEST_BOAT.entityType(), CRIMSON_CHEST_BOAT);
        boat(ModBoats.WARPED_CHEST_BOAT.entityType(), WARPED_CHEST_BOAT);
    }

    private static void boat(EntityType<? extends AbstractBoat> type, ModelLayerLocation layer) {
        EntityRendererRegistry.register(type, ctx -> new BoatRenderer(ctx, layer));
    }
}