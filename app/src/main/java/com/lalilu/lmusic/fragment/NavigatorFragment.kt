package com.lalilu.lmusic.fragment

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


    fun getNavController(singleUse: Boolean = false): NavController {
        singleUseFlag = singleUse
        return (mBinding as DialogNavigatorBinding)
            .dialogNavigator
            .findNavController()
    }
}