package net.nullcoil.soulscorch.effect.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.effect.custom.WitnessedEffect;
import net.nullcoil.soulscorch.mixin.client.PostChainAccessor;
import net.nullcoil.soulscorch.mixin.client.PostPassAccessor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Witnessed, as you see it: the world drains to grayscale as you turn to stone (fully gray at the
 * moment of petrification). Only the world and your hand; the HUD keeps its color.
 *
 * <p>It's a post effect ({@code soulscorch:petrified}), run right after the world is drawn. Post
 * effect uniforms are normally fixed values from the JSON, so the first time the chain is used
 * its {@code PetrifiedConfig} buffer is swapped for one this class can write every frame.
 */
@Environment(EnvType.CLIENT)
public final class PetrifiedVision {
    private PetrifiedVision() {}

    private static final Identifier CHAIN_ID = Soulscorch.id("petrified");
    /** The uniform block in petrified.fsh, and in the post effect JSON. */
    private static final String UNIFORM = "PetrifiedConfig";
    /** std140 rounds a block up to 16 bytes. */
    private static final int UNIFORM_SIZE = 16;

    /** The chain whose uniform was swapped, and the swapped-in buffer. A resource reload makes a
     *  new chain (and closes the old one along with this buffer), so it's swapped again. */
    private static @Nullable PostChain boundChain;
    private static @Nullable GpuBuffer buffer;

    /** Called by GameRendererMixin right after the world (and hand) are drawn. */
    public static void render(Minecraft minecraft, RenderTarget mainTarget, GraphicsResourceAllocator resourcePool) {
        float stone = petrification(minecraft);
        if (stone <= 0.0F) return;

        PostChain chain = minecraft.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) return;
        if (chain != boundChain && !bind(chain)) return;

        write(stone);
        chain.process(mainTarget, resourcePool);
    }

    /** How petrified the local player is, 0 to 1. */
    private static float petrification(Minecraft minecraft) {
        if (minecraft.player == null) return 0.0F;
        MobEffectInstance witnessed = minecraft.player.getEffect(ModEffects.WITNESSED);
        return witnessed == null ? 0.0F : WitnessedEffect.petrification(witnessed.getDuration());
    }

    /** Swaps the chain's fixed PetrifiedConfig buffer for a writable one. */
    private static boolean bind(PostChain chain) {
        for (PostPass pass : ((PostChainAccessor) chain).soulscorch$getPasses()) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).soulscorch$getCustomUniforms();
            GpuBuffer fixed = uniforms.get(UNIFORM);
            if (fixed == null) continue;

            buffer = RenderSystem.getDevice().createBuffer(() -> "Soulscorch petrified vision",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UNIFORM_SIZE);
            uniforms.put(UNIFORM, buffer);
            fixed.close();
            boundChain = chain;
            return true;
        }
        Soulscorch.LOGGER.error("Post effect {} has no {} uniform block", CHAIN_ID, UNIFORM);
        boundChain = chain; // don't look again every frame
        buffer = null;
        return false;
    }

    private static void write(float stone) {
        if (buffer == null || buffer.isClosed()) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE).putFloat(stone).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
        }
    }
}
