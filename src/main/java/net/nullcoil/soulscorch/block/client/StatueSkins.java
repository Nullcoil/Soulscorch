package net.nullcoil.soulscorch.block.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.Soulscorch;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Grayscale copies of player skins, for statues. Made the first time a skin is needed and
 * kept for the session.
 *
 * <p>Downloaded skins are registered as {@link DynamicTexture}s, which keep their pixels in
 * memory, so those are copied straight from the texture. Built-in skins (Steve, Alex and the
 * rest) are read from the resource packs.
 */
@Environment(EnvType.CLIENT)
public final class StatueSkins {
    private StatueSkins() {}

    private static final Map<Identifier, Identifier> GRAY = new HashMap<>();

    /** The grayscale copy of a skin texture (made now if needed), or the skin itself if it can't be read. */
    public static Identifier gray(Identifier skin) {
        Identifier made = GRAY.get(skin);
        if (made != null) return made;

        Minecraft minecraft = Minecraft.getInstance();
        Identifier id = Soulscorch.id("statue_skin/" + skin.getNamespace() + "/" + skin.getPath());
        NativeImage gray;

        AbstractTexture texture = minecraft.getTextureManager().getTexture(skin);
        if (texture instanceof DynamicTexture dynamic && dynamic.getPixels() != null) {
            gray = grayscale(dynamic.getPixels());
        } else {
            try (InputStream in = minecraft.getResourceManager().open(skin);
                 NativeImage source = NativeImage.read(in)) {
                gray = grayscale(source);
            } catch (IOException e) {
                Soulscorch.LOGGER.warn("Couldn't read skin {} for a statue", skin, e);
                GRAY.put(skin, skin);
                return skin;
            }
        }

        minecraft.getTextureManager().register(id, new DynamicTexture(id::toString, gray));
        GRAY.put(skin, id);
        return id;
    }

    /**
     * A stone-gray copy: brightness only, squeezed into a narrower, lighter range so dark
     * skins still read as stone instead of black. Transparency (the hat and jacket layers) kept.
     */
    private static NativeImage grayscale(NativeImage source) {
        NativeImage result = new NativeImage(source.getWidth(), source.getHeight(), true);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getPixel(x, y);
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                float luma = 0.2126F * r + 0.7152F * g + 0.0722F * b;
                int v = Math.min(255, Math.round(48 + luma * 0.72F));
                result.setPixel(x, y, (argb & 0xFF000000) | (v << 16) | (v << 8) | v);
            }
        }
        return result;
    }
}
