package com.lalilu.lmusic

import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.component.extension.collectWithLifeCycleOwner
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmedia.LMedia
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.compose.App
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.helper.LastTouchTimeHelper
import com.lalilu.lmusic.service.LMusicServiceConnector
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingsSp: SettingsSp by inject()
    private val connector: LMusicServiceConnector by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 判断是否已完成初次启动时的用户引导
        val isGuidingOver = settingsSp.isGuidingOver.value
        val isPermissionsGranted = ActivityCompat.checkSelfPermission(this, REQUIRE_PERMISSIONS)
        if (!isGuidingOver || isPermissionsGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityUtils.startActivity(GuidingActivity::class.java)
            finish()
            return
        }

        // 优先最高帧率运行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val params: WindowManager.LayoutParams = window.attributes
            params.preferredRefreshRate = ContextCompat.getDisplayOrDefault(this)
                .supportedModes.maxOf { it.refreshRate }
            window.attributes = params
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
                list.onEach { result ->
                    (result as? ExtensionLoadResult.Ready)
                        ?.let { lifecycle.addObserver(it.extension) }
                }
            }

        LMedia.initialize(this)

        lifecycle.addObserver(connector)
        lifecycle.addObserver(ExtensionManager)
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

