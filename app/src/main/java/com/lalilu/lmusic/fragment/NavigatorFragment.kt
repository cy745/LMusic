package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.lalilu.R
import com.lalilu.databinding.FragmentNavigatorBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.event.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class NavigatorFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mEvent: SharedViewModel

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_navigator)
            .addParam(BR.ev, mEvent)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentNavigatorBinding
        val toolbar2 = binding.toolbar2
        val navigator = binding.navigator

        launch {
            delay(100)

            withContext(Dispatchers.Main) {
                NavigationUI.setupWithNavController(
                    toolbar2,
                    Navigation.findNavController(navigator)
                )
            }
        }
    }
}