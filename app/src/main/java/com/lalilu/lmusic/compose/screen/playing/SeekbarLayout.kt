package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.component.extension.collectWithLifeCycleOwner
import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.ui.CLICK_PART_LEFT
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.CLICK_PART_RIGHT
import com.lalilu.ui.ClickPart
import com.lalilu.ui.NewSeekBar
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject

@Composable
fun BoxScope.SeekbarLayout(
    modifier: Modifier = Modifier,
    seekBarModifier: Modifier = Modifier,
    settingsSp: SettingsSp = koinInject(),
    animateColor: State<Color>
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 72.dp, start = 50.dp, end = 50.dp)
            .align(Alignment.BottomCenter)
    ) {
        AndroidView(
            modifier = seekBarModifier
                .fillMaxWidth()
                .height(48.dp),
            factory = { context ->
                val activity = context.getActivity()!!

                NewSeekBar(context).apply {
                    setSwitchToCallback(
                        ContextCompat.getDrawable(context, R.drawable.ic_shuffle_line)!! to {
                            settingsSp.playMode.value = PlayMode.Shuffle.value
                            DynamicTips.push(
                                title = "随机播放",
                                subTitle = "随机播放将触发列表重排序"
                            )
                        },
                        ContextCompat.getDrawable(context, R.drawable.ic_order_play_line)!! to {
                            settingsSp.playMode.value = PlayMode.ListRecycle.value
                            DynamicTips.push(
                                title = "列表循环",
                                subTitle = "循环循环循环"
                            )
                        },
                        ContextCompat.getDrawable(context, R.drawable.ic_repeat_one_line)!! to {
                            settingsSp.playMode.value = PlayMode.RepeatOne.value
                            DynamicTips.push(
                                title = "单曲循环",
                                subTitle = "循环循环循环"
                            )
                        }
                    )

                    switchIndexUpdateCallback = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    valueToText = { it.toLong().durationToTime() }

                    scrollListeners.add(object :
                        OnSeekBarScrollToThresholdListener({ 300f }) {
                        override fun onScrollToThreshold() {
                            HapticUtils.haptic(this@apply)
                            NavigationWrapper.navigator?.show()
                        }

                        override fun onScrollRecover() {
                            HapticUtils.haptic(this@apply)
                            NavigationWrapper.navigator?.hide()
                        }
                    })

                    clickListeners.add(object : OnSeekBarClickListener {
                        override fun onClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.haptic(this@apply)
                            when (clickPart) {
                                CLICK_PART_LEFT -> PlayerAction.SkipToPrevious.action()
                                CLICK_PART_MIDDLE -> PlayerAction.PlayOrPause.action()
                                CLICK_PART_RIGHT -> PlayerAction.SkipToNext.action()
                                else -> {
                                }
                            }
                        }

                        override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.haptic(this@apply)
                        }

                        override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.doubleHaptic(this@apply)
                        }
                    })

                    seekToListeners.add(OnSeekBarSeekToListener { value ->
                        PlayerAction.SeekTo(value.toLong()).action()
                    })

                    cancelListeners.add(OnSeekBarCancelListener {
                        HapticUtils.haptic(this@apply)
                    })

                    LPlayer.runtime.info.durationFlow.collectWithLifeCycleOwner(activity) {
                        maxValue = it.takeIf { it > 0f }?.toFloat() ?: 0f
                    }
                    LPlayer.runtime.info.positionFlow.collectWithLifeCycleOwner(activity) {
                        updateValue(it.toFloat())
                    }

                    snapshotFlow { animateColor.value }
                        .onEach { thumbColor = it.toArgb() }
                        .launchIn(activity.lifecycleScope)
                }
            }
        )
    }
}