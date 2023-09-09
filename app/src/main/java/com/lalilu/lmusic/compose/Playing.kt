package com.lalilu.lmusic.compose

import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.ConvertUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.lalilu.R
import com.lalilu.common.ColorAnimator
import com.lalilu.common.HapticUtils
import com.lalilu.databinding.FragmentPlayingRebuildBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.NewPlayingAdapter
import com.lalilu.lmusic.adapter.loadCover
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.component.settings.FileSelectWrapper
import com.lalilu.lmusic.compose.new_screen.ScreenData
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.helper.LastTouchTimeHelper
import com.lalilu.lmusic.ui.AppbarBehavior
import com.lalilu.lmusic.ui.AppbarStateHelper
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.lplayer.playback.Playback
import com.lalilu.ui.CLICK_PART_LEFT
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.CLICK_PART_RIGHT
import com.lalilu.ui.ClickPart
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import com.lalilu.ui.OnValueChangeListener
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get

@OptIn(ExperimentalAnimationApi::class)
object Playing {


    @Composable
    fun ToolbarContent(settingsSp: SettingsSp) {
        var isDrawTranslation by settingsSp.isDrawTranslation
        var isEnableBlurEffect by settingsSp.isEnableBlurEffect

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
                transitionSpec = { fadeIn() with fadeOut() }, label = ""
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


    var backPressCallback: (AppbarStateHelper.State) -> Unit = {}
        private set

    @Composable
    fun Content(
        playingVM: PlayingViewModel = get(),
        settingsSp: SettingsSp = get(),
        navController: NavController = LocalNavigatorHost.current,
    ) {
        val isDrawTranslation by remember { settingsSp.isDrawTranslation }
        val isEnableBlurEffect by remember { settingsSp.isEnableBlurEffect }
        val keepScreenOnWhenLyricExpanded by remember { settingsSp.keepScreenOnWhenLyricExpanded }
        val nowState = remember { mutableStateOf<AppbarStateHelper.State?>(null) }

        val keepScreenOn = remember {
            derivedStateOf { keepScreenOnWhenLyricExpanded && nowState.value == AppbarStateHelper.State.EXPENDED }
        }

        AndroidViewBinding(factory = { inflater, parent, attachToParent ->
            FragmentPlayingRebuildBinding.inflate(inflater, parent, attachToParent).apply {
                val adapter = initAdapter(navController, playingVM) {
                    fmRecyclerView.scrollToPosition(0)
                }
                initBehavior(nowState)
                initRecyclerView(adapter)
                initCover()
                initSeekBar(playingVM, settingsSp)
                initToolBar { ToolbarContent(settingsSp) }

                backPressCallback = {
                    (fmAppbarLayout.behavior as? AppbarBehavior)?.apply {
                        positionHelper.animateToState(it)
                    }
                }

                bindData(adapter, playingVM, settingsSp)
                bindEvent(keepScreenOn = { keepScreenOn.value })
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
            .setOnLongClickCB {
                BottomSheetWrapper.show()
                if (navController.currentDestination?.route == ScreenData.SongsDetail.destination.route) {
                    navController.popBackStack()
                }
                navController.navigate(
                    SongDetailScreenDestination(
                        mediaId = it,
                        fromPlaying = true
                    )
                ) {
                    launchSingleTop = true
                }
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
                    onScrollToTop()
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
    }

    private fun FragmentPlayingRebuildBinding.initBehavior(
        nowState: MutableState<AppbarStateHelper.State?>,
    ) {
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
            positionHelper.addListenerForToMinProgress { progress, fromUser ->
                if (!fromUser) return@addListenerForToMinProgress

                fmTopPic.alpha = progress
            }
            positionHelper.addListenerForToMaxProgress { progress, fromUser ->
                if (!fromUser) return@addListenerForToMaxProgress

                fmTopPic.scalePercent = progress
                fmTopPic.blurPercent = progress

                fmLyricViewX.alpha = progress
                fmEdgeTransparentView.alpha = progress
            }
            positionHelper.addListenerForFullProgress { progress, fromUser ->
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
            fmAppbarLayout.aspectRatio = it ?: 1f
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
                SmartModalBottomSheet.show()
            }

            override fun onScrollRecover() {
                HapticUtils.haptic(root)
                BottomSheetWrapper.hide()
                SmartModalBottomSheet.hide()
            }
        })

        maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
            override fun onClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.haptic(root)
                when (clickPart) {
                    CLICK_PART_LEFT -> playingVM.browser.skipToPrevious()
                    CLICK_PART_MIDDLE -> playingVM.browser.sendCustomAction(Playback.PlaybackAction.PlayPause)
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

        var now: Long
        var lastTime = System.currentTimeMillis()

        maSeekBar.onValueChangeListener.add(OnValueChangeListener {
            now = System.currentTimeMillis()
            if (lastTime + 50 > now) return@OnValueChangeListener

            fmLyricViewX.updateTime(it.toLong())
            lastTime = now
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

        playingVM.currentSongs.observe(activity) { songs ->
            adapter.setDiffData(songs)
        }

        playingVM.currentPlaying.observe(activity) {
            maSeekBar.maxValue = it?.durationMs?.toFloat() ?: 0f
//            binding.fmCollapseLayout.title = it?.name?.takeIf(String::isNotBlank)
//                ?: activity.getString(R.string.default_slogan)
            fmTopPic.loadCover(it)

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

    private fun FragmentPlayingRebuildBinding.bindEvent(
        keepScreenOn: () -> Boolean,
    ) {
        val activity = root.context.getActivity()!!

        LastTouchTimeHelper.listenToLastTouch(activity.lifecycleScope) {
            if (it > 0) root.keepScreenOn = keepScreenOn()
        }
    }
}

