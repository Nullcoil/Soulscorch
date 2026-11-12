package net.nullcoil.soulscorch.client.lava;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The lava heat scale, coolest to hottest. Each stage is the active lava texture
 * run through a hue/saturation/brightness recipe, so every stage keeps the
 * original's light-and-dark variation instead of being flattened into one color.
 *
 * <p>Heat values sit between stages (e.g. 2.3 = 70% yellow, 30% white),
 * and the renderer crossfades the two neighbors.
 */
@Environment(EnvType.CLIENT)
public enum LavaStage {
    //      name        hue shift  saturation  brightness  lift to white  mirror to hue
    RED     ("red",       -18f,       1.10f,       0.82f,       0.00f,         -1f),
    VANILLA ("vanilla",     0f,       1.00f,       1.00f,       0.00f,         -1f),
    YELLOW  ("yellow",     22f,       0.95f,       1.12f,       0.00f,         -1f),
    WHITE   ("white",       0f,       0.12f,       1.00f,       0.65f,         -1f),
    INFINITE("infinite",    0f,       0.35f,       1.00f,       0.55f,        215f),
    // Soul matches a GIMP Hue-Chroma edit of vanilla lava, see SOUL_LCH below
    SOUL    ("soul",        0f,       1.00f,       1.00f,       0.00f,         -1f);

    /**
     * GIMP "Hue-Chroma" settings for the soul stage, applied in CIE LCh like GIMP does,
     * then blended over the original with "Replace" at the given opacity.
     * hue shift (degrees), chroma (+/-), lightness (+/-), opacity (0..1)
     */
    private static final LchRecipe SOUL_LCH = new LchRecipe(145.61f, 64.97f, 17.20f, 1.0f);

    /**
     * Roughly the middle hue of vanilla lava (between its red shadows and yellow highlights).
     * "Mirror to hue" flips the lava's hues around this point onto a new center, so the
     * shadows and highlights trade places: red shadows become deep blue, yellow highlights
     * become cyan. That reads like soul fire instead of a straight recolor.
     */
    private static final float LAVA_CENTER_HUE = 25f;

    public final String id;
    private final float hueShift;
    private final float saturation;
    private final float brightness;
    private final float liftToWhite;
    private final float mirrorHue;

    LavaStage(String id, float hueShift, float saturation, float brightness, float liftToWhite, float mirrorHue) {
        this.id = id;
        this.hueShift = hueShift;
        this.saturation = saturation;
        this.brightness = brightness;
        this.liftToWhite = liftToWhite;
        this.mirrorHue = mirrorHue;
    }

    public static final int MAX = values().length - 1;

    /** Vanilla is drawn with the untouched active texture; every other stage gets a generated copy. */
    public boolean isGenerated() {
        return this != VANILLA;
    }

    /** Applies this stage's recipe to one ARGB pixel. */
    public int apply(int argb) {
        if (this == SOUL) return SOUL_LCH.apply(argb);

        int a = (argb >>> 24) & 0xFF;
        float[] hsv = rgbToHsv((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);

        float h = hsv[0];
        if (mirrorHue >= 0) h = mirrorHue - (h - LAVA_CENTER_HUE);
        h += hueShift;

        float s = clamp01(hsv[1] * saturation);
        float v = clamp01(hsv[2] * brightness);
        v = 1f - (1f - v) * (1f - liftToWhite); // keeps relative darkness, just closer to white

        return (a << 24) | hsvToRgb(h, s, v);
    }

    private static float clamp01(float f) {
        return Math.max(0f, Math.min(1f, f));
    }

    private static float[] rgbToHsv(int r8, int g8, int b8) {
        float r = r8 / 255f, g = g8 / 255f, b = b8 / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;

        float h;
        if (d == 0) h = 0;
        else if (max == r) h = 60f * (((g - b) / d) % 6f);
        else if (max == g) h = 60f * (((b - r) / d) + 2f);
        else h = 60f * (((r - g) / d) + 4f);

        return new float[]{h, max == 0 ? 0 : d / max, max};
    }

    private static int hsvToRgb(float h, float s, float v) {
        h = ((h % 360f) + 360f) % 360f;
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = v - c;

        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }

        int ri = Math.round((r + m) * 255), gi = Math.round((g + m) * 255), bi = Math.round((b + m) * 255);
        return (ri << 16) | (gi << 8) | bi;
    }

    /**
     * GIMP's Hue-Chroma filter: shifts hue and adds chroma and lightness in CIE LCh(ab)
     * (D50, like GIMP/babl), then mixes with the original in linear light ("Replace" mode).
     */
    private record LchRecipe(float hue, float chroma, float lightness, float opacity) {
        // sRGB (linear) <-> XYZ, D50-adapted
        private static final double[][] TO_XYZ = {
                {0.4360747, 0.3850649, 0.1430804},
                {0.2225045, 0.7168786, 0.0606169},
                {0.0139322, 0.0971045, 0.7141733}};
        private static final double[][] FROM_XYZ = {
                {3.1338561, -1.6168667, -0.4906146},
                {-0.9787684, 1.9161415, 0.0334540},
                {0.0719453, -0.2289914, 1.4052427}};
        private static final double XN = 0.9642, YN = 1.0, ZN = 0.8249;
        private static final double EPS = 216.0 / 24389.0, KAPPA = 24389.0 / 27.0;

        int apply(int argb) {
            int a = (argb >>> 24) & 0xFF;
            double[] lin = {toLinear((argb >> 16) & 0xFF), toLinear((argb >> 8) & 0xFF), toLinear(argb & 0xFF)};
            double[] xyz = mul(TO_XYZ, lin);

            double fx = f(xyz[0] / XN), fy = f(xyz[1] / YN), fz = f(xyz[2] / ZN);
            double l = 116 * fy - 16, labA = 500 * (fx - fy), labB = 200 * (fy - fz);

            double c = Math.hypot(labA, labB);
            double h = Math.toDegrees(Math.atan2(labB, labA));
            l = Math.max(0, Math.min(100, l + lightness));
            c = Math.max(0, c + chroma);
            h += hue;
            labA = c * Math.cos(Math.toRadians(h));
            labB = c * Math.sin(Math.toRadians(h));

            fy = (l + 16) / 116;
            fx = fy + labA / 500;
            fz = fy - labB / 200;
            double[] adjusted = mul(FROM_XYZ, new double[]{XN * fInv(fx), YN * fInv(fy), ZN * fInv(fz)});

            int rgb = 0;
            for (int i = 0; i < 3; i++) {
                double out = Math.max(0, Math.min(1, adjusted[i]));
                out = lin[i] + (out - lin[i]) * opacity; // Replace mode at partial opacity
                rgb = (rgb << 8) | toSrgb(out);
            }
            return (a << 24) | rgb;
        }

        private static double f(double t) {
            return t > EPS ? Math.cbrt(t) : (KAPPA * t + 16) / 116;
        }

        private static double fInv(double t) {
            double t3 = t * t * t;
            return t3 > EPS ? t3 : (116 * t - 16) / KAPPA;
        }

        private static double toLinear(int c8) {
            double c = c8 / 255.0;
            return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
        }

        private static int toSrgb(double c) {
            double v = c <= 0.0031308 ? 12.92 * c : 1.055 * Math.pow(c, 1 / 2.4) - 0.055;
            return (int) Math.round(Math.max(0, Math.min(1, v)) * 255);
        }

        private static double[] mul(double[][] m, double[] v) {
            return new double[]{
                    m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
                    m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
                    m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]};
        }
    }
}