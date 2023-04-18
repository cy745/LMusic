package com.lalilu.lmusic.compose.screen

import android.view.View
import android.view.WindowManager
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
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
import com.lalilu.common.HapticUtils
import com.lalilu.common.OnDoubleClickListener
import com.lalilu.common.SystemUiUtil
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.NewPlayingAdapter
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.new_screen.ScreenData
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.service.playback.Playback
import com.lalilu.lmusic.utils.OnBackPressHelper
import com.lalilu.lmusic.utils.SeekBarHandler
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_CLICK
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_DOUBLE_CLICK
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_LONG_CLICK
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.ClickPart
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import com.lalilu.ui.OnValueChangeListener
import com.lalilu.ui.appbar.MyAppbarBehavior
import com.lalilu.ui.internal.StateHelper.Companion.STATE_COLLAPSED
import com.lalilu.ui.internal.StateHelper.Companion.STATE_FULLY_EXPENDED
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
@ExperimentalMaterialApi
fun PlayingScreen(
    onBackPressHelper: OnBackPressHelper,
    lMusicSp: LMusicSp = get(),
    playingVM: PlayingViewModel = get(),
    navController: NavController = LocalNavigatorHost.current
) {
    val systemUiController = rememberSystemUiController()

    AndroidViewBinding(factory = { inflater, parent, attachToParent ->
        FragmentPlayingBinding.inflate(inflater, parent, attachToParent).apply {
            val activity = parent.context.getActivity()!!
            val seekBarActionHandler = SeekBarHandler(
                onPlayNext = { playingVM.browser.skipToNext() },
                onPlayPause = { playingVM.browser.sendCustomAction(Playback.PlaybackAction.PlayPause) },
                onPlayPrevious = { playingVM.browser.skipToPrevious() }
            )

            val statusBarHeight = SystemUiUtil.getFixedStatusHeight(activity)

            val behavior = fmAppbarLayout.behavior as MyAppbarBehavior
            activity.setSupportActionBar(fmToolbar)
            fmToolbar.setPadding(0, statusBarHeight, 0, 0)
            fmToolbar.layoutParams.apply { height += statusBarHeight }

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
                    playingVM.browser.playById(it)
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
                    if (seekBarActionHandler.clickHandleMode != CLICK_HANDLE_MODE_CLICK) {
                        playingVM.browser.sendCustomAction(Playback.PlaybackAction.PlayPause)
                        return
                    }
                    seekBarActionHandler.handle(clickPart)
                }

                override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.haptic(this@apply.root)
                    if (seekBarActionHandler.clickHandleMode != CLICK_HANDLE_MODE_LONG_CLICK || clickPart == CLICK_PART_MIDDLE) {
                        fmAppbarLayout.autoToggleExpand()
                        return
                    }
                    seekBarActionHandler.handle(clickPart)
                }

                override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.doubleHaptic(this@apply.root)
                    if (seekBarActionHandler.clickHandleMode != CLICK_HANDLE_MODE_DOUBLE_CLICK) return
                    seekBarActionHandler.handle(clickPart)
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

            lMusicSp.apply {
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
                    it ?: return@onEach run {
                        fmLyricViewX.setLyricTypeface(typeface = null)
                    }
                    fmLyricViewX.setLyricTypeface(path = it)
                }.launchIn(activity.lifecycleScope)

                seekBarHandler.flow(true).onEach {
                    seekBarActionHandler.clickHandleMode =
                        it ?: Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
                }.launchIn(activity.lifecycleScope)

                forceHideStatusBar.flow(true).flatMapLatest { forceHide ->
                    autoHideSeekbar.flow(true).flatMapLatest { autoHide ->
                        keepScreenOnWhenLyricExpanded.flow(true).flatMapLatest { keepScreenOn ->
                            SmartModalBottomSheet.isVisibleFlow.flatMapLatest { isBottomSheetVisible ->
                                behavior.stateHelper.nowStateFlow.mapLatest { nowState ->
                                    // 通过判断当前是否展开歌词页来判断是否需要拦截返回键事件
                                    onBackPressHelper.isEnabled = nowState == STATE_FULLY_EXPENDED

                                    // 通过判断当前是否展开歌词页来判断是否需要保持屏幕常亮
                                    if (nowState == STATE_FULLY_EXPENDED && keepScreenOn == true && !isBottomSheetVisible) {
                                        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    } else {
                                        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    }

                                    // 通过判断当前是否展开歌词页来判断是否需要隐藏Seekbar
                                    val targetAlpha =
                                        if (autoHide == true && nowState == STATE_FULLY_EXPENDED) 0f else 100f
                                    maSeekBar.animateAlphaTo(targetAlpha)

                                    // 通过判断当前是否展开歌词页来判断是否需要隐藏状态栏
                                    // 同时考虑强制隐藏状态栏的情况和底部弹窗是否可见的情况
                                    val hideStatusBar =
                                        forceHide == true || (autoHide == true && nowState == STATE_FULLY_EXPENDED && !isBottomSheetVisible)
                                    systemUiController.isStatusBarVisible = !hideStatusBar
                                }
                            }
                        }
                    }
                }.launchIn(activity.lifecycleScope)
            }
        }
    }) {
        // 当页面重建时，若页面滚动位置与AppbarLayout的状态不匹配，则进行修正
        // TODO 应当在View的onRestoreInstanceState中进行修正
        if (fmRecyclerView.computeVerticalScrollOffset() > 0 && (fmAppbarLayout.behavior as MyAppbarBehavior).stateHelper.nowState != STATE_COLLAPSED) {
            fmAppbarLayout.setExpanded(false)
        }
    }
}