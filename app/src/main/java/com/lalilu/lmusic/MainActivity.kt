package com.lalilu.lmusic

import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmedia.LMedia
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.compose.App
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.helper.LastTouchTimeHelper
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.extension.collectWithLifeCycleOwner
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingsSp: SettingsSp by inject()
    private val browser: LMusicBrowser by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 判断是否已完成初次启动时的用户引导
        val isGuidingOver = settingsSp.isGuidingOver.get()
        val isPermissionsGranted = ActivityCompat.checkSelfPermission(this, REQUIRE_PERMISSIONS)
        if (!isGuidingOver || isPermissionsGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityUtils.startActivity(GuidingActivity::class.java)
            finish()
            return
        }

        // 深色模式控制
        settingsSp.darkModeOption.flow(true)
            .collectWithLifeCycleOwner(this) {
                AppCompatDelegate.setDefaultNightMode(
                    when (it) {
                        1 -> MODE_NIGHT_YES
                        2 -> MODE_NIGHT_NO
                        else -> MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }

        ExtensionManager.extensionsFlow
            .collectWithLifeCycleOwner(this) { list ->
                list.asSequence()
                    .filter { it.extension != null }
                    .onEach { lifecycle.addObserver(it.extension!!) }
            }

        LMedia.initialize(this)

        lifecycle.addObserver(browser)
        SystemUiUtil.immerseNavigationBar(this)
        SystemUiUtil.immersiveCutout(window)

        // 注册返回键事件回调
        onBackPressedDispatcher.addCallback { this@MainActivity.moveTaskToBack(false) }

        setContent { App.Content(activity = this) }

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        LastTouchTimeHelper.onDispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}

