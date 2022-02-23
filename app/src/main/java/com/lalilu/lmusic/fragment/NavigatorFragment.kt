package com.lalilu.lmusic.fragment

import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.lalilu.R
import com.lalilu.databinding.DialogNavigatorBinding
import com.lalilu.lmusic.base.BaseBottomSheetFragment
import com.lalilu.lmusic.base.DataBindingConfig

class NavigatorFragment : BaseBottomSheetFragment<Any, DialogNavigatorBinding>() {
    private var singleUseFlag: Boolean = false

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.dialog_navigator)
    }

    override fun onBackPressed(): Boolean {
        if (singleUseFlag) {
            this.dismiss()
            return false
        }
        return getNavController().navigateUp()
    }

    override fun onBind(data: Any?, binding: ViewDataBinding) {
        val bd = binding as DialogNavigatorBinding
        bd.dialogBackButton.setOnClickListener {
            if (!onBackPressed()) {
                this.dismiss()
            }
        }
        bd.dialogCloseButton.setOnClickListener {
            this.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val bd = mBinding as DialogNavigatorBinding
        getNavController().addOnDestinationChangedListener { controller, destination, arguments ->
            var lastDestination = controller.previousBackStackEntry?.destination?.label
            if (lastDestination == null || singleUseFlag) {
                lastDestination = requireContext().resources
                    .getString(R.string.dialog_bottom_sheet_navigator_back)
            }
            bd.dialogBackButton.text = lastDestination
        }
    }

    fun getNavController(singleUse: Boolean = false): NavController {
        if (!singleUseFlag) singleUseFlag = singleUse
        return (mBinding as DialogNavigatorBinding)
            .dialogNavigator
            .findNavController()
    }
}