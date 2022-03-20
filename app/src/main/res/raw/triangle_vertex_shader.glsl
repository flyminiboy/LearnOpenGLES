#version 300 es

layout (location = 0) in vec4 vPosition;

void main() {
    gl_Position = vPosition; // gl_Position 内建变量，是一个vec4类型
}


