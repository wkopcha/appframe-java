#version 420 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec4 inColor;

out vec4 exColor;

void main() {
    gl_Position = vec4(position, 1.0);
    exColor = inColor;
}