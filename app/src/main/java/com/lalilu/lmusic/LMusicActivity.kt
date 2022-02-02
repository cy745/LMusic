package com.lalilu.lmusic

import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.LMusicPlayerModule
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.ui.MySearchBar
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
    lateinit var playerModule: LMusicPlayerModule

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_search -> mEvent.collapseAppbarLayout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        playerModule.connect()
        super.onStart()
    }

    override fun onPause() {
        playerModule.disconnect()
        super.onPause()
    }

    override fun onResume() {
        volumeControlStream = AudioManager.STREAM_MUSIC
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
        if (!canNavigatorUp) super.onBackPressed()
    }
}