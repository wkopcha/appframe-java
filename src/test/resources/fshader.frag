#version 420 core

in vec4 exColor;
in vec3 exNormal;
out vec4 fragColor;

void main() {
    if(exColor.a <= 0.1)
        discard;
    float dot = exNormal.z;
    fragColor = exColor * dot;
}