package com.lalilu.lmusic

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.indexer.FilterType
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.compose.App
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.OnBackPressHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

@OptIn(
    ExperimentalCoroutinesApi::class
)
class MainActivity : AppCompatActivity() {
    private val settingsSp: SettingsSp by inject()
    private val browser: LMusicBrowser by inject()
    private var hasNewIntent = false

    override fun onResume() {
        super.onResume()
        if (hasNewIntent) {
            browser.whenConnected { handleIntent(intent) }
            hasNewIntent = false
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        hasNewIntent = true
    }

    private fun handleIntent(intent: Intent) {
        if (intent.data == null) return

        val start = System.currentTimeMillis()
        LMedia.read(this, intent.data) {
            if (it?.id == null) {
                ToastUtils.showShort("解析失败: [${intent.data}]")
                return@read
            }
            LogUtils.i("[onNewIntent]: 解析完成, 耗时：${System.currentTimeMillis() - start}ms", it)
            browser.addToNext(it.id)
            browser.playById(it.id)
        }
    }

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
        settingsSp.darkModeOption.flow(true).onEach {
            AppCompatDelegate.setDefaultNightMode(
                when (it) {
                    1 -> MODE_NIGHT_YES
                    2 -> MODE_NIGHT_NO
                    else -> MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }.launchIn(lifecycleScope)

        /**
         * 在LMedia初始化完成前，设置元素筛选器逻辑
         */
        Indexer.setFilterPipe {
            settingsSp.run {
                enableUnknownFilter.flow(true).flatMapLatest { hideUnknown ->
                    blockedPaths.flow(true).flatMapLatest { blockedPaths ->
                        durationFilter.flow(true).mapLatest { minDuration ->
                            mutableListOf<FilterType>().also {
                                if (blockedPaths != null) {
                                    it.add(FilterType.Path(blockedPaths))
                                }
                                if (hideUnknown == true) {
                                    it.add(FilterType.UnknownArtist)
                                }
                                if (minDuration != null && minDuration > 0) {
                                    it.add(FilterType.Duration(minDuration))
                                }
                            }
                        }
                    }
                }
            }
        }

        Indexer.startListen()
        lifecycle.addObserver(browser)
        SystemUiUtil.immerseNavigationBar(this)
        SystemUiUtil.immersiveCutout(window)

        val backPressHelper = OnBackPressHelper()
        // 注册返回键事件回调
        onBackPressedDispatcher.addCallback { this@MainActivity.moveTaskToBack(false) }
        onBackPressedDispatcher.addCallback(backPressHelper)

        setContent { App.Content(activity = this) }

        volumeControlStream = AudioManager.STREAM_MUSIC
        browser.whenConnected { handleIntent(intent) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                LMusicFlowBus.lastTouchTime.post(lifecycleScope, -1L)
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            -> {
                LMusicFlowBus.lastTouchTime.post(lifecycleScope, System.currentTimeMillis())
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}

