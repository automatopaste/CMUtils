#version 330 core

layout (location = 0) in mat4 modelViewInstanced;
layout (location = 4) in vec4 edgeColorInstanced;
layout (location = 5) in vec4 fillColorInstanced;
layout (location = 6) in vec2 dataInstanced;

out VS_OUT {
    mat4 modelView;
    vec4 edgeColor;
    vec4 fillColor;
    vec2 data;
} vs_out;

void main() {
    gl_Position = vec4(0.0, 0.0, 0.0, 1.0);

    vs_out.modelView = modelViewInstanced;
    vs_out.edgeColor = edgeColorInstanced;
    vs_out.fillColor = fillColorInstanced;
    vs_out.data = dataInstanced;
}