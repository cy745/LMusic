package com.lalilu.lmusic.ui.shadertoy

class ShaderToyContext(
    private val queueEventFunc: (Runnable) -> Unit = {}
) {
    fun queueGLEvent(action: () -> Unit) = queueEventFunc.invoke(action)

    var mMouse = FloatArray(4)
    var mResolution = FloatArray(3)
    var mStartTime: Long = 0
    var iFrame: Int = 0
    var iFrameRate: Float = 60f
    var iTime: Float = 0f

    @Volatile
    var dirty: Boolean = false

    /**
     * 简易的计数器，用于在一个draw周期中，区分开不同的纹理使用的单元
     */
    private var textureCounter = 0
    fun tryBindTexture(action: (count: Int) -> Unit) {
        action(textureCounter)
        textureCounter++
    }

    fun resetCounter() {
        textureCounter = 0
    }
}

interface ShaderToyChannel {
    fun getTexture(): Int
    fun getTextureResolution(): FloatArray
}

interface ShaderToyPass {
    fun init(commonContent: String = "") {}
    fun update(context: ShaderToyContext, screenWidth: Int, screenHeight: Int) {}
    fun draw(context: ShaderToyContext) {}

    fun onDraw(context: ShaderToyContext) {}
    fun output(): ShaderToyChannel? = null
}