package com.lalilu.lmusic.compose.screen.guiding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.blankj.utilcode.util.ActivityUtils
import com.lalilu.common.HapticUtils
import com.lalilu.lmusic.MainActivity
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.ui.CLICK_PART_LEFT
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.CLICK_PART_RIGHT
import com.lalilu.ui.ClickPart
import com.lalilu.ui.NewSeekBar
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SeekbarGuidingPage(
    settingsSp: SettingsSp = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showLyric = remember { mutableStateOf(false) }
    val playPause = remember { mutableStateOf(false) }
    val seekToNext = remember { mutableStateOf(false) }
    val seekToPrevious = remember { mutableStateOf(false) }
    val seekToPosition = remember { mutableStateOf(false) }
    val cancelSeekToPosition = remember { mutableStateOf(false) }
    val expendLibrary = remember { mutableStateOf(false) }

    var isGuidingOver by settingsSp.isGuidingOver
    val reUpdateDelay = 200L

    LaunchedEffect(Unit) {
        showLyric.value = false
        playPause.value = false
        seekToNext.value = false
        seekToPrevious.value = false
        seekToPosition.value = false
        seekToPosition.value = false
        cancelSeekToPosition.value = false
        expendLibrary.value = false
    }

    val complete: () -> Unit = {
        context.getActivity()?.apply {
            isGuidingOver = true

            if (!ActivityUtils.isActivityExistsInStack(MainActivity::class.java)) {
                ActivityUtils.startActivity(MainActivity::class.java)
            }
            finishAfterTransition()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ActionCard(
                    confirmTitle = "跳过",
                    onConfirm = complete
                ) {
                    // TODO 修改说明文本
                    """
                    来看看这个神奇的进度条，三种切歌方式任君挑选，选好之后照着下面的提示来体验一下吧，当然你也可以每一种都试一试。
                    """
                }
            }
            item {
                CheckActionCard(isPassed = seekToPrevious.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[切换至上一首歌曲]: 单击进度条左侧"
                    )
                }
            }
            item {
                CheckActionCard(isPassed = seekToNext.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[切换至下一首歌曲]: 单击进度条右侧"
                    )
                }
            }
            item {
                CheckActionCard(isPassed = playPause.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[播放/暂停]: 单击进度条中部"
                    )
                }
            }
//            item {
//                CheckActionCard(isPassed = showLyric.value) {
//                    Text(
//                        fontSize = 14.sp,
//                        lineHeight = 20.sp,
//                        text = "[展开歌词页]: 长按进度条中部"
//                    )
//                }
//            }
            item {
                CheckActionCard(isPassed = seekToPosition.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[调整进度]: 左右滑动"
                    )
                }
            }
            item {
                CheckActionCard(isPassed = cancelSeekToPosition.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[取消调节进度]: 进度条上滑（振动第一下）"
                    )
                }
            }
            item {
                CheckActionCard(isPassed = expendLibrary.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[打开曲库]: 进度条上滑（振动第二下）"
                    )
                }
            }
            item {
                ActionCard(
                    confirmTitle = "结束",
                    onConfirm = complete
                ) {
                    """
                    进度条平分为三个区域，这样设计其实为了在一个进度条上，尽可能的方便操作和避免误触，可能也能算的上是某种意义上的简陋，不理解的话请多试试看。
                    
                    重试该教程在：
                    【曲库->设置->其他->新手引导】
                    """
                }
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 50.dp, vertical = 72.dp)
                .height(48.dp),
            factory = {
                NewSeekBar(it).apply {
                    maxValue = 4 * 45 * 1000F
                    cancelListeners.add(OnSeekBarCancelListener {
                        HapticUtils.haptic(this)
                        delayReUpdate(scope, cancelSeekToPosition, reUpdateDelay)
                    })

                    scrollListeners.add(object : OnSeekBarScrollToThresholdListener({ 300f }) {
                        override fun onScrollToThreshold() {
                            HapticUtils.haptic(this@apply)
                            delayReUpdate(scope, expendLibrary, reUpdateDelay)
                        }

                        override fun onScrollRecover() {
                            HapticUtils.haptic(this@apply)
                        }
                    })

                    seekToListeners.add(OnSeekBarSeekToListener {
                        delayReUpdate(scope, seekToPosition, reUpdateDelay)
                    })

                    clickListeners.add(object : OnSeekBarClickListener {
                        override fun onClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.haptic(this@apply)
                            when (clickPart) {
                                CLICK_PART_LEFT -> delayReUpdate(
                                    scope,
                                    seekToPrevious,
                                    reUpdateDelay
                                )

                                CLICK_PART_MIDDLE -> delayReUpdate(scope, playPause, reUpdateDelay)
                                CLICK_PART_RIGHT -> delayReUpdate(scope, seekToNext, reUpdateDelay)
                                else -> {}
                            }
                        }

                        override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.haptic(this@apply)
                        }

                        override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.doubleHaptic(this@apply)
                        }
                    })
                }
            })
    }
}

fun delayReUpdate(scope: CoroutineScope, updateValue: MutableState<Boolean>, delay: Long) =
    scope.launch {
        if (updateValue.value) {
            updateValue.value = false
            delay(delay)
        }
        updateValue.value = true
    }