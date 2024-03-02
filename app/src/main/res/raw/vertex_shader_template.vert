#version 300 es

layout (location = 14) in vec2 af_Position;    //纹理坐标
layout (location = 15) in vec4 av_Position;    //顶点坐标
layout (location = 16) uniform mat4 u_Matrix;
out vec2 texCoord;      //纹理位置  与fragment_shader交互

void main() {
    texCoord = af_Position;
    gl_Position = u_Matrix * av_Position;
}