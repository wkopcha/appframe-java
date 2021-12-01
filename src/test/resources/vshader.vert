#version 420 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec4 inColor;
layout (location = 2) in vec3 normal;

out vec4 exColor;
out vec3 exNormal;

void main() {
    gl_Position = vec4(position, 1.0);
    exColor = inColor;
    exNormal = normal;
}