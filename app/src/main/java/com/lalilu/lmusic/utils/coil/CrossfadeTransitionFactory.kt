package com.lalilu.lmusic.utils.coil

import coil3.decode.DataSource
import coil3.transition.CrossfadeDrawable
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.transition.CrossfadeTransition
import coil3.transition.Transition
import coil3.transition.TransitionTarget

/**
 * A copy of [CrossfadeTransition.Factory] that applies a transition to error results. You know.
 * Like they used to.
 * @author Coil Team
 * @author OxygenCobalt
 */
class CrossfadeTransitionFactory : Transition.Factory {
    override fun create(target: TransitionTarget, result: ImageResult): Transition {
        // Don't animate if the request was fulfilled by the memory cache.
        if (result is SuccessResult && result.dataSource == DataSource.MEMORY_CACHE) {
            return Transition.Factory.NONE.create(target, result)
        }

        return CrossfadeTransition(target, result, CrossfadeDrawable.DEFAULT_DURATION, false)
    }
}
