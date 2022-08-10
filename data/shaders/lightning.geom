#version 330

layout (lines) in;
layout (triangle_strip, max_vertices = 22) out;

out vec2 fCoords;

//in VS_OUT {
//    vec2 dim; // x is age mult, y is width
//    vec4 color;
//    float intervals[];
//    float offsets[];
//} gs_in[];
//
in VS_OUT {
    mat4 gModel;
    vec2 gDim; // x is age mult, y is width
} gs_in[];

uniform mat4 projection;
uniform mat4 view;

const float intervals[] = float[](
0.1625066, 0.11110018, 0.14083639, 0.0722918, 0.1366666, 0.093935736, 0.13174625, 0.056110613, 0.027836327, 0.066969536
);
const float intervals2[] = float[](
-0.21529305, 0.16658172, -0.22082055, 0.14462474, -0.25338304, 0.17177191, -0.14244607, 0.053435713, -0.07599272, -0.049186826
);

void main() {
    mat4 pvm = projection * view * gs_in[0].gModel;

    gl_Position = pvm * (gl_in[0].gl_Position + vec4(0.0, 0.2, 0.0, 0.0));
    fCoords = vec2(0f, 0f);
    EmitVertex();

    gl_Position = pvm * (gl_in[0].gl_Position + vec4(0.0, -0.2, 0.0, 0.0));
    fCoords = vec2(0f, 1f);
    EmitVertex();

    float cum = intervals[0];
    for (int i = 1; i < 10; i++) {
        float sub = intervals2[i] * gs_in[0].gDim.y;

        gl_Position = pvm * (gl_in[0].gl_Position + vec4(cum, sub, 0.0, 0.0));
        fCoords = vec2(cum, 0f);
        EmitVertex();

        gl_Position = pvm * (gl_in[0].gl_Position + vec4(cum, sub, 0.0, 0.0));
        fCoords = vec2(cum, 1f);
        EmitVertex();

        cum += intervals[i];
    }

    gl_Position = pvm * (gl_in[1].gl_Position + vec4(0.0, -0.2, 0.0, 0.0));
    fCoords = vec2(1f, 0f);
    EmitVertex();

    gl_Position = pvm * (gl_in[1].gl_Position + vec4(0.0, -0.2, 0.0, 0.0));
    fCoords = vec2(1f, 1f);
    EmitVertex();

    EndPrimitive();
}