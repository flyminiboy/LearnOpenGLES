#version 300 es

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 tPosition;

uniform mat4 uPMatrix; // 接收一个矩阵

out vec2 outTexturePosition;

void main() {
    gl_Position = uPMatrix * vPosition; // gl_Position 内建变量，是一个vec4类型
    outTexturePosition = tPosition;
}


