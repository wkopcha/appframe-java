#version 420 core

in vec4 exColor;
layout(location = 0) out vec4 fragColor;

void main() {
    if(exColor.a <= 0.1)
        discard;
    fragColor = exColor;
}