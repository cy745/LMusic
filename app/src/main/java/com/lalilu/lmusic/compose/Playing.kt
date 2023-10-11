package com.lalilu.lmusic.compose

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.R
import com.lalilu.common.ColorAnimator
import com.lalilu.common.HapticUtils
import com.lalilu.common.base.Playable
import com.lalilu.databinding.FragmentPlayingRebuildBinding
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.NewPlayingAdapter
import com.lalilu.lmusic.adapter.ViewEvent
import com.lalilu.lmusic.adapter.loadCover
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.playing.PlayingToolbarScaffold
import com.lalilu.lmusic.compose.component.playing.PlayingToolbarScaffoldState
import com.lalilu.lmusic.compose.component.playing.rememberPlayingToolbarScaffoldState
import com.lalilu.lmusic.compose.component.playing.sealed.LyricViewToolbar
import com.lalilu.lmusic.compose.component.playing.sealed.PlayingToolbar
import com.lalilu.lmusic.compose.new_screen.ScreenData
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.helper.LastTouchTimeHelper
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.collectWithLifeCycleOwner
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.ui.CLICK_PART_LEFT
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.CLICK_PART_RIGHT
import com.lalilu.ui.ClickPart
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import com.lalilu.ui.OnValueChangeListener
import com.lalilu.ui.appbar.AppbarBehavior
import com.lalilu.ui.appbar.AppbarStateHelper
import com.ramcosta.composedestinations.navigation.navigate
import org.koin.compose.koinInject
import kotlin.math.pow

object Playing {

    var backPressCallback: (AppbarStateHelper.State) -> Unit = {}
        private set

    @Composable
    fun Content(
        playingVM: PlayingViewModel = koinInject(),
        settingsSp: SettingsSp = koinInject(),
        navController: NavController = LocalNavigatorHost.current,
    ) {
        val systemController = rememberSystemUiController()
        val isDrawTranslation by remember { settingsSp.isDrawTranslation }
        val isEnableBlurEffect by remember { settingsSp.isEnableBlurEffect }
        val forceHideStatusBar by remember { settingsSp.forceHideStatusBar }
        val autoHideSeekbar by remember { settingsSp.autoHideSeekbar }

        val keepScreenOnWhenLyricExpanded by remember { settingsSp.keepScreenOnWhenLyricExpanded }
        val nowState = remember { mutableStateOf<AppbarStateHelper.State?>(null) }
        val playingToolbarScaffoldState = rememberPlayingToolbarScaffoldState()
        val isBottomSheetVisible by BottomSheetWrapper.collectBottomSheetIsExpended()

        val keepScreenOn by remember {
            derivedStateOf { keepScreenOnWhenLyricExpanded && nowState.value == AppbarStateHelper.State.EXPENDED }
        }
        val hideStatusBar by remember {
            derivedStateOf { forceHideStatusBar || (autoHideSeekbar && nowState.value == AppbarStateHelper.State.EXPENDED && !isBottomSheetVisible) }
        }

        LaunchedEffect(hideStatusBar) {
            systemController.isStatusBarVisible = !hideStatusBar
        }

        AndroidViewBinding(factory = { inflater, parent, attachToParent ->
            FragmentPlayingRebuildBinding.inflate(inflater, parent, attachToParent).apply {
                val adapter = initAdapter(navController, playingVM) {
                    fmRecyclerView.scrollToPosition(0)
                }
                initBehavior(nowState, playingToolbarScaffoldState)
                initRecyclerView(adapter)
                initCover()
                initSeekBar(playingVM, settingsSp)
                initToolBar {
                    PlayingToolbarScaffold(
                        state = playingToolbarScaffoldState,
                        topContent = { PlayingToolbar() },
                        bottomContent = { LyricViewToolbar() }
                    )
                }

                backPressCallback = {
                    (fmAppbarLayout.behavior as? AppbarBehavior)?.apply {
                        positionHelper.animateToState(it)
                    }
                }

                bindData(adapter, playingVM, settingsSp)
                bindEvent(keepScreenOn = { keepScreenOn })
            }
        }) {
            fmLyricViewX.setIsDrawTranslation(isDrawTranslation = isDrawTranslation)
            fmLyricViewX.setIsEnableBlurEffect(isEnableBlurEffect = isEnableBlurEffect)
            fmLyricViewX.setHorizontalOffsetPercent(0.4f)
            fmLyricViewX.setItemOffsetPercent(0f)
        }

        BottomSheetWrapper.BackHandler(enable = { nowState.value == AppbarStateHelper.State.EXPENDED }) {
            backPressCallback.invoke(AppbarStateHelper.State.NORMAL)
        }
    }

    private fun FragmentPlayingRebuildBinding.initAdapter(
        navController: NavController,
        playingVM: PlayingViewModel,
        onScrollToTop: () -> Unit = {},
    ): NewPlayingAdapter {
        return NewPlayingAdapter.Builder()
            .setViewEvent { event, item ->
                when (event) {
                    ViewEvent.OnClick -> playingVM.play(mediaId = item.mediaId, playOrPause = true)
                    ViewEvent.OnLongClick -> {
                        BottomSheetWrapper.show()
                        if (navController.currentDestination?.route == ScreenData.SongsDetail.destination.route) {
                            navController.popBackStack()
                        }
                        navController.navigate(
                            SongDetailScreenDestination(
                                mediaId = item.mediaId,
                                fromPlaying = true
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }

                    ViewEvent.OnSwipeLeft -> {
                        DynamicTips.push(
                            title = item.title,
                            subTitle = "下一首播放",
                            imageData = item.imageSource
                        )
                        playingVM.browser.addToNext(item.mediaId)
                    }

                    ViewEvent.OnSwipeRight -> playingVM.browser.removeById(item.mediaId)
                    ViewEvent.OnBind -> {

                    }
                }
            }
            .setOnDataUpdatedCB { needScrollToTop -> if (needScrollToTop) onScrollToTop() }
            .setOnItemBoundCB { binding, item ->
                playingVM.requireLyric(item) {
                    binding.songLrc.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
            }
            .setItemCallback(object : DiffUtil.ItemCallback<Playable>() {
                override fun areItemsTheSame(oldItem: Playable, newItem: Playable): Boolean =
                    oldItem.mediaId == newItem.mediaId

                override fun areContentsTheSame(oldItem: Playable, newItem: Playable): Boolean =
                    oldItem.mediaId == newItem.mediaId &&
                            oldItem.title == newItem.title &&
                            oldItem.durationMs == newItem.durationMs
            })
            .build()
    }

    private fun FragmentPlayingRebuildBinding.initBehavior(
        nowState: MutableState<AppbarStateHelper.State?>,
        toolbarState: PlayingToolbarScaffoldState,
    ) {
        // y = -2 * (x - 0.5) ^ 2 + 0.5
        val transition: (Float) -> Float = { x -> -2f * (x - 0.5f).pow(2) + 0.5f }
        val interpolator = AccelerateDecelerateInterpolator()
        val behavior = fmAppbarLayout.behavior as? AppbarBehavior
        behavior?.apply {
            positionHelper.addOnStateChangeListener { newState, oldState, updateFromUser ->
                nowState.value = newState
                if (
                    newState == AppbarStateHelper.State.EMPTY2
                    && oldState != AppbarStateHelper.State.EMPTY2
                    && updateFromUser
                ) {
                    HapticUtils.haptic(fmAppbarLayout, HapticUtils.Strength.HAPTIC_STRONG)
                }
            }
            positionHelper.addListenerForToMinProgress { progress, _ ->
                fmTopPic.alpha = progress
                toolbarState.updateProgress(minProgress = progress)
            }
            positionHelper.addListenerForToMaxProgress { progress, _ ->
                fmTopPic.scalePercent = progress
                fmTopPic.blurPercent = progress
                toolbarState.updateProgress(maxProgress = progress)

                val floatProgress = transition(progress)
                val translation = floatProgress * positionHelper.dragThreshold
                fmTopPic.translationY = translation
                fmLyricViewX.translationY = translation * 3f

                val interpolation = interpolator.getInterpolation(progress)
                val progressIncrease = (2 * interpolation - 1F).coerceAtLeast(0F)
                fmLyricViewX.alpha = progressIncrease
                fmEdgeTransparentView.alpha = progressIncrease
            }
            positionHelper.addListenerForFullProgress { progress, _ ->
                // motionLayout到达progress的[0,1]边界时会触发回调，同时触发界面重新测量
                motionLayout.progress = progress.coerceIn(0.001f, 0.999f)
            }
        }
    }

    private fun FragmentPlayingRebuildBinding.initRecyclerView(adapter: NewPlayingAdapter) {
        fmRecyclerView.layoutManager = calculateExtraLayoutSpace(root.context, 500)
        fmRecyclerView.adapter = adapter
        fmRecyclerView.setItemViewCacheSize(5)
    }

    private fun FragmentPlayingRebuildBinding.initCover() {
        val activity = root.context.getActivity()!!

        fmTopPic.aspectRatioLiveData.observe(activity) {
            fmAppbarLayout.updateAspectRatio(it ?: 1f)
        }
        fmTopPic.palette.observe(activity) {
            ColorAnimator.setBgColorFromPalette(it, fmAppbarLayout::setBackgroundColor)
            ColorAnimator.setBgColorFromPalette(it, maSeekBar::thumbColor::set)
        }
    }

    private fun FragmentPlayingRebuildBinding.initSeekBar(
        playingVM: PlayingViewModel,
        settingsSp: SettingsSp,
    ) {
        maSeekBar.setSwitchToCallback(
            ContextCompat.getDrawable(root.context, R.drawable.ic_shuffle_line)!! to {
                settingsSp.playMode.value = PlayMode.Shuffle.value
                DynamicTips.push(
                    title = "随机播放",
                    subTitle = "随机播放将触发列表重排序"
                )
            },
            ContextCompat.getDrawable(root.context, R.drawable.ic_order_play_line)!! to {
                settingsSp.playMode.value = PlayMode.ListRecycle.value
                DynamicTips.push(
                    title = "列表循环",
                    subTitle = "循环循环循环"
                )
            },
            ContextCompat.getDrawable(root.context, R.drawable.ic_repeat_one_line)!! to {
                settingsSp.playMode.value = PlayMode.RepeatOne.value
                DynamicTips.push(
                    title = "单曲循环",
                    subTitle = "循环循环循环"
                )
            }
        )


        maSeekBar.valueToText = { it.toLong().durationToTime() }

        maSeekBar.scrollListeners.add(object :
            OnSeekBarScrollToThresholdListener({ 300f }) {
            override fun onScrollToThreshold() {
                HapticUtils.haptic(root)
                BottomSheetWrapper.show()
            }

            override fun onScrollRecover() {
                HapticUtils.haptic(root)
                BottomSheetWrapper.hide()
            }
        })

        maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
            override fun onClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.haptic(root)
                when (clickPart) {
                    CLICK_PART_LEFT -> playingVM.browser.skipToPrevious()
                    CLICK_PART_MIDDLE -> playingVM.browser.playOrPause()
                    CLICK_PART_RIGHT -> playingVM.browser.skipToNext()
                    else -> {
                    }
                }
            }

            override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.haptic(root)
            }

            override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.doubleHaptic(root)
            }
        })

        maSeekBar.seekToListeners.add(OnSeekBarSeekToListener { value ->
            playingVM.browser.seekTo(value)
        })

        maSeekBar.cancelListeners.add(OnSeekBarCancelListener {
            HapticUtils.haptic(root)
        })

        maSeekBar.onValueChangeListener.add(OnValueChangeListener {
            fmLyricViewX.updateTime(it.toLong())
        })
    }

    private fun FragmentPlayingRebuildBinding.initToolBar(
        content: @Composable () -> Unit = {},
    ) {
        fmComposeToolbar.setContent(content)
    }

    private fun FragmentPlayingRebuildBinding.bindData(
        adapter: NewPlayingAdapter,
        playingVM: PlayingViewModel,
        settingsSp: SettingsSp,
    ) {
        val activity = root.context.getActivity()!!

        playingVM.runtime.playableFlow.collectWithLifeCycleOwner(activity) {
            adapter.setDiffData(it)
        }
        LPlayer.runtime.info.positionFlow.collectWithLifeCycleOwner(activity) {
            maSeekBar.updateValue(it.toFloat())
        }
        playingVM.runtime.playingFlow.collectWithLifeCycleOwner(activity) { playable ->
            maSeekBar.maxValue = playable?.durationMs?.takeIf { it > 0f }?.toFloat() ?: 0f
            fmTopPic.loadCover(playable)

            if (playable != null) {
                DynamicTips.push(
                    title = playable.title,
                    subTitle = "播放中",
                    imageData = playable.imageSource
                )
            }
        }
        playingVM.lyricRepository.currentLyric.collectWithLifeCycleOwner(activity) {
            fmLyricViewX.setLyricEntryList(emptyList())
            fmLyricViewX.loadLyric(it?.first, it?.second)
        }

        settingsSp.lyricGravity.flow(true).collectWithLifeCycleOwner(activity) {
            when (it ?: Config.DEFAULT_SETTINGS_LYRIC_GRAVITY) {
                0 -> fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                1 -> fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                2 -> fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
            }
        }
        settingsSp.lyricTextSize.flow(true).collectWithLifeCycleOwner(activity) {
            val sp = it ?: Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
            val textSize = ConvertUtils.sp2px(sp.toFloat()).toFloat()
            fmLyricViewX.setNormalTextSize(textSize)
            fmLyricViewX.setCurrentTextSize(textSize * 1.2f)
        }
        settingsSp.lyricTypefacePath.flow(true).collectWithLifeCycleOwner(activity) {
            LogUtils.i("lyricPath: $it")
            // TODO 排查偶现的字体未加载问题
            if (it == null) {
                fmLyricViewX.setLyricTypeface(typeface = null)
            } else {
                fmLyricViewX.setLyricTypeface(path = it)
            }
        }
    }

    private fun FragmentPlayingRebuildBinding.bindEvent(
        keepScreenOn: () -> Boolean,
    ) {
        val activity = root.context.getActivity()!!

        LastTouchTimeHelper.listenToLastTouchFlow().collectWithLifeCycleOwner(activity) {
            if (it <= 0) return@collectWithLifeCycleOwner
            root.keepScreenOn = keepScreenOn()
        }
    }
}

