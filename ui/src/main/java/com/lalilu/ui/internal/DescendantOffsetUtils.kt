/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lalilu.ui.internal

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

/**
 * Utility class for descendant [Rect] calculations.
 *
 * @hide
 */
object DescendantOffsetUtils {
    private val matrix = ThreadLocal<Matrix>()
    private val rectF = ThreadLocal<RectF>()

    /**
     * This is a port of the common [ViewGroup.offsetDescendantRectToMyCoords] from
     * the framework, but adapted to take transformations into account. The result will be the
     * bounding rect of the real transformed rect.
     *
     * @param descendant view defining the original coordinate system of rect
     * @param rect (in/out) the rect to offset from descendant to this view's coordinate system
     */
    fun offsetDescendantRect(
        parent: ViewGroup, descendant: View, rect: Rect
    ) {
        var m = matrix.get()
        if (m == null) {
            m = Matrix()
            matrix.set(m)
        } else {
            m.reset()
        }
        offsetDescendantMatrix(parent, descendant, m)
        var rectF = rectF.get()
        if (rectF == null) {
            rectF = RectF()
            DescendantOffsetUtils.rectF.set(rectF)
        }
        rectF.set(rect)
        m.mapRect(rectF)
        rect[(rectF.left + 0.5f).toInt(), (rectF.top + 0.5f).toInt(), (rectF.right + 0.5f).toInt()] =
            (rectF.bottom + 0.5f).toInt()
    }

    /**
     * Retrieve the transformed bounding rect of an arbitrary descendant view. This does not need to
     * be a direct child.
     *
     * @param descendant descendant view to reference
     * @param out rect to set to the bounds of the descendant view
     */
    fun getDescendantRect(
        parent: ViewGroup, descendant: View, out: Rect
    ) {
        out[0, 0, descendant.width] = descendant.height
        offsetDescendantRect(parent, descendant, out)
    }

    private fun offsetDescendantMatrix(
        target: ViewParent, view: View, m: Matrix
    ) {
        val parent = view.parent
        if (parent is View && parent !== target) {
            val vp = parent as View
            offsetDescendantMatrix(target, vp, m)
            m.preTranslate(-vp.scrollX.toFloat(), -vp.scrollY.toFloat())
        }
        m.preTranslate(view.left.toFloat(), view.top.toFloat())
        if (!view.matrix.isIdentity) {
            m.preConcat(view.matrix)
        }
    }
}