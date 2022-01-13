package com.lalilu.lmusic

import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.event.LMusicPlayerModule
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.ToastUtil
import com.lalilu.lmusic.scanner.MSongScanner
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

    @Inject
    lateinit var songScanner: MSongScanner

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
            R.id.appbar_scan_song -> {
                songScanner.setScanFinish {
                    ToastUtil.text("[扫描完成]: 共计 $it 首歌曲被添加至Worker").show(this)
                }.scanStart(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        playerModule.connect()
        super.onStart()
    }

    override fun onStop() {
        playerModule.disconnect()
        super.onStop()
    }

    override fun onResume() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        val items = menu.findItem(R.id.appbar_search)
        val mSearchView = items.actionView as SearchView
        val mUnderline = mSearchView.findViewById<View>(R.id.search_plate)
        mUnderline.setBackgroundColor(Color.argb(0, 255, 255, 255))

        mSearchView.setHasTransientState(true)
        mSearchView.showDividers = SearchView.SHOW_DIVIDER_NONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSearchView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                playerModule.searchFor(newText)
                return false
            }
        })
        mSearchView.setOnCloseListener {
            playerModule.searchFor(null)
            return@setOnCloseListener false
        }
        mEvent.isSearchViewExpand.observe(this) {
            it?.get {
                if (items.isActionViewExpanded)
                    items.collapseActionView()
            }
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
}