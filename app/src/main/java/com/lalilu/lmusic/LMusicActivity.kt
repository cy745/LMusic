package com.lalilu.lmusic

import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.customview.customView
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.ui.MySearchBar
import com.lalilu.lmusic.utils.DeviceUtil
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LMusicActivity : DataBindingActivity() {

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var playerModule: PlayerModule

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immerseStatusBar(this)
        PermissionUtils.requestPermission(this)

//        println("isPad: ${DeviceUtil.isPad(this)}")
//        requestedOrientation = if (DeviceUtil.isPad(this)) {
//            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        } else {
//            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        }

        val view = View.inflate(this, R.layout.fragment_settings, null)
        dialog = MaterialDialog(this, BottomSheet()).apply {
            setPeekHeight(DeviceUtil.getHeight(this@LMusicActivity) / 3)
            window?.setDimAmount(0.01f)
            title(text = "设置")
            message(text = "一些简单的选项")
            cornerRadius(16f)
            customView(view = view)
        }
    }

    lateinit var dialog: MaterialDialog

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_search -> mEvent.collapseAppbarLayout()
            R.id.appbar_settings -> dialog.show {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        playerModule.disconnect()
        super.onPause()
    }

    override fun onResume() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        playerModule.connect()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        val items = menu.findItem(R.id.appbar_search)
        val searchBar = MySearchBar(items) {
            playerModule.searchFor(it)
        }
        mEvent.isSearchViewExpand.observe(this) {
            it?.get { searchBar.collapse() }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val manager = supportFragmentManager
        val refreshList = listOf(
            R.id.fragment_main
        )

        val trans = manager.beginTransaction()
        refreshList.forEach { id ->
            manager.findFragmentById(id)?.let {
                trans.remove(it).add(id, it.javaClass.newInstance())
            }
        }
        trans.commit()
    }

    override fun onBackPressed() {
        val canNavigatorUp = findNavController(R.id.navigator).navigateUp()
        if (!canNavigatorUp) moveTaskToBack(false)
    }
}