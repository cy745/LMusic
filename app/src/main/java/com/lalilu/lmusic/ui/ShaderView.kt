package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.view.SurfaceHolder
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.lalilu.lmusic.ui.shadertoy.BasePass
import com.lalilu.lmusic.ui.shadertoy.Shader
import com.lalilu.lmusic.ui.shadertoy.ShaderToyChannel
import com.lalilu.lmusic.ui.shadertoy.ShaderToyContext
import com.lalilu.lmusic.ui.shadertoy.ShaderToyPass
import com.lalilu.lmusic.ui.shadertoy.ShaderUtils
import com.lalilu.lmusic.ui.shadertoy.StaticScaleType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

fun interface FrameRateChangeListener {
    fun onFrameRateChange(frameRate: Float)
}

class ShaderView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    private val shaderContext: ShaderToyContext by lazy { ShaderToyContext(queueEventFunc = this::queueEvent) }
    private val renderer: ShaderToyRenderer by lazy { ShaderToyRenderer(image, shaderContext) }

    private val bitmapChannel = BitmapChannel()
    private val colorChannel = ColorChannel()

    private val bufferA = TransformBuffer(
        content = ShowImage,
        channel0 = colorChannel
    )
    private val image = Image(
        common = "",
        content = ShowImage,
        channel0 = colorChannel,
        channel1 = bufferA
    )

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        tryUpdateFrameRate()
    }

    fun updateBitmap(bitmap: Bitmap? = null) {
        bitmap ?: return
        queueEvent {
            bitmapChannel.update(bitmap)
            requestRender()
        }
    }

    fun updateColor(color: Int) {
        queueEvent {
            colorChannel.updateColor(color)
            requestRender()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        super.surfaceChanged(holder, format, w, h)
        tryUpdateFrameRate()
    }

    private fun tryUpdateFrameRate() {
        val refreshRate = ContextCompat.getDisplayOrDefault(context).refreshRate
        renderer.onFrameRateChange(refreshRate)
    }
}


class ShaderToyRenderer(
    private val image: Image,
    private val shaderContext: ShaderToyContext
) : GLSurfaceView.Renderer, FrameRateChangeListener {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        shaderContext.mStartTime = System.currentTimeMillis()
        image.init()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        shaderContext.mResolution = floatArrayOf(width.toFloat(), height.toFloat(), 1f)
        image.update(shaderContext, width, height)
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onFrameRateChange(frameRate: Float) {
        shaderContext.iFrameRate = frameRate
    }

    override fun onDrawFrame(gl: GL10?) {
        shaderContext.resetCounter()
        shaderContext.iFrame = ++shaderContext.iFrame
        shaderContext.iTime = (System.currentTimeMillis() - shaderContext.mStartTime)
            .toFloat() / 1000f

        image.draw(shaderContext)
    }
}

class TransformBuffer(
    content: String,
    channel0: ShaderToyPass
) : Buffer(content = content, channel0 = channel0) {
    override fun draw(context: ShaderToyContext) {
        val resolution = channel0?.output()?.getTextureResolution()
        requireNotNull(resolution) { "channal0 must have an output!" }

        val width = resolution.getOrNull(0) ?: 0
        val height = resolution.getOrNull(1) ?: 0

        StaticScaleType.Crop.updateMatrix(
            mvpMatrix = shader.mvpMatrix,
            modelMatrix = shader.modelMatrix,
            viewMatrix = shader.viewMatrix,
            projectionMatrix = shader.projectionMatrix,
            screenWidth = shader.screenWidth,
            screenHeight = shader.screenHeight,
            textureWidth = width.toInt(),
            textureHeight = height.toInt()
        )

        // y轴上下翻转
        Matrix.scaleM(shader.mvpMatrix, 0, 1f, -1f, 1f)

        super.draw(context)
    }
}

open class Buffer(
    val content: String,
    channel0: ShaderToyPass? = null,
    channel1: ShaderToyPass? = null,
    channel2: ShaderToyPass? = null,
    channel3: ShaderToyPass? = null
) : BasePass(
    shader = Shader(content = content),
    channel0 = channel0,
    channel1 = channel1,
    channel2 = channel2,
    channel3 = channel3,
), ShaderToyChannel {
    override fun getTexture(): Int = shader.textureId
    override fun getTextureResolution(): FloatArray = shader.textureResolution
    override fun output(): ShaderToyChannel = this
}

class Image(
    val content: String,
    val common: String = "",
    channel0: ShaderToyPass? = null,
    channel1: ShaderToyPass? = null,
    channel2: ShaderToyPass? = null,
    channel3: ShaderToyPass? = null
) : BasePass(
    shader = Shader(content, false),
    channel0 = channel0,
    channel1 = channel1,
    channel2 = channel2,
    channel3 = channel3,
) {
    override fun init(commonContent: String) {
        super.init(common)
    }
}

class BitmapChannel(
    private val bitmap: Bitmap? = null
) : ShaderToyChannel, ShaderToyPass {
    private var textureId = 0
    private var textureResolution = floatArrayOf(0f, 0f, 0f)

    fun update(bitmap: Bitmap) {
        if (textureId == 0) {
            textureId = ShaderUtils.createTexture(bitmap)
        } else {
            ShaderUtils.updateTexture(textureId, bitmap)
        }
        textureResolution[0] = bitmap.width.toFloat()
        textureResolution[1] = bitmap.height.toFloat()
    }

    override fun init(commonContent: String) {
        if (bitmap != null) update(bitmap)
    }

    // 若无预设bitmap，则用屏幕宽高创建Texture，需确保texture必须在绘制和更新数据前已完成创建
    override fun update(context: ShaderToyContext, screenWidth: Int, screenHeight: Int) {
        if (textureId == 0) {
            textureId = ShaderUtils.createTexture(screenWidth, screenHeight)
            textureResolution[0] = screenWidth.toFloat()
            textureResolution[1] = screenHeight.toFloat()
        }
    }

    override fun getTexture(): Int = textureId
    override fun getTextureResolution(): FloatArray = textureResolution
    override fun output(): ShaderToyChannel = this
}


class ColorChannel(
    @ColorInt color: Int = Color.BLACK
) : ShaderToyChannel, ShaderToyPass {
    private var textureId = 0
    private var textureResolution = floatArrayOf(1f, 1f, 0f)
    private val colorBitmap by lazy {
        Bitmap.createBitmap(intArrayOf(color), 1, 1, Bitmap.Config.ARGB_8888)
    }

    override fun init(commonContent: String) {
        textureId = ShaderUtils.createTexture(colorBitmap)
    }

    fun updateColor(color: Int) {
        if (textureId != 0) {
            colorBitmap.setPixel(0, 0, color)
            ShaderUtils.updateTexture(textureId, colorBitmap)
        }
    }

    override fun getTexture(): Int = textureId
    override fun getTextureResolution(): FloatArray = textureResolution
    override fun output(): ShaderToyChannel = this
}