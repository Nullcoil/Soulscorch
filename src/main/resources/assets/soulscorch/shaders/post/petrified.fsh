#version 330

// Witnessed: drains the world's color as you turn to stone.
// Petrification goes from 0 (full color) to 1 (grayscale), written every frame by PetrifiedVision.

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform PetrifiedConfig {
    float Petrification;
};

out vec4 fragColor;

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    // Perceived brightness (Rec. 709), so the gray keeps the scene's light and dark
    float gray = dot(color, vec3(0.2126, 0.7152, 0.0722));
    fragColor = vec4(mix(color, vec3(gray), clamp(Petrification, 0.0, 1.0)), 1.0);
}
