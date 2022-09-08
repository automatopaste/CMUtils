#version 330

in vec2 fCoord;
in vec2 age; // age.y unused
//in vec4 modColor;

out vec4 fColor;

// credit: MaxBittker from https://github.com/MaxBittker/glsl-voronoi-noise
const mat2 myt = mat2(.12121212, .13131313, -.13131313, .12121212);
const vec2 mys = vec2(1e4, 1e6);

vec2 rhash(vec2 fCoord) {
    fCoord *= myt;
    fCoord *= mys;
    return fract(fract(fCoord / mys) * fCoord);
}

vec3 hash(vec3 p) {
    return fract(sin(vec3(dot(p, vec3(1.0, 57.0, 113.0)),
    dot(p, vec3(57.0, 113.0, 1.0)),
    dot(p, vec3(113.0, 1.0, 57.0)))) *
    43758.5453);
}

float voronoi2d(const in vec2 point) {
    vec2 p = floor(point);
    vec2 f = fract(point);
    float res = 0.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 b = vec2(i, j);
            vec2 r = vec2(b) - f + rhash(p + b);
            res += 1. / pow(dot(r, r), 8.);
        }
    }
    return pow(1. / res, 0.0625);
}

void main() {
    const vec4 modColor = vec4(0.2, 0.2, 1.0, 1.0);

    vec3 mag = vec3(0.5 - fCoord.y);
    float v = voronoi2d(modColor.xy * 40.0);
    vec3 ff = modColor.rgb * 0.01;
    vec3 ff2 = ff * 2.0;
    mag.rgb += vec3(ff2 * v - ff);
    mag *= 1.7;
    mag = 0.5 - (mag * mag);

    const float a = 0.08;
    const float ainv = 1.0 / a;
    const float b = 0.3;
    const float binv = 1.0 / b;

//    float t = max(ainv * min(age.x - fCoord.x, a), 0.0);
    float t = floor(age.x - fCoord.x + 1.0);
    vec3 core = vec3(max(binv * min(mag - 0.15, b), 0.0));
    float v1 = voronoi2d(vec2(1.0 - fCoord.x, fCoord.y) * 6.0);
    float v2 = voronoi2d(fCoord * 6.0);
    vec3 fringe = mag * v1 * v2;

    vec3 o = core;
    vec3 col = o * t;


    fColor = vec4(col, modColor.a);
}