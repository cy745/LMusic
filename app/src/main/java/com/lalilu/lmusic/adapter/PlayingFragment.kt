package com.lalilu.lmusic.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.component.settings.FileSelectWrapper
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.ui.AppbarBehavior
import com.lalilu.lmusic.ui.AppbarStateHelper
import com.lalilu.lmusic.utils.extension.durationToTime
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
import com.lalilu.ui.OnTapEventListener
import com.lalilu.ui.OnValueChangeListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class PlayingFragment : Fragment() {

    private lateinit var binding: FragmentPlayingRebuildBinding

    private val playingVM: PlayingViewModel by inject()
    private val settingsSp: SettingsSp by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlayingRebuildBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NewPlayingAdapter.Builder()
            .setOnLongClickCB {

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
                    binding.fmRecyclerView.scrollToPosition(0)
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

        val behavior = binding.fmAppbarLayout.behavior as? AppbarBehavior
        behavior?.apply {
            positionHelper.addOnStateChangeListener { newState, oldState, updateFromUser ->
                if (
                    newState == AppbarStateHelper.State.EMPTY2
                    && oldState != AppbarStateHelper.State.EMPTY2
                    && updateFromUser
                ) {
                    HapticUtils.haptic(binding.fmAppbarLayout, HapticUtils.Strength.HAPTIC_STRONG)
                }
            }
            positionHelper.addListenerForToMinProgress { progress, fromUser ->
                if (!fromUser) return@addListenerForToMinProgress

                binding.fmTopPic.alpha = progress
            }
            positionHelper.addListenerForToMaxProgress { progress, fromUser ->
                if (!fromUser) return@addListenerForToMaxProgress

                binding.fmTopPic.scalePercent = progress
                binding.fmTopPic.blurPercent = progress

                binding.fmLyricViewX.alpha = progress
                binding.fmEdgeTransparentView.alpha = progress
            }
            positionHelper.addListenerForFullProgress { progress, fromUser ->
                binding.motionLayout.progress = progress
            }
        }

        binding.fmRecyclerView.adapter = adapter
        binding.fmRecyclerView.setItemViewCacheSize(5)

        binding.fmTopPic.aspectRatioLiveData.observe(viewLifecycleOwner) {
            binding.fmAppbarLayout.aspectRatio = it ?: 1f
        }
        binding.fmTopPic.palette.observe(viewLifecycleOwner) {
            ColorAnimator.setBgColorFromPalette(it, binding.fmAppbarLayout::setBackgroundColor)
            ColorAnimator.setBgColorFromPalette(it, binding.maSeekBar::thumbColor::set)
        }


        binding.maSeekBar.setSwitchToCallback(
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_shuffle_line)!! to {
                settingsSp.playMode.value = PlayMode.Shuffle.value
                DynamicTips.push(
                    title = "随机播放",
                    subTitle = "随机播放将触发列表重排序"
                )
            },
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_order_play_line)!! to {
                settingsSp.playMode.value = PlayMode.ListRecycle.value
                DynamicTips.push(
                    title = "列表循环",
                    subTitle = "循环循环循环"
                )
            },
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_repeat_one_line)!! to {
                settingsSp.playMode.value = PlayMode.RepeatOne.value
                DynamicTips.push(
                    title = "单曲循环",
                    subTitle = "循环循环循环"
                )
            }
        )

        binding.maSeekBar.valueToText = { it.toLong().durationToTime() }
        binding.maSeekBar.scrollListeners.add(object :
            OnSeekBarScrollToThresholdListener({ 300f }) {
            override fun onScrollToThreshold() {
                HapticUtils.haptic(binding.root)
                SmartModalBottomSheet.show()
            }

            override fun onScrollRecover() {
                HapticUtils.haptic(binding.root)
                SmartModalBottomSheet.hide()
            }
        })
        binding.maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
            override fun onClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.haptic(binding.root)
                when (clickPart) {
                    CLICK_PART_LEFT -> playingVM.browser.skipToPrevious()
                    CLICK_PART_MIDDLE -> playingVM.browser.sendCustomAction(Playback.PlaybackAction.PlayPause)
                    CLICK_PART_RIGHT -> playingVM.browser.skipToNext()
                    else -> {
                    }
                }
            }

            override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.haptic(binding.root)
            }

            override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                HapticUtils.doubleHaptic(binding.root)
            }
        })
        binding.maSeekBar.seekToListeners.add(OnSeekBarSeekToListener { value ->
            playingVM.browser.seekTo(value)
        })
        binding.maSeekBar.cancelListeners.add(OnSeekBarCancelListener {
            HapticUtils.haptic(binding.root)
        })

        var now: Long
        var lastTime = System.currentTimeMillis()
        binding.maSeekBar.onValueChangeListener.add(OnValueChangeListener {
            now = System.currentTimeMillis()
            if (lastTime + 50 > now) return@OnValueChangeListener
            binding.fmLyricViewX.updateTime(it.toLong())
            lastTime = now
        })


        val onLeaveEventFlow = callbackFlow {
            trySend(System.currentTimeMillis())
            val enterListener = OnTapEventListener { trySend(-1L) }
            val leaveListener = OnTapEventListener { trySend(System.currentTimeMillis()) }
            binding.maSeekBar.onTapEnterListeners.add(enterListener)
            binding.maSeekBar.onTapLeaveListeners.add(leaveListener)
            awaitClose {
                binding.maSeekBar.onTapEnterListeners.remove(enterListener)
                binding.maSeekBar.onTapLeaveListeners.remove(leaveListener)
            }
        }

        // 控制显隐进度条,松手3秒后隐藏进度条
//        onLeaveEventFlow.debounce(3000)
//            .combine(flowForHideSeekBar) { time, hide ->
//                if (time > 0) maSeekBar.animateAlphaTo(if (hide) 0f else 100f)
//            }
//            .launchIn(activity.lifecycleScope)

        // 监听全局触摸事件，当十秒未有操作后，更新当前的keepScreenOn属性
//        LMusicFlowBus.lastTouchTime.flow()
//            .debounce(10000)
//            .mapLatest { if (it > 0) root.keepScreenOn = keepScreenOn.value }
//            .launchIn(activity.lifecycleScope)

        playingVM.currentSongs.observe(requireActivity()) { songs ->
            adapter.setDiffData(songs)
        }

        playingVM.currentPlaying.observe(requireActivity()) {
            binding.maSeekBar.maxValue = it?.durationMs?.toFloat() ?: 0f
//            binding.fmCollapseLayout.title = it?.name?.takeIf(String::isNotBlank)
//                ?: activity.getString(R.string.default_slogan)
            binding.fmTopPic.loadCover(it)

            if (it != null) {
                DynamicTips.push(
                    title = it.name,
                    subTitle = "播放中",
                    imageData = it
                )
            }
        }

        binding.fmComposeToolbar.setContent {
            var isDrawTranslation by settingsSp.isDrawTranslation
            var isEnableBlurEffect by settingsSp.isEnableBlurEffect

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
        }

        playingVM.currentPosition.observe(requireActivity()) {
            binding.maSeekBar.updateValue(it.toFloat())
        }

        playingVM.currentLyric.observe(requireActivity()) {
            binding.fmLyricViewX.setLyricEntryList(emptyList())
            binding.fmLyricViewX.loadLyric(it?.first, it?.second)
        }

        settingsSp.apply {
            lyricGravity.flow(true).onEach {
                when (it ?: Config.DEFAULT_SETTINGS_LYRIC_GRAVITY) {
                    0 -> binding.fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                    1 -> binding.fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                    2 -> binding.fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
                }
            }.launchIn(lifecycleScope)

            lyricTextSize.flow(true).onEach {
                val sp = it ?: Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
                val textSize = ConvertUtils.sp2px(sp.toFloat()).toFloat()
                binding.fmLyricViewX.setNormalTextSize(textSize)
                binding.fmLyricViewX.setCurrentTextSize(textSize * 1.2f)
            }.launchIn(lifecycleScope)

            lyricTypefacePath.flow(true).onEach {
                // TODO 排查偶现的字体未加载问题
                it ?: return@onEach run {
                    binding.fmLyricViewX.setLyricTypeface(typeface = null)
                }
                binding.fmLyricViewX.setLyricTypeface(path = it)
            }.launchIn(lifecycleScope)
        }
    }
}