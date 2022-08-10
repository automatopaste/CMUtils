#version 330

in vec2 vCoord;
in vec4 modColor;

out vec4 fColor;

vec2 rotate(vec2 v, float a) {
    float s = sin(a);
    float c = cos(a);
    mat2 m = mat2(c, -s, s, c);
    return m * v;
}

void main() {
    float q = 2.0 * mod(modColor.a, 1.0) - 1.0;

    float df = length(vCoord - vec2(0.5));
    vec3 d = vec3(length(vCoord - vec2(0.51, 0.49)), length(vCoord - vec2(0.49, 0.51)), length(vCoord - vec2(0.49, 0.49)));

    float r = q * q;
    vec3 v = vec3(1.0 - abs(r - d));
    v *= v * v * v * v;

    vec2 uvd = vCoord * 2.0 - 1.0;
    uvd = rotate(uvd, q * 8.0 * df);
    float ray1 = max(0.0, abs(uvd.x / r * uvd.y * 105.0));
    float ray2 = max(0.0, abs(uvd.x / r * uvd.y * 100.0));
    v += 0.1 * (ray2 - ray1);

    v += max(v - 0.2, 0.0);

    // edge falloff
    const float g = 0.7;
    v *= clamp((5.0 * g) - (10.0 * g * d), 0.0, 1.0);
    v *= modColor.rgb;

    // Output to screen
    fColor = vec4(v, modColor.a);
}