package com.lalilu.lmusic.fragment

import android.content.DialogInterface
import androidx.annotation.IdRes
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.lalilu.R
import com.lalilu.databinding.DialogNavigatorBinding
import com.lalilu.lmusic.base.BaseBottomSheetFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.viewmodel.NavigatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

fun NavigatorFragment.navigateFrom(@IdRes startDestinationId: Int): NavController {
    mState.startFrom = startDestinationId
    return getNavController()
}

@AndroidEntryPoint
class NavigatorFragment : BaseBottomSheetFragment<Any, DialogNavigatorBinding>() {
    @Inject
    lateinit var mState: NavigatorViewModel

    private fun isStartDestination(): Boolean {
        return getNavController().currentDestination?.id != null
                && getNavController().currentDestination?.id == mState.startFrom
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.dialog_navigator)
    }

    override fun onBackPressed(): Boolean {
        if (isStartDestination()) {
            cancel()
            return false
        }
        return getNavController().navigateUp()
    }

    override fun onCancel(dialog: DialogInterface) {
        mState.startFrom = -1
        super.onCancel(dialog)
    }

    fun cancel() {
        mState.startFrom = -1
        this.dismiss()
    }

    override fun onBind(data: Any?, binding: ViewDataBinding) {
        val bd = binding as DialogNavigatorBinding
        bd.dialogBackButton.setOnClickListener {
            if (!onBackPressed()) {
                cancel()
            }
        }
        bd.dialogCloseButton.setOnClickListener {
            cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        val bd = mBinding as DialogNavigatorBinding
        getNavController().addOnDestinationChangedListener { controller, _, _ ->
            var lastDestination = controller.previousBackStackEntry?.destination?.label
            if (lastDestination == null || isStartDestination()) {
                lastDestination = requireContext().resources
                    .getString(R.string.dialog_bottom_sheet_navigator_back)
            }
            bd.dialogBackButton.text = lastDestination
        }
    }

    fun getNavController(): NavController {
        return (mBinding as DialogNavigatorBinding)
            .dialogNavigator
            .findNavController()
    }
}