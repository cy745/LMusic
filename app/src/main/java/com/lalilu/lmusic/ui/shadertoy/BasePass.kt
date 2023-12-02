package com.lalilu.lmusic.ui.shadertoy

import android.opengl.GLES30


open class BasePass(
    protected val shader: Shader,
    internal var channel0: ShaderToyPass? = null,
    internal var channel1: ShaderToyPass? = null,
    internal var channel2: ShaderToyPass? = null,
    internal var channel3: ShaderToyPass? = null
) : ShaderToyPass {
    private val iChannelResolution = FloatArray(12)

    override fun init(commonContent: String) {
        channel0?.init(commonContent)
        channel1?.init(commonContent)
        channel2?.init(commonContent)
        channel3?.init(commonContent)
        shader.onCreate(commonContent)
    }

    override fun update(context: ShaderToyContext, screenWidth: Int, screenHeight: Int) {
        channel0?.update(context, screenWidth, screenHeight)
        channel1?.update(context, screenWidth, screenHeight)
        channel2?.update(context, screenWidth, screenHeight)
        channel3?.update(context, screenWidth, screenHeight)
        shader.onSizeChange(context, screenWidth, screenHeight)
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
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + count)      // GL_TEXTUREx
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it.getTexture())    // textureId
                    GLES30.glUniform1i(ShaderUtils.iChannel0Location, count)       // x
                    it.getTextureResolution().let {
                        iChannelResolution[0] = it[0]
                        iChannelResolution[1] = it[1]
                        iChannelResolution[2] = it[2]
                    }
                }
            }
            channel1?.output()?.let {
                context.tryBindTexture { count ->
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + count)
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it.getTexture())
                    GLES30.glUniform1i(ShaderUtils.iChannel1Location, count)
                    it.getTextureResolution().let {
                        iChannelResolution[3] = it[0]
                        iChannelResolution[4] = it[1]
                        iChannelResolution[5] = it[2]
                    }
                }
            }
            channel2?.output()?.let {
                context.tryBindTexture { count ->
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + count)
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it.getTexture())
                    GLES30.glUniform1i(ShaderUtils.iChannel2Location, count)
                    it.getTextureResolution().let {
                        iChannelResolution[6] = it[0]
                        iChannelResolution[7] = it[1]
                        iChannelResolution[8] = it[2]
                    }
                }
            }
            channel3?.output()?.let {
                context.tryBindTexture { count ->
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + count)
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it.getTexture())
                    GLES30.glUniform1i(ShaderUtils.iChannel3Location, count)
                    it.getTextureResolution().let {
                        iChannelResolution[9] = it[0]
                        iChannelResolution[10] = it[1]
                        iChannelResolution[11] = it[2]
                    }
                }
            }
            GLES30.glUniform3fv(
                ShaderUtils.iChannelResolutionLocation, 4,
                iChannelResolution, 0
            )
            onDraw(context)
        }
    }
}