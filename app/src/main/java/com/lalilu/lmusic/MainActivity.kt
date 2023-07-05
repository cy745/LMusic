package com.lalilu.lmusic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.databinding.ActivityMainBinding
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.indexer.FilterType
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.adapter.LibraryFragment
import com.lalilu.lmusic.adapter.PlayingFragment
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
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
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

    @OptIn(ExperimentalFoundationApi::class)
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

        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this), null, false)
        setContentView(binding.root)

        binding.viewPager.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            private val fragments = arrayOf(PlayingFragment(), LibraryFragment())

            override fun getCount(): Int = 2

            override fun getItem(position: Int): Fragment {
                return fragments.getOrElse(position) { PlayingFragment() }
            }
        }

//        setContent {
//            LMusicTheme {
//                CompositionLocalProvider(
//                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
//                    LocalNavigatorHost provides rememberAnimatedNavController(),
//                ) {
//                    val pagerState = rememberPagerState(initialPage = 0)
//
//                    Box {
//                        HorizontalPager(
//                            state = pagerState,
//                            pageCount = 2,
//                            flingBehavior = PagerDefaults.flingBehavior(
//                                state = pagerState,
//                                lowVelocityAnimationSpec = spring(stiffness = Spring.StiffnessVeryLow)
//                            ),
//                            beyondBoundsPageCount = 2
//                        ) {
//                            when (it) {
//                                0 -> {
//                                    PlayingScreen(onBackPressHelper = backPressHelper)
//                                }
//
//                                1 -> {
//                                    LMusicNavHost(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .edgeTransparentForStatusBar(SmartModalBottomSheet.enableFadeEdgeForStatusBar.value)
//                                    )
//                                }
//                            }
//                        }
////                        SmartModalBottomSheet.SmartModalBottomSheetContent(
////                            sheetContent = {
////                                LMusicNavHost(
////                                    modifier = Modifier
////                                        .fillMaxSize()
////                                        .edgeTransparentForStatusBar(SmartModalBottomSheet.enableFadeEdgeForStatusBar.value)
////                                )
////                                SmartFloatBtnsContent(
////                                    modifier = Modifier.graphicsLayer {
////                                        translationY = -SmartModalBottomSheet.offset * 0.8f
////                                        alpha = SmartModalBottomSheet.offsetHalfPercent
////                                    }
////                                )
////                                SmartBarContent(
////                                    modifier = Modifier.graphicsLayer {
////                                        translationY = -SmartModalBottomSheet.offset * 0.9f
////                                        alpha = SmartModalBottomSheet.offsetHalfPercent
////                                    }
////                                )
////                            },
////                            content = {  }
////                        )
//                        ShowScreen()
//                        DynamicTips.Content(modifier = Modifier.align(Alignment.TopCenter))
//                    }
//                }
//            }
//        }
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

