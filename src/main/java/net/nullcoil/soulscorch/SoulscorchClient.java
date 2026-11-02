package net.nullcoil.soulscorch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import net.nullcoil.soulscorch.entity.ModBoats;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.entity.client.blazt.BlaztModel;
import net.nullcoil.soulscorch.entity.client.blazt.BlaztRenderer;
import net.nullcoil.soulscorch.entity.client.companion.SoulCatRenderer;
import net.nullcoil.soulscorch.entity.client.companion.SoulWolfRenderer;
import net.nullcoil.soulscorch.entity.client.heedless.HeedlessModel;
import net.nullcoil.soulscorch.entity.client.heedless.HeedlessRenderer;
import net.nullcoil.soulscorch.entity.client.jellyfish.JellyfishModel;
import net.nullcoil.soulscorch.entity.client.jellyfish.JellyfishRenderer;
import net.nullcoil.soulscorch.entity.client.restless.RestlessManeModel;
import net.nullcoil.soulscorch.entity.client.restless.RestlessModel;
import net.nullcoil.soulscorch.entity.client.restless.RestlessRenderer;
import net.nullcoil.soulscorch.entity.client.soulless.SoullessModel;
import net.nullcoil.soulscorch.entity.client.soulless.SoullessRenderer;
import net.nullcoil.soulscorch.entity.vehicle.NetherBoatRenderer;
import net.nullcoil.soulscorch.particles.ModParticles;
import net.nullcoil.soulscorch.particles.SeepingDripParticle;
import net.nullcoil.soulscorch.screen.ModScreenHandlers;
import net.nullcoil.soulscorch.screen.SoulBrewingStandScreen;

public class SoulscorchClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.SOUL_BREWING_STAND, SoulBrewingStandScreen::new);

        BlockEntityRenderers.register(ModBlockEntities.SEEPING_SIGN, StandingSignRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.SALLOW_SIGN, StandingSignRenderer::new);

        ModelLayerRegistry.registerModelLayer(BlaztModel.BLAZT, BlaztModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.BLAZT, BlaztRenderer::new);

        EntityRendererRegistry.register(ModEntities.SOUL_CHARGE_PROJECTILE, net.minecraft.client.renderer.entity.ThrownItemRenderer::new);

        ModelLayerRegistry.registerModelLayer(SoullessModel.SOULLESS, SoullessModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SoullessModel.SOULLESS_BABY, SoullessModel::createBabyBodyLayer);
        EntityRendererRegistry.register(ModEntities.SOULLESS, SoullessRenderer::new);

        ModelLayerRegistry.registerModelLayer(RestlessModel.RESTLESS, RestlessModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(RestlessManeModel.MANE_LAYER, RestlessManeModel::createManeLayer);
        EntityRendererRegistry.register(ModEntities.RESTLESS, RestlessRenderer::new);

        ModelLayerRegistry.registerModelLayer(JellyfishModel.HYTODOM, JellyfishModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.HYTODOM, JellyfishRenderer::new);

        EntityRendererRegistry.register(ModEntities.SOULBORNE_CAT, SoulCatRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOULBORNE_WOLF, SoulWolfRenderer::new);

        ModelLayerRegistry.registerModelLayer(HeedlessModel.HEEDLESS, HeedlessModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.HEEDLESS, HeedlessRenderer::new);

        ParticleProviderRegistry.getInstance().register(ModParticles.SEEPING_DRIP_HANG, SeepingDripParticle.HangProvider::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SEEPING_DRIP_FALL, SeepingDripParticle.FallProvider::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SEEPING_DRIP_LAND, SeepingDripParticle.LandProvider::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SEEPING_SALLOW_LEAVES, FallingLeavesParticle.CherryProvider::new);

        ModelLayerRegistry.registerModelLayer(layer("boat/sallow"), BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("chest_boat/sallow"), BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("boat/crimson"), BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("chest_boat/crimson"), BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("boat/warped"), BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("chest_boat/warped"), BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("boat/seeping"), BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(layer("chest_boat/seeping"), BoatModel::createChestBoatModel);

        boat(ModBoats.SALLOW_BOAT.entityType(), layer("boat/sallow"));
        boat(ModBoats.SALLOW_CHEST_BOAT.entityType(), layer("chest_boat/sallow"));
        boat(ModBoats.CRIMSON_BOAT.entityType(), layer("boat/crimson"));
        boat(ModBoats.CRIMSON_CHEST_BOAT.entityType(), layer("chest_boat/crimson"));
        boat(ModBoats.WARPED_BOAT.entityType(), layer("boat/warped"));
        boat(ModBoats.WARPED_CHEST_BOAT.entityType(), layer("chest_boat/warped"));
        boat(ModBoats.SEEPING_BOAT.entityType(), layer("boat/seeping"));
        boat(ModBoats.SEEPING_CHEST_BOAT.entityType(), layer("chest_boat/seeping"));


    }

    @SuppressWarnings("unchecked")
    private static void boat(EntityType<? extends AbstractBoat> type, ModelLayerLocation layer) {
        EntityType<AbstractBoat> raw = (EntityType<AbstractBoat>) type;
        EntityRendererRegistry.register(raw, ctx -> new NetherBoatRenderer(ctx, layer));
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(Soulscorch.id(path), "main");
    }
}
