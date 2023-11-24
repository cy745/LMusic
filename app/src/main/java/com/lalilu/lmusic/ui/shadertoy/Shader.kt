package com.lalilu.lmusic.ui.shadertoy

import android.opengl.GLES32
import android.opengl.Matrix

/**
 * 一个简单的Shader封装
 *
 * @param content ShaderToy中的Shader代码
 * @param output 是否使用帧缓冲并输出一个有内容的Texture
 */
class Shader(
    private val content: String,
    private val output: Boolean = true
) {
    var screenWidth: Int = 0
        private set
    var screenHeight: Int = 0
        private set

    val mvpMatrix = FloatArray(16)
    val modelMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)

    val textureResolution: FloatArray =
        floatArrayOf(screenWidth.toFloat(), screenHeight.toFloat(), 0f)
    var textureId: Int = 0
        private set
    private var fboId: Int = 0
    private var programId: Int = 0

    fun onCreate(commonContent: String) {
        if (programId == 0) {
            val fragmentCode = ShaderUtils.FragmentCodeTemplate
                .replace("//<**ShaderToyContent**>", content)
                .replace("//<**ShaderToyCommon**>", commonContent)
            programId = ShaderUtils.createProgram(ShaderUtils.VertexCode, fragmentCode)
        }
    }

    fun onSizeChange(context: ShaderToyContext, width: Int, height: Int) {
        val changed = this.screenWidth != width || this.screenHeight != height
        if (!changed) return

        if (output) {
            textureId = ShaderUtils.createTexture(width, height)
            fboId = ShaderUtils.createFBO(textureId)
        }

        this.screenWidth = width
        this.screenHeight = height
        this.textureResolution[0] = width.toFloat()
        this.textureResolution[1] = height.toFloat()

        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
    }

    fun draw(context: ShaderToyContext, action: () -> Unit) {
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, fboId)

        // 设置背景颜色
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
        GLES32.glClearColor(0f, 0f, 0f, 1f)

        // 应用GL程序
        GLES32.glUseProgram(programId)

        GLES32.glUniformMatrix4fv(ShaderUtils.mUMatrixLocation, 1, false, mvpMatrix, 0)
        ShaderUtils.bindUniformVariable(context)
        ShaderUtils.drawVertex(action)

        // 重置
        // GLES32.glBindVertexArray(0)
        // GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0)
        // GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0)
    }
}
