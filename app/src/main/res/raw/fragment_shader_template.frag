#version 300 es
precision highp float;

in vec2 texCoord;  //纹理位置  接收于vertex_shader
out vec4 FragColor;

// 着色器输入 注释掉的为未支持的参数
layout (location = 1) uniform vec3 iResolution;                 // viewport resolution (in pixels)
layout (location = 2) uniform float iTime;                      // shader playback time (in seconds)
layout (location = 3) uniform float iFrameRate;                 // shader frame rate
layout (location = 4) uniform int iFrame;                       // shader playback frame
layout (location = 5) uniform vec4 iMouse;                      // mouse pixel coords. xy: current (if MLB down), zw: click
layout (location = 6) uniform sampler2D iChannel0;              // input channel.
layout (location = 7) uniform sampler2D iChannel1;              // input channel.
layout (location = 8) uniform sampler2D iChannel2;              // input channel.
layout (location = 9) uniform sampler2D iChannel3;              // input channel.
layout (location = 10) uniform vec3 iChannelResolution[4];     // channel resolution (in pixels)

//uniform float     iTimeDelta;            // render time (in seconds)
//uniform float     iChannelTime[4];       // channel playback time (in seconds)
//uniform vec4      iDate;                 // (year, month, day, time in seconds)
//uniform float     iSampleRate;           // sound sample rate (i.e., 44100)

//<**ShaderToyCommon**>

//<**ShaderToyContent**>

void main() {
    mainImage(FragColor, texCoord * iResolution.xy);
}