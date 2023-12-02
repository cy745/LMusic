package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.ui.shadertoy.BasePass
import com.lalilu.lmusic.ui.shadertoy.Shader
import com.lalilu.lmusic.ui.shadertoy.ShaderToyChannel
import com.lalilu.lmusic.ui.shadertoy.ShaderToyContext
import com.lalilu.lmusic.ui.shadertoy.ShaderToyPass
import com.lalilu.lmusic.ui.shadertoy.ShaderUtils
import com.lalilu.lmusic.ui.shadertoy.StaticScaleType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.ceil
import kotlin.math.min

fun interface FrameRateChangeListener {
    fun onFrameRateChange(frameRate: Float)
}

class ShaderView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    private val shaderContext: ShaderToyContext by lazy { ShaderToyContext(queueEventFunc = this::queueEvent) }
    private val renderer: ShaderToyRenderer by lazy { ShaderToyRenderer(image, shaderContext) }
    val palette = MutableLiveData<Palette?>(null)

    private val bitmapChannel = BitmapChannel()
    private val bufferA = TransformBuffer(
        content = ShowImage,
        channel0 = bitmapChannel
    )
    private val blurBuffer = BlurBuffer(
        channel0 = bufferA
    )

    private val colorChannel = ColorChannel()
    private val mixBuffer = MixBuffer(
        channel0 = colorChannel,
        channel1 = blurBuffer
    )

    private val image = Image(
        common = "",
        content = ShowImage,
        channel0 = mixBuffer
    )

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        tryUpdateFrameRate()
    }

    fun updateBitmap(bitmap: Bitmap? = null) {
        bitmap ?: return
        palette.postValue(Palette.from(bitmap).generate())
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

    fun updateAlpha(alpha: Float) {
        queueEvent {
            mixBuffer.updateAlpha(alpha)
            requestRender()
        }
    }

    fun updateProgress(progress: Float) {
        queueEvent {
            bufferA.updateProgress(progress)
            blurBuffer.updateRadius(50f * progress)
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
        GLES30.glViewport(0, 0, width, height)
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
    private var progress: Float = 0f

    fun updateProgress(progress: Float) {
        this.progress = progress
    }

    val aInterpolator = DecelerateInterpolator()

    override fun draw(context: ShaderToyContext) {
        val resolution = channel0?.output()?.getTextureResolution()
        requireNotNull(resolution) { "channal0 must have an output!" }

        val textureWidth = resolution.getOrNull(0) ?: 0
        val textureHeight = resolution.getOrNull(1) ?: 0

        Matrix.setIdentityM(shader.modelMatrix, 0)

        val value2 = aInterpolator.getInterpolation(progress)
        val screenHeight = lerp(shader.screenWidth, shader.screenHeight, 1f - value2)
        val textureAspectRatio = textureWidth.toFloat() / textureHeight.toFloat()
        val screenAspectRatio = shader.screenWidth.toFloat() / screenHeight

        // 先移动
        val moveY = (shader.screenHeight - shader.screenWidth).toFloat() / shader.screenHeight
        val translateY = lerp(moveY, 0f, progress)
        Matrix.translateM(shader.modelMatrix, 0, 0f, translateY, 0f)

        // 后缩放
        val scale = screenAspectRatio / textureAspectRatio
        Matrix.scaleM(shader.modelMatrix, 0, scale, scale, 0f)

        StaticScaleType.Crop.updateMatrix(
            mvpMatrix = shader.mvpMatrix,
            modelMatrix = shader.modelMatrix,
            viewMatrix = shader.viewMatrix,
            projectionMatrix = shader.projectionMatrix,
            screenWidth = shader.screenWidth,
            screenHeight = shader.screenHeight,
            textureWidth = textureWidth.toInt(),
            textureHeight = textureHeight.toInt()
        )

        // x,y轴翻转
        Matrix.scaleM(shader.mvpMatrix, 0, 1f, -1f, 1f)

        super.draw(context)
    }

    private fun lerp(start: Int, stop: Int, fraction: Float): Int {
        return start + ((stop - start) * fraction.toDouble()).toInt()
    }
}

class MixBuffer(
    channel0: ShaderToyPass,
    channel1: ShaderToyPass
) : Buffer(
    content = MixShader,
    channel0 = channel0,
    channel1 = channel1
) {
    companion object {
        val MixShader = """
            uniform float alpha;
            
            void mainImage( out vec4 fragColor, in vec2 fragCoord )
            {
                vec2 uv = fragCoord.xy / iResolution.xy;
                
                //    vec2 uv0 = fragCoord.xy / iChannelResolution[0].xy;
                //    vec2 uv1 = fragCoord.xy / iChannelResolution[1].xy;
                //    
                vec4 color0 = texture(iChannel0, uv);
                vec4 color1 = texture(iChannel1, uv);
            
                fragColor = mix(color0, color1, alpha);
            }
        """.trimIndent()
    }

    private var mAlphaLocation: Int = 0
    private var alpha: Float = 1f

    fun updateAlpha(alpha: Float) {
        this.alpha = alpha
    }

    override fun init(commonContent: String) {
        super.init(commonContent)
//        mAlphaLocation = GLES30.glGetUniformLocation(shader.programId, "alpha")
    }

    override fun onDraw(context: ShaderToyContext) {
        GLES30.glUniform1f(mAlphaLocation, alpha)
    }
}

class BlurBuffer(
    private val channel0: ShaderToyPass,
) : ShaderToyPass, ShaderToyChannel {
    companion object {
        val BlurShader = """
            uniform vec2 uOffset;
            
            void mainImage( out vec4 fragColor, in vec2 fragCoord )
            {
                vec2 vUV = fragCoord.xy / iResolution.xy;

                fragColor  = texture(iChannel0, vUV, 0.0);
                fragColor += texture(iChannel0, vUV + vec2( uOffset.x,  uOffset.y), 0.0);
                fragColor += texture(iChannel0, vUV + vec2( uOffset.x, -uOffset.y), 0.0);
                fragColor += texture(iChannel0, vUV + vec2(-uOffset.x,  uOffset.y), 0.0);
                fragColor += texture(iChannel0, vUV + vec2(-uOffset.x, -uOffset.y), 0.0);

                fragColor = vec4(fragColor.rgb * 0.2, 1.0);
            }
            """.trimIndent()
    }

    private var mRadius: Float = 0f
    fun updateRadius(radius: Float) {
        this.mRadius = radius
    }

    private val sampleScale: Int = 4
    private val textureResolution: FloatArray = floatArrayOf(0f, 0f, 0f)
    private var blurProgram: Int = 0
    private var pongTexture: Int = 0
    private var pingTexture: Int = 0
    private var pongFbo: Int = 0
    private var pingFbo: Int = 0
    private var lastDrawFbo: Int = 0

    private var mOffsetLoc: Int = 0
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var fboWidth: Int = 0
    private var fboHeight: Int = 0
    private val mvpMatrix = FloatArray(16)

    override fun init(commonContent: String) {
        channel0.init(commonContent)

        if (blurProgram == 0) {
            val fragmentCode = ShaderUtils.FragmentCodeTemplate
                .replace("//<**ShaderToyContent**>", BlurShader)
                .replace("//<**ShaderToyCommon**>", commonContent)
            blurProgram = ShaderUtils.createProgram(ShaderUtils.VertexCode, fragmentCode)
            mOffsetLoc = GLES30.glGetUniformLocation(blurProgram, "uOffset")
            Matrix.setIdentityM(mvpMatrix, 0)
        }
    }

    override fun update(context: ShaderToyContext, screenWidth: Int, screenHeight: Int) {
        channel0.update(context, screenWidth, screenHeight)

        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        this.fboWidth = screenWidth / sampleScale
        this.fboHeight = screenHeight / sampleScale
        this.textureResolution[0] = fboWidth.toFloat()
        this.textureResolution[1] = fboHeight.toFloat()

        if (pongTexture == 0) {
            pingTexture = ShaderUtils.createTexture(fboWidth, fboHeight)
            pingFbo = ShaderUtils.createFBO(pingTexture)
            pongTexture = ShaderUtils.createTexture(fboWidth, fboHeight)
            pongFbo = ShaderUtils.createFBO(pongTexture)
        }
    }

    override fun onDraw(context: ShaderToyContext) {
        val resolution = channel0.output()?.getTextureResolution()
        requireNotNull(resolution) { "channal0 must have an output!" }

        val textureWidth = resolution.getOrNull(0) ?: 0f
        val textureHeight = resolution.getOrNull(1) ?: 0f

        val radius = mRadius / 6.0f
        val passes = min(8, ceil(radius).toInt())

        // 若当前所需的次数为0，则不进行绘制，交由channel0进行绘制
        if (passes == 0 || passes == 1) {
            lastDrawFbo = -1
            return
        }

        val radiusByPasses = radius / passes
        val stepX = radiusByPasses / textureWidth
        val stepY = radiusByPasses / textureHeight

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, pingFbo)

        // 设置背景颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glClearColor(0f, 0f, 0f, 1f)

        GLES30.glUseProgram(blurProgram)
        GLES30.glUniformMatrix4fv(ShaderUtils.mUMatrixLocation, 1, false, mvpMatrix, 0)
        ShaderUtils.bindUniformVariable(context)

        bindTextureFromOutput(context) // 将channel0的纹理绑定至着色器对应变量
        GLES30.glUniform2f(mOffsetLoc, stepX, stepY)

        GLES30.glViewport(0, 0, fboWidth, fboHeight)
        ShaderUtils.drawVertex()

        var temp: Int
        var readFbo = pingFbo
        var drawFbo = pongFbo

        GLES30.glViewport(0, 0, fboWidth, fboHeight)
        for (i in 1 until passes) {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, drawFbo)

            // 此前没有启用其他纹理操作单元，则bindTexture将绑定至iChannel0
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, getTextureIdByFboId(readFbo))
            GLES30.glUniform2f(mOffsetLoc, stepX * i, stepY * i)
            ShaderUtils.drawVertex()

            // fbo交换
            temp = drawFbo
            drawFbo = readFbo
            readFbo = temp
        }

        lastDrawFbo = readFbo
        GLES30.glViewport(0, 0, screenWidth, screenHeight)
    }

    private fun getTextureIdByFboId(fboId: Int): Int {
        return if (fboId == pingFbo) pingTexture else pongTexture
    }

    private fun bindTextureFromOutput(context: ShaderToyContext) {
        channel0.output()?.let {
            context.tryBindTexture { count ->
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + count)   // 启用某个纹理操作单元，若后续没有启用其他操作单元，则bindTexture都会绑定至该操作单元
                GLES30.glUniform1i(ShaderUtils.iChannel0Location, count)    // 将该纹理操作单元映射到着色器中
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it.getTexture())
                GLES30.glUniform3fv(
                    ShaderUtils.iChannelResolutionLocation, 1,
                    it.getTextureResolution(), 0
                )
            }
        }
    }

    override fun draw(context: ShaderToyContext) {
        channel0.draw(context)
        onDraw(context)
    }

    override fun getTexture(): Int = getTextureIdByFboId(lastDrawFbo)
    override fun getTextureResolution(): FloatArray = textureResolution
    override fun output(): ShaderToyChannel {
        return if (lastDrawFbo == -1) channel0.output() ?: this else this
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
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            .also { it.setPixel(0, 0, color) }
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