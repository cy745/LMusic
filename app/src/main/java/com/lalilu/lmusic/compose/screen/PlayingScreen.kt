package com.lalilu.lmusic.compose.screen

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.common.OnDoubleClickListener
import com.lalilu.common.SystemUiUtil
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.LMusicFlowBus
import com.lalilu.lmusic.adapter.NewPlayingAdapter
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.component.settings.FileSelectWrapper
import com.lalilu.lmusic.compose.new_screen.ScreenData
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.OnBackPressHelper
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.playback.Playback
import com.lalilu.ui.CLICK_PART_LEFT
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.CLICK_PART_RIGHT
import com.lalilu.ui.ClickPart
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import com.lalilu.ui.OnTapEventListener
import com.lalilu.ui.OnValueChangeListener
import com.lalilu.ui.appbar.MyAppbarBehavior
import com.lalilu.ui.internal.StateHelper.Companion.STATE_COLLAPSED
import com.lalilu.ui.internal.StateHelper.Companion.STATE_EXPENDED
import com.lalilu.ui.internal.StateHelper.Companion.STATE_FULLY_EXPENDED
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalAnimationApi::class, FlowPreview::class)
@Composable
@ExperimentalMaterialApi
fun PlayingScreen(
    onBackPressHelper: OnBackPressHelper,
    settingsSp: SettingsSp = get(),
    playingVM: PlayingViewModel = get(),
    navController: NavController = LocalNavigatorHost.current
) {
    val systemUiController = rememberSystemUiController()
    val keepScreenOnWhenLyricExpanded by settingsSp.keepScreenOnWhenLyricExpanded
    val isBottomSheetVisible by SmartModalBottomSheet.isVisible
    val forceHideStatusBar by settingsSp.forceHideStatusBar
    val autoHideSeekbar by settingsSp.autoHideSeekbar
    var isDrawTranslation by settingsSp.isDrawTranslation
    var isEnableBlurEffect by settingsSp.isEnableBlurEffect

    var nowState by remember { mutableStateOf(STATE_EXPENDED) }

    val flowForHideSeekBar = remember {
        snapshotFlow { derivedStateOf { autoHideSeekbar && nowState == STATE_FULLY_EXPENDED }.value }
    }
    val stateForHideStatusBar = remember {
        // 通过判断当前是否展开歌词页来判断是否需要隐藏状态栏
        // 同时考虑强制隐藏状态栏的情况和底部弹窗是否可见的情况
        derivedStateOf { forceHideStatusBar || (autoHideSeekbar && nowState == STATE_FULLY_EXPENDED && !isBottomSheetVisible) }
    }
    val keepScreenOn = remember {
        derivedStateOf { keepScreenOnWhenLyricExpanded && !isBottomSheetVisible && nowState == STATE_FULLY_EXPENDED }
    }

    LaunchedEffect(stateForHideStatusBar.value) {
        systemUiController.isStatusBarVisible = !stateForHideStatusBar.value
    }
    LaunchedEffect(nowState) {
        // 通过判断当前是否展开歌词页来判断是否需要拦截返回键事件
        onBackPressHelper.isEnabled = nowState == STATE_FULLY_EXPENDED
    }

    val toolbarContent: @Composable () -> Unit = remember {
        {
            Box(contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val iconAlpha1 = animateFloatAsState(
                        targetValue = if (isEnableBlurEffect) 1f else 0.5f, label = ""
                    )
                    val iconAlpha2 = animateFloatAsState(
                        targetValue = if (isDrawTranslation) 1f else 0.5f, label = ""
                    )

                    Text(
                        modifier = Modifier.weight(1f),
                        text = "",
                        color = Color.White.copy(0.5f)
                    )

                    FileSelectWrapper(state = settingsSp.lyricTypefacePath) { launcher, _ ->
                        Icon(
                            modifier = Modifier
                                .clickable { launcher.launch("font/ttf") },
                            painter = painterResource(id = R.drawable.ic_text),
                            contentDescription = "",
                            tint = Color.White
                        )
                    }

                    AnimatedContent(
                        targetState = isEnableBlurEffect,
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) { enable ->
                        Icon(
                            modifier = Modifier
                                .clickable { isEnableBlurEffect = !enable }
                                .graphicsLayer { alpha = iconAlpha1.value },
                            painter = painterResource(id = if (enable) R.drawable.drop_line else R.drawable.blur_off_line),
                            contentDescription = "",
                            tint = Color.White
                        )
                    }

                    Icon(
                        modifier = Modifier
                            .clickable { isDrawTranslation = !isDrawTranslation }
                            .graphicsLayer { alpha = iconAlpha2.value },
                        painter = painterResource(id = R.drawable.translate_2),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
    }

    AndroidViewBinding(factory = { inflater, parent, attachToParent ->
        FragmentPlayingBinding.inflate(inflater, parent, attachToParent).apply {
            val activity = parent.context.getActivity()!!
            val behavior = fmAppbarLayout.behavior as MyAppbarBehavior
            behavior.stateHelper.nowStateFlow
                .mapLatest { nowState = it }
                .launchIn(activity.lifecycleScope)

            val statusBarHeight = SystemUiUtil.getFixedStatusHeight(activity)
            activity.setSupportActionBar(fmToolbar)
            fmToolbar.setPadding(0, statusBarHeight, 0, 0)
            fmToolbar.layoutParams.apply { height += statusBarHeight }

            fmComposeToolbar.setContent(toolbarContent)

            // 双击返回顶部
            fmToolbar.setOnClickListener(OnDoubleClickListener {
                if (behavior.stateHelper.nowState == STATE_COLLAPSED) {
                    if (fmRecyclerView.computeVerticalScrollOffset() > ScreenUtils.getAppScreenHeight() * 3) {
                        fmRecyclerView.scrollToPosition(0)
                    } else {
                        fmRecyclerView.smoothScrollToPosition(0)
                    }
                }
            })

            val adapter = NewPlayingAdapter.Builder()
                .setOnLongClickCB {
                    if (navController.currentDestination?.route == ScreenData.SongsDetail.destination.route) {
                        navController.popBackStack()
                    }
                    navController.navigate(SongDetailScreenDestination(it)) {
                        launchSingleTop = true
                    }
                    SmartModalBottomSheet.show()
                }
                .setOnClickCB {
                    playingVM.play(
                        mediaId = it,
                        playOrPause = true
                    )
                }
                .setOnSwipedLeftCB {
                    DynamicTips.push(
                        title = it.name,
                        subTitle = "下一首播放",
                        imageData = it
                    )
                    playingVM.browser.addToNext(it.id)
                }
                .setOnSwipedRightCB {
                    playingVM.browser.removeById(it.id)
                }
                .setOnDataUpdatedCB { needScrollToTop ->
                    if (needScrollToTop) {
                        fmRecyclerView.scrollToPosition(0)
                    }
                }
                .setOnItemBoundCB { binding, item ->
                    playingVM.requireLyric(item) {
                        binding.songLrc.visibility = if (it) View.VISIBLE else View.INVISIBLE
                    }
                }
                .setItemCallback(object : DiffUtil.ItemCallback<LSong>() {
                    override fun areItemsTheSame(oldItem: LSong, newItem: LSong): Boolean =
                        oldItem.id == newItem.id

                    override fun areContentsTheSame(oldItem: LSong, newItem: LSong): Boolean =
                        oldItem.id == newItem.id &&
                                oldItem.name == newItem.name &&
                                oldItem.durationMs == newItem.durationMs
                })
                .build()

            onBackPressHelper.callback = {
                fmAppbarLayout.setExpanded(true)
            }

//            fmRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    if (dy <= 10) return
//                    if (!fmToolbar.hasExpandedActionView()) return
//                    if (!KeyboardUtils.isSoftInputVisible(activity)) return
//                    KeyboardUtils.hideSoftInput(activity)
//                }
//            })

            fmRecyclerView.adapter = adapter
            fmRecyclerView.layoutManager = calculateExtraLayoutSpace(this.root.context, 500)
            fmRecyclerView.setItemViewCacheSize(5)

            fmTopPic.palette.observe(activity, this::setPalette)

            maSeekBar.valueToText = { it.toLong().durationToTime() }
            maSeekBar.scrollListeners.add(object : OnSeekBarScrollToThresholdListener({ 300f }) {
                override fun onScrollToThreshold() {
                    HapticUtils.haptic(this@apply.root)
                    SmartModalBottomSheet.show()
                }

                override fun onScrollRecover() {
                    HapticUtils.haptic(this@apply.root)
                    SmartModalBottomSheet.hide()
                }
            })
            maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
                override fun onClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.haptic(this@apply.root)
                    when (clickPart) {
                        CLICK_PART_LEFT -> playingVM.browser.skipToPrevious()
                        CLICK_PART_MIDDLE -> playingVM.browser.sendCustomAction(Playback.PlaybackAction.PlayPause)
                        CLICK_PART_RIGHT -> playingVM.browser.skipToNext()
                        else -> {
                        }
                    }
                }

                override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.haptic(this@apply.root)
                }

                override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.doubleHaptic(this@apply.root)
                }
            })
            maSeekBar.seekToListeners.add(OnSeekBarSeekToListener { value ->
                playingVM.browser.seekTo(value)
            })
            maSeekBar.cancelListeners.add(OnSeekBarCancelListener {
                HapticUtils.haptic(this@apply.root)
            })

            var now: Long
            var lastTime = System.currentTimeMillis()
            maSeekBar.onValueChangeListener.add(OnValueChangeListener {
                now = System.currentTimeMillis()
                if (lastTime + 50 > now) return@OnValueChangeListener
                fmLyricViewX.updateTime(it.toLong())
                lastTime = now
            })

            val onLeaveEventFlow = callbackFlow {
                trySend(System.currentTimeMillis())
                val enterListener = OnTapEventListener { trySend(-1L) }
                val leaveListener = OnTapEventListener { trySend(System.currentTimeMillis()) }
                maSeekBar.onTapEnterListeners.add(enterListener)
                maSeekBar.onTapLeaveListeners.add(leaveListener)
                awaitClose {
                    maSeekBar.onTapEnterListeners.remove(enterListener)
                    maSeekBar.onTapLeaveListeners.remove(leaveListener)
                }
            }

            // 控制显隐进度条,松手3秒后隐藏进度条
            onLeaveEventFlow.debounce(3000)
                .combine(flowForHideSeekBar) { time, hide ->
                    if (time > 0) maSeekBar.animateAlphaTo(if (hide) 0f else 100f)
                }
                .launchIn(activity.lifecycleScope)

            // 监听全局触摸事件，当十秒未有操作后，更新当前的keepScreenOn属性
            LMusicFlowBus.lastTouchTime.flow()
                .debounce(10000)
                .mapLatest { if (it > 0) root.keepScreenOn = keepScreenOn.value }
                .launchIn(activity.lifecycleScope)

            playingVM.currentSongs.observe(activity) {
                adapter.setDiffData(it)
            }

            playingVM.currentPlaying.observe(activity) {
                maSeekBar.maxValue = it?.durationMs?.toFloat() ?: 0f
                song = it

                if (it != null) {
                    DynamicTips.push(
                        title = it.name,
                        subTitle = "播放中",
                        imageData = it
                    )
                }
            }

            playingVM.currentPosition.observe(activity) {
                maSeekBar.updateValue(it.toFloat())
            }

            playingVM.currentLyric.observe(activity) {
                fmLyricViewX.setLyricEntryList(emptyList())
                fmLyricViewX.loadLyric(it?.first, it?.second)
            }

            settingsSp.apply {
                lyricGravity.flow(true).onEach {
                    when (it ?: Config.DEFAULT_SETTINGS_LYRIC_GRAVITY) {
                        0 -> fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                        1 -> fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                        2 -> fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
                    }
                }.launchIn(activity.lifecycleScope)

                lyricTextSize.flow(true).onEach {
                    val sp = it ?: Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
                    val textSize = ConvertUtils.sp2px(sp.toFloat()).toFloat()
                    fmLyricViewX.setNormalTextSize(textSize)
                    fmLyricViewX.setCurrentTextSize(textSize * 1.2f)
                }.launchIn(activity.lifecycleScope)

                lyricTypefacePath.flow(true).onEach {
                    // TODO 排查偶现的字体未加载问题
                    it ?: return@onEach run {
                        fmLyricViewX.setLyricTypeface(typeface = null)
                    }
                    fmLyricViewX.setLyricTypeface(path = it)
                }.launchIn(activity.lifecycleScope)
            }
        }
    }) {
        // 当页面重建时，若页面滚动位置与AppbarLayout的状态不匹配，则进行修正
        // TODO 应当在View的onRestoreInstanceState中进行修正
        if (fmRecyclerView.computeVerticalScrollOffset() > 0 && (fmAppbarLayout.behavior as MyAppbarBehavior).stateHelper.nowState != STATE_COLLAPSED) {
            fmAppbarLayout.setExpanded(false)
        }
        fmLyricViewX.setIsDrawTranslation(isDrawTranslation = isDrawTranslation)
        fmLyricViewX.setIsEnableBlurEffect(isEnableBlurEffect = isEnableBlurEffect)
        fmLyricViewX.setHorizontalOffsetPercent(0.4f)
        fmLyricViewX.setItemOffsetPercent(0f)
    }
}