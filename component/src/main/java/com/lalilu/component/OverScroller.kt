package com.lalilu.component

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * 用于计算滚动的最终位置和最终速度的实现
 */
class OverScroller(
    animationSpec: DecayAnimationSpec<Float> = exponentialDecay()
) {
    private val flingBehavior = NoMotionFlingBehavior(animationSpec)
    private var position: Float = 0f
    private var minPosition: Float = 0f
    private var maxPosition: Float = Float.MAX_VALUE
    private val scrollScope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float {
            val oldPosition = position
            position = (position + pixels).coerceIn(minPosition, maxPosition)
            return position - oldPosition
        }
    }

    /**
     * 滚动的最终位置，需要在调用[fling]函数后获取
     */
    val finalPosition: Float
        get() = position

    /**
     * fling实现
     *
     * @param initialVelocity  初始速度
     * @param startPosition    起始位置
     * @param min              最小值
     * @param max              最大值
     *
     * @return 到达边界时的最终速度，或未到达边界速度减至0
     */
    suspend fun fling(
        initialVelocity: Float,
        startPosition: Float = position,
        min: Float = minPosition,
        max: Float = maxPosition
    ): Float {
        position = startPosition
        minPosition = min
        maxPosition = max

        return with(flingBehavior) {
            scrollScope.performFling(initialVelocity = initialVelocity)
        }
    }
}

/**
 * 默认的DefaultFlingBehavior的Copy,
 * 替换了[motionDurationScale]为[DisableScrollMotionDurationScale]
 */
internal class NoMotionFlingBehavior(
    private val flingDecay: DecayAnimationSpec<Float>,
    private val motionDurationScale: MotionDurationScale = DisableScrollMotionDurationScale
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        return withContext(motionDurationScale) {
            if (abs(initialVelocity) > 1f) {
                var velocityLeft = initialVelocity
                var lastValue = 0f
                val animationState =
                    AnimationState(
                        initialValue = 0f,
                        initialVelocity = initialVelocity,
                    )
                try {
                    animationState.animateDecay(flingDecay) {
                        val delta = value - lastValue
                        val consumed = scrollBy(delta)
                        lastValue = value
                        velocityLeft = this.velocity
                        // avoid rounding errors and stop if anything is unconsumed
                        if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
                    }
                } catch (exception: CancellationException) {
                    velocityLeft = animationState.velocity
                }
                velocityLeft
            } else {
                initialVelocity
            }
        }
    }
}

/**
 * [MotionDurationScale.scaleFactor]为0f时，动画会在下一帧内直接完成，而不会阻塞
 * 0f would cause motion to finish in the next frame callback.
 */
internal val DisableScrollMotionDurationScale =
    object : MotionDurationScale {
        override val scaleFactor: Float
            get() = 0f
    }
