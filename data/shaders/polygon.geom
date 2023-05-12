#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices = 192) out;

in VS_OUT {
    mat4 modelView;
    vec4 edgeColor;
    vec4 fillColor;
    vec2 data;
} vs_in[];

out vec4 edgeColor;
out vec4 fillColor;
out vec2 pData;

uniform mat4 projection;

void main() {
    mat4 t = projection * vs_in[0].modelView;

    float num = vs_in[0].data.x;
    float d = 6.28318530718 / num;

    int n = int(num);
    float a = 0.0;
    for (int i = 0; i < n; i++) {
        gl_Position = t * vec4(cos(a), -sin(a), 0.0, 1.0);
        edgeColor = vs_in[0].edgeColor;
        fillColor = vs_in[0].fillColor;
        pData = vec2(1.0, vs_in[0].data.y);
        EmitVertex();

        a += d;

        gl_Position = t * vec4(cos(a), -sin(a), 0.0, 1.0);
        edgeColor = vs_in[0].edgeColor;
        fillColor = vs_in[0].fillColor;
        pData = vec2(1.0, vs_in[0].data.y);
        EmitVertex();

        gl_Position = t * vec4(0.0, 0.0, 0.0, 1.0);
        edgeColor = vs_in[0].edgeColor;
        fillColor = vs_in[0].fillColor;
        pData = vec2(0.0, vs_in[0].data.y);
        EmitVertex();

        EndPrimitive();
    }
}