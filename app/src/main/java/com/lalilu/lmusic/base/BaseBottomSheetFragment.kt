package com.lalilu.lmusic.base

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
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

fun <I : Any, B : ViewDataBinding> Fragment.showDialog(
    dialog: BaseBottomSheetFragment<I, B>?, bindData: I? = null,
    callback: BaseBottomSheetFragment<I, B>.() -> Unit = {}
) {
    if (dialog?.isAdded == true) dialog.dismiss()
    dialog?.onInitialized = callback
    dialog?.bind(bindData)
    dialog?.show(requireActivity().supportFragmentManager, dialog.tag)
}

abstract class BaseBottomSheetFragment<I : Any, B : ViewDataBinding> :
    DataBindingBottomSheetDialogFragment() {
    private var mTranslateYAnimation: SpringAnimation? = null
    private var lateBindData: I? = null
    var onInitialized: (BaseBottomSheetFragment<I, B>.() -> Unit)? = null

    open fun onBind(data: I?, binding: ViewDataBinding) {}

    fun bind(data: I?) {
        mBinding?.let {
            onBind(data, it)
            return
        }
        lateBindData = data
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    open fun getHeight(): Int {
        return context?.resources?.displayMetrics?.heightPixels ?: 0
    }

    open fun getPeekHeight(): Int {
        return getHeight() / 2
    }

    open fun getPaddingTop(): Int {
        return 0
    }

    override fun onViewCreated() {
        bind(lateBindData)
    }

    override fun onResume() {
        super.onResume()
        onInitialized?.invoke(this)
        onInitialized = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context ?: return super.onCreateDialog(savedInstanceState)
        return BottomSheetDialog(requireContext(), R.style.MY_BottomSheet).also {
            it.setOnKeyListener { _, i, keyEvent ->
                if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                    return@setOnKeyListener onBackPressed()
                }
                return@setOnKeyListener false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getRootViewOfDialog<FrameLayout>(dialog)?.let { bottomSheet ->
            bottomSheet.layoutParams.height = getHeight()
            bottomSheet.setPadding(0, getPaddingTop(), 0, 0)
            animateTranslateYTo(getPeekHeight(), onStart = {
                it.peekHeight = 0
            }, onEnd = {
                it.peekHeight = getPeekHeight()
                if (it.state == BottomSheetBehavior.STATE_HIDDEN) {
                    it.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            })
        }
    }

    private fun animateTranslateYTo(
        translateYValue: Number,
        onStart: (BottomSheetBehavior<View>) -> Unit = {},
        onEnd: (BottomSheetBehavior<View>) -> Unit = {}
    ) {
        mTranslateYAnimation = getRootViewOfDialog<View>(dialog)?.let { view ->
            val behavior = BottomSheetBehavior.from(view)
            onStart(behavior)
            SpringAnimation(behavior, TranslateYFloatProperty(), 0f).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                spring.stiffness = 500f
                addEndListener { _, canceled, _, _ ->
                    if (!canceled) onEnd(behavior)
                }
            }
        }
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
        FloatPropertyCompat<BottomSheetBehavior<View>>("translateY") {
        override fun getValue(obj: BottomSheetBehavior<View>): Float {
            return obj.peekHeight.toFloat()
        }

        override fun setValue(obj: BottomSheetBehavior<View>, value: Float) {
            obj.setPeekHeight(value.toInt(), false)
        }
    }
}