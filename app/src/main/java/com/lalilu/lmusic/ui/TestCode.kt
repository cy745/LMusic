package com.lalilu.lmusic.ui


val ShowImage = """
vec4 textureNice( sampler2D sam, vec2 uv )
{
    float textureResolution = float(textureSize(sam,0).x);
    uv = uv*textureResolution + 0.5;
    vec2 iuv = floor( uv );
    vec2 fuv = fract( uv );
    uv = iuv + fuv*fuv*(3.0-2.0*fuv);
    uv = (uv - 0.5)/textureResolution;
    return texture( sam, uv );
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    vec2 uv = fragCoord/iResolution.xy;

    fragColor = textureNice(iChannel0, uv);
}
""".trimIndent()
