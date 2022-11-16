#version 330 core

in vec2 vCoord;
in vec4 modColor;

out vec4 fColor;

uniform sampler2D image0;

void main() {
    fColor = texture(image0, vCoord) * modColor;
}