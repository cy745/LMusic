package com.lalilu.lmusic.ui.shadertoy

import android.graphics.Bitmap
import android.opengl.GLES32
import android.opengl.GLUtils
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.lalilu.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


object ShaderUtils {
    private const val LOGTAG = "ShaderUtils"
    private const val iResolutionLocation = 1
    private const val iTimeLocation = 2
    private const val iFrameRateLocation = 3
    private const val iFrameLocation = 4
    private const val iMouseLocation = 5
    const val iChannel0Location = 6
    const val iChannel1Location = 7
    const val iChannel2Location = 8
    const val iChannel3Location = 9
    const val iChannelResolutionLocation = 10
    private const val mafPositionLocation = 14
    private const val mavPositionLocation = 15
    const val mUMatrixLocation = 16

    //顶点坐标
    private val vertexData = floatArrayOf(
        -1f, -1f, 0.0f, // bottom left
        1f, -1f, 0.0f,  // bottom right
        -1f, 1f, 0.0f,  // top left
        1f, 1f, 0.0f,   // top right
    )

    // 纹理坐标
    private val textureData = floatArrayOf(
        1f, 0f, 0.0f,   // top right
        0f, 0f, 0.0f,   // top left
        1f, 1f, 0.0f,   // bottom right
        0f, 1f, 0.0f,   // bottom left
    )

    private const val COORDS_PER_VERTEX = 3 //每一次取点的时候取几个点
    private const val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex //每一次取的总的点 大小
    private val vertexCount: Int = vertexData.size / COORDS_PER_VERTEX

    private val vertexBuffer: FloatBuffer by lazy {
        ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
            .also { it.position(0) }
    }

    private val textureBuffer: FloatBuffer by lazy {
        ByteBuffer.allocateDirect(textureData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureData)
            .also { it.position(0) }
    }

    val VertexCode: String by lazy {
        Utils.getApp().resources
            .openRawResource(R.raw.vertex_shader_template)
            .readBytes()
            .decodeToString()
    }

    val FragmentCodeTemplate: String by lazy {
        Utils.getApp().resources
            .openRawResource(R.raw.fragment_shader_template)
            .readBytes()
            .decodeToString()
    }

    fun bindUniformVariable(context: ShaderToyContext) {
        GLES32.glUniform1f(iTimeLocation, context.iTime)
        GLES32.glUniform1i(iFrameLocation, context.iFrame)
        GLES32.glUniform1f(iFrameRateLocation, context.iFrameRate)
        GLES32.glUniform4fv(iMouseLocation, 1, context.mMouse, 0)
        GLES32.glUniform3fv(iResolutionLocation, 1, context.mResolution, 0)
    }

    fun drawVertex(action: () -> Unit) {
        action()

        GLES32.glEnableVertexAttribArray(mavPositionLocation)
        GLES32.glEnableVertexAttribArray(mafPositionLocation)

        //设置顶点位置值
        GLES32.glVertexAttribPointer(
            mavPositionLocation,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES32.glVertexAttribPointer(
            mafPositionLocation,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            textureBuffer
        )

        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, vertexCount)

        GLES32.glDisableVertexAttribArray(mavPositionLocation)
        GLES32.glDisableVertexAttribArray(mafPositionLocation)
    }

    /**
     * 创建一个着色器程序
     *
     * @return 该着色器在内存中的ID
     */
    fun createProgram(vertexCode: String, fragmentCode: String): Int {
        // 创建GL程序
        // Create the GL program
        val programId = GLES32.glCreateProgram()

        // 加载、编译vertex shader和fragment shader
        // Load and compile vertex shader and fragment shader
        val vertexShader = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER)
        val fragmentShader = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER)
        GLES32.glShaderSource(vertexShader, vertexCode)
        GLES32.glShaderSource(fragmentShader, fragmentCode)
        GLES32.glCompileShader(vertexShader)
        GLES32.glCompileShader(fragmentShader)
        checkGLError { "Shader create error" }

        // 将shader程序附着到GL程序上
        // Attach the compiled shaders to the GL program
        GLES32.glAttachShader(programId, vertexShader)
        GLES32.glAttachShader(programId, fragmentShader)

        // 链接GL程序
        // Link the GL program
        GLES32.glLinkProgram(programId)

        checkGLError()
        return programId
    }

    /**
     * 创建一个空的纹理对象
     *
     * @return 纹理对象的ID
     */
    fun createTexture(width: Int, height: Int): Int {
        val texture = IntArray(1)
        GLES32.glGenTextures(1, texture, 0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0])
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_WRAP_S,
            GLES32.GL_CLAMP_TO_EDGE
        )
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_WRAP_T,
            GLES32.GL_CLAMP_TO_EDGE
        )
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MIN_FILTER,
            GLES32.GL_NEAREST
        )
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MAG_FILTER,
            GLES32.GL_NEAREST
        )
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0,
            GLES32.GL_RGBA, width, height, 0,
            GLES32.GL_RGBA,
            GLES32.GL_UNSIGNED_BYTE,
            null,
        )

        checkGLError()
        return texture[0]
    }


    /**
     * 使用bitmap创建一个纹理对象
     *
     * @return 纹理对象的ID
     */
    fun createTexture(bitmap: Bitmap): Int {
        val texture = IntArray(1)
        GLES32.glGenTextures(1, texture, 0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0])
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_WRAP_S,
            GLES32.GL_CLAMP_TO_EDGE
        )
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_WRAP_T,
            GLES32.GL_CLAMP_TO_EDGE
        )
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MIN_FILTER,
            GLES32.GL_NEAREST
        )
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MAG_FILTER,
            GLES32.GL_NEAREST
        )
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)

        checkGLError()
        return texture[0]
    }

    /**
     * 更新某一纹理的内容
     *
     * @param textureId 目标纹理对象的id
     * @param bitmap
     *
     * @return 纹理对象的ID
     */
    fun updateTexture(textureId: Int, bitmap: Bitmap): Int {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
        return textureId
    }

    /**
     * 创建一个帧缓冲对象
     *
     * @return 帧缓冲对象的ID
     */
    fun createFBO(textureId: Int): Int {
        // 创建帧缓冲
        val fbo = IntArray(1)
        GLES32.glGenFramebuffers(1, fbo, 0)
        // 绑定帧缓冲
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, fbo[0])
        // 把纹理作为帧缓冲的附件
        GLES32.glFramebufferTexture2D(
            GLES32.GL_FRAMEBUFFER,
            GLES32.GL_COLOR_ATTACHMENT0,
            GLES32.GL_TEXTURE_2D, textureId, 0
        )

        checkGLError { "FBO create error" }
        checkFrameBufferError()

        return fbo[0]
    }

    private fun checkGLError(msg: () -> String = { "" }) {
        val error = GLES32.glGetError()
        if (error != GLES32.GL_NO_ERROR) {
            val hexErrorCode = Integer.toHexString(error)
            val message = "[GLError: $hexErrorCode] ${msg()}"
            Log.e(LOGTAG, message)
            throw RuntimeException(message)
        }
    }

    private fun checkFrameBufferError() {
        // 检查帧缓冲是否完整
        val fboStatus = GLES32.glCheckFramebufferStatus(GLES32.GL_FRAMEBUFFER)
        if (fboStatus != GLES32.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(LOGTAG, "initFBO failed, status: $fboStatus")
            throw RuntimeException("GLError")
        }
    }
}
