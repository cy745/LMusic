package com.lalilu.lmusic.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lalilu.R
import kotlin.math.roundToInt

fun <I : Any, B : ViewDataBinding> Fragment.showDialog(
    dialog: BaseBottomSheetFragment<I, B>?, data: I? = null
) {
    dialog?.bind(data)
    dialog?.show(requireActivity().supportFragmentManager, dialog.tag)
}

abstract class BaseBottomSheetFragment<I : Any, B : ViewDataBinding> :
    DataBindingBottomSheetDialogFragment() {
    private var mTranslateYAnimation: SpringAnimation? = null
    private var lateBindData: I? = null

    open fun onBind(data: I?, binding: ViewDataBinding) {}

    fun bind(data: I?) {
        mBinding?.let {
            onBind(data, it)
            return
        }
        lateBindData = data
    }

    open fun getHeight(): Int {
        return context?.resources?.displayMetrics?.heightPixels ?: 0
    }

    open fun getPeekHeight(): Int {
        return getHeight() / 2
    }

    open fun getPaddingTop(): Int {
        val scale = context?.resources?.displayMetrics?.density ?: return 0
        return (scale * 20f).roundToInt()
    }

    override fun onViewCreated() {
        bind(lateBindData)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (context == null) return super.onCreateDialog(savedInstanceState)
        return BottomSheetDialog(requireContext(), R.style.MY_BottomSheet)
    }

    override fun onStart() {
        super.onStart()
        getRootViewOfDialog<FrameLayout>(dialog)?.let { bottomSheet ->
            bottomSheet.layoutParams.height = getHeight()
            bottomSheet.setPadding(0, getPaddingTop(), 0, 0)
            val behavior = BottomSheetBehavior.from(bottomSheet)
            animateTranslateYTo(-getPeekHeight(), onStart = {
                behavior.peekHeight = 0
            }, onEnd = {
                bottomSheet.translationY = 0f
                behavior.peekHeight = getPeekHeight()
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            })
        }
    }

    private fun animateTranslateYTo(
        translateYValue: Number,
        onStart: () -> Unit = {},
        onEnd: () -> Unit = {}
    ) {
        mTranslateYAnimation = getRootViewOfDialog<View>(dialog)?.let { view ->
            SpringAnimation(view, TranslateYFloatProperty(), 0f).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                spring.stiffness = 500f
                addEndListener { _, canceled, _, _ ->
                    if (!canceled) onEnd()
                }
            }
        }
        onStart()
        mTranslateYAnimation?.cancel()
        mTranslateYAnimation?.animateToFinalPosition(translateYValue.toFloat())
    }

    private fun <T : View> getRootViewOfDialog(dialog: Dialog?): T? {
        if (dialog == null || dialog !is BottomSheetDialog) {
            return null
        }
        return dialog.delegate.findViewById(R.id.design_bottom_sheet)
    }

    class TranslateYFloatProperty :
        FloatPropertyCompat<View>("translateY") {
        override fun getValue(obj: View): Float {
            return obj.translationY
        }

        override fun setValue(obj: View, value: Float) {
            obj.translationY = value
        }
    }
}