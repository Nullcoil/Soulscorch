package net.nullcoil.soulscorch.entity.client.ghaze;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.soulscorch.entity.ai.GhazeEntity;

/**
 * The Ghaze's gaze: while one is agitated at you and you can see each other, your camera
 * drifts toward the center of its head. Breaking line of sight ends it.
 *
 * <p>Runs every frame, right after the mouse turns the camera (see GhazeGazeMouseMixin), so
 * the pull is smooth and you can always fight it by moving the mouse.
 */
@Environment(EnvType.CLIENT)
public final class GhazeGaze {
    private GhazeGaze() {}

    /** Only Ghazes this close (blocks) can pull. */
    private static final double RANGE = 32.0;
    /** Share of the way to the Ghaze's face the camera turns per second (subtle). */
    private static final float PULL_PER_SECOND = 0.6F;
    /** Fastest the pull turns the camera, in degrees per second. */
    private static final float MAX_DEGREES_PER_SECOND = 25.0F;

    private static long lastFrameNanos = Util.getNanos();

    /** Called once per frame. */
    public static void tick(Minecraft minecraft) {
        long now = Util.getNanos();
        float seconds = Math.min((now - lastFrameNanos) / 1.0E9F, 0.1F);
        lastFrameNanos = now;

        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.isPaused() || minecraft.gui.screen() != null) return;
        if (player.isSpectator()) return;

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 eyes = player.getEyePosition(partialTick);

        // The nearest Ghaze glaring at this player that it can see
        GhazeEntity watcher = null;
        Vec3 face = null;
        double nearest = RANGE * RANGE;
        AABB area = player.getBoundingBox().inflate(RANGE);
        for (GhazeEntity ghaze : minecraft.level.getEntitiesOfClass(GhazeEntity.class, area)) {
            if (!ghaze.isAgitated() || ghaze.getGazeTargetId() != player.getId()) continue;
            Vec3 head = ghaze.getHeadPosition(partialTick);
            double distance = head.distanceToSqr(eyes);
            if (distance >= nearest) continue;
            HitResult hit = minecraft.level.clip(new ClipContext(eyes, head,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) continue;
            watcher = ghaze;
            face = head;
            nearest = distance;
        }
        if (watcher == null) return;

        Vec3 toFace = face.subtract(eyes);
        float wantYaw = (float) (Mth.atan2(toFace.z, toFace.x) * (180.0 / Math.PI)) - 90.0F;
        float wantPitch = (float) (-(Mth.atan2(toFace.y, toFace.horizontalDistance()) * (180.0 / Math.PI)));

        float yawError = Mth.wrapDegrees(wantYaw - player.getYRot());
        float pitchError = wantPitch - player.getXRot();

        // Turn a share of the way there, scaled by frame time so it's the same at any frame rate
        float share = 1.0F - (float) Math.exp(-PULL_PER_SECOND * seconds);
        float maxStep = MAX_DEGREES_PER_SECOND * seconds;
        float yawStep = Mth.clamp(yawError * share, -maxStep, maxStep);
        float pitchStep = Mth.clamp(pitchError * share, -maxStep, maxStep);

        player.setYRot(player.getYRot() + yawStep);
        player.setXRot(Mth.clamp(player.getXRot() + pitchStep, -90.0F, 90.0F));
        // Move the previous rotation along too, so the turn doesn't get smoothed backward
        player.yRotO += yawStep;
        player.xRotO += pitchStep;
    }
}
