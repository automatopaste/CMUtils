#version 330 core

in vec4 edgeColor;
in vec4 fillColor;
in vec2 pData;

out vec4 fColor;

float numSamples = 0.0;

float linterp(float v1, float v2, float x) {
    x = clamp(x, 0.0, 1.0);
    return x * v2 + (1.0 - x) * v1;
}

vec4 getVal(float x, float offset) {
    numSamples += 1.0;

    return vec4(
    linterp(fillColor.r, edgeColor.r, x),
    linterp(fillColor.g, edgeColor.g, x),
    linterp(fillColor.b, edgeColor.b, x),
    linterp(fillColor.a, edgeColor.a, x)
    );
}

void main() {
    float e = 1.0 - max(min(1000.0 * (pData.x - 0.999), 1.0), 0.0);
    float x = min(max(10.0 * (pData.x - (1.0 - pData.y)), 0.0), 1.0);

//    const float offset = 0.1;

    vec4 c = getVal(x, 0.0);
//    c += getVal(x, offset);
//    c += getVal (x, -offset);

//    c /= numSamples;

    fColor = c * e;
}