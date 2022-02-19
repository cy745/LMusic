package com.lalilu.lmusic.fragment

import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingBottomSheetDialogFragment
import com.lalilu.lmusic.base.DataBindingConfig


class BottomSheetsFragment : DataBindingBottomSheetDialogFragment() {
    var bottomSheetDialog: BottomSheetDialog? = null

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.dialog_library)
    }

    override fun onStart() {
        super.onStart()
        bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog?.delegate?.let {
            val bottomSheet: FrameLayout =
                it.findViewById(R.id.design_bottom_sheet) ?: return@let
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.peekHeight = getPeekHeight()
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    open fun getPeekHeight(): Int {
        return resources.displayMetrics.heightPixels
    }
}