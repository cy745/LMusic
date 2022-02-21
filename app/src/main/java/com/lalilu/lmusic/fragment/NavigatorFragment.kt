package com.lalilu.lmusic.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.lalilu.R
import com.lalilu.databinding.DialogNavigatorBinding
import com.lalilu.lmusic.base.BaseBottomSheetFragment
import com.lalilu.lmusic.base.DataBindingConfig

class NavigatorFragment : BaseBottomSheetFragment<Any, DialogNavigatorBinding>() {
    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.dialog_navigator)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.setOnKeyListener { _, i, _ ->
                if (i == KeyEvent.KEYCODE_BACK) {
                    return@setOnKeyListener requireNavController().navigateUp()
                }
                return@setOnKeyListener false
            }
        }
    }

    private fun requireNavController(): NavController {
        return (mBinding as DialogNavigatorBinding)
            .dialogNavigator
            .findNavController()
    }
}