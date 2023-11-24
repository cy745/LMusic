package com.lalilu.lmusic.ui.shadertoy

import android.opengl.GLES32


open class BasePass(
    protected val shader: Shader,
    internal var channel0: ShaderToyPass? = null,
    internal var channel1: ShaderToyPass? = null,
    internal var channel2: ShaderToyPass? = null,
    internal var channel3: ShaderToyPass? = null
) : ShaderToyPass {
    override fun init(commonContent: String) {
        shader.onCreate(commonContent)
        channel0?.init(commonContent)
        channel1?.init(commonContent)
        channel2?.init(commonContent)
        channel3?.init(commonContent)
    }

    override fun update(context: ShaderToyContext, screenWidth: Int, screenHeight: Int) {
        shader.onSizeChange(context, screenWidth, screenHeight)
        channel0?.update(context, screenWidth, screenHeight)
        channel1?.update(context, screenWidth, screenHeight)
        channel2?.update(context, screenWidth, screenHeight)
        channel3?.update(context, screenWidth, screenHeight)
    }

    override fun draw(context: ShaderToyContext) {
        channel0?.draw(context)
        channel1?.draw(context)
        channel2?.draw(context)
        channel3?.draw(context)

        /**
         *  glActiveTexture函数需要传的是 GL_TEXTURE0
         *  而传递至GLSL的uniform变量时glUniform1i需要传的只是 0
         */
        shader.draw(context) {
            channel0?.output()?.let {
                context.tryBindTexture { count ->
                    GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + count)      // GL_TEXTUREx
                    GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, it.getTexture())    // textureId
                    GLES32.glUniform1i(ShaderUtils.iChannel0Location, count)       // x
                    GLES32.glUniform3fv(
                        ShaderUtils.iChannelResolutionLocation, 1,
                        it.getTextureResolution(), 0
                    )
                }
            }
            channel1?.output()?.let {
                context.tryBindTexture { count ->
                    GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + count)
                    GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, it.getTexture())
                    GLES32.glUniform1i(ShaderUtils.iChannel1Location, count)
                    GLES32.glUniform3fv(
                        ShaderUtils.iChannelResolutionLocation, 1,
                        it.getTextureResolution(), 1
                    )
                }
            }
            channel2?.output()?.let {
                context.tryBindTexture { count ->
                    GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + count)
                    GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, it.getTexture())
                    GLES32.glUniform1i(ShaderUtils.iChannel2Location, count)
                    GLES32.glUniform3fv(
                        ShaderUtils.iChannelResolutionLocation, 1,
                        it.getTextureResolution(), 2
                    )
                }
            }
            channel3?.output()?.let {
                context.tryBindTexture { count ->
                    GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + count)
                    GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, it.getTexture())
                    GLES32.glUniform1i(ShaderUtils.iChannel3Location, count)
                    GLES32.glUniform3fv(
                        ShaderUtils.iChannelResolutionLocation, 1,
                        it.getTextureResolution(), 3
                    )
                }
            }
        }
    }
}