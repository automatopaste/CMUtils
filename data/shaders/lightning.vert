#version 330

layout (location = 0) in vec2 vertex;
layout (location = 1) in mat4 modelViewInstanced;

out vec2 fCoord;
out vec2 age;

uniform mat4 projection;

void main() {
    gl_Position = projection * modelViewInstanced * vec4(vertex, 0.0, 1.0);

    fCoord = vertex;
    age = vec2(1.0, 0.0);
}