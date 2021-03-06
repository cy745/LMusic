package com.lalilu.lmusic.screen.guiding

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.MainActivity
import com.lalilu.lmusic.screen.component.settings.SettingStateSeekBar
import com.lalilu.lmusic.utils.SeekBarHandler
import com.lalilu.lmusic.utils.getActivity
import com.lalilu.lmusic.utils.safeLaunch
import com.lalilu.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay


@Composable
fun SeekbarGuidingPage(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showLyric = remember { mutableStateOf(false) }
    val playPause = remember { mutableStateOf(false) }
    val seekToNext = remember { mutableStateOf(false) }
    val seekToPrevious = remember { mutableStateOf(false) }
    val seekToPosition = remember { mutableStateOf(false) }
    val cancelSeekToPosition = remember { mutableStateOf(false) }
    val expendLibrary = remember { mutableStateOf(false) }

    val seekbarHandlerData = rememberDataSaverState(
        Config.KEY_SETTINGS_SEEKBAR_HANDLER,
        Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
    )

    val reUpdateDelay = 200L
    val seekBarHandler = remember {
        SeekBarHandler(
            onPlayNext = { delayReUpdate(scope, seekToNext, reUpdateDelay) },
            onPlayPrevious = { delayReUpdate(scope, seekToPrevious, reUpdateDelay) },
            onPlayPause = { delayReUpdate(scope, playPause, reUpdateDelay) }
        )
    }

    val seekbarHandlerValue by seekbarHandlerData
    LaunchedEffect(seekbarHandlerValue) {
        showLyric.value = false
        playPause.value = false
        seekToNext.value = false
        seekToPrevious.value = false
        seekToPosition.value = false
        seekToPosition.value = false
        cancelSeekToPosition.value = false
        expendLibrary.value = false
        seekBarHandler.clickHandleMode = seekbarHandlerValue
    }

    val currentHandlerText = when (seekbarHandlerValue) {
        0 -> "??????"
        1 -> "??????"
        else -> "??????"
    }

    val complete: () -> Unit = {
        context.getActivity()?.apply {
            SPUtils.getInstance(this.packageName, AppCompatActivity.MODE_PRIVATE)
                .put(Config.KEY_REMEMBER_IS_GUIDING_OVER, true)

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
                    confirmTitle = "??????",
                    onConfirm = complete
                ) {
                    """
                    ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    """
                }
            }
            item {
                Surface(shape = RoundedCornerShape(10.dp)) {
                    SettingStateSeekBar(
                        state = seekbarHandlerData,
                        selection = stringArrayResource(id = R.array.seekbar_handler).toList(),
                        title = "?????????????????????????????????",
                        paddingValues = PaddingValues(vertical = 20.dp, horizontal = 20.dp)
                    )
                }
            }
            item {
                CheckActionCard(isPassed = seekToPrevious.value) {
                    Crossfade(targetState = currentHandlerText) {
                        Text(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            text = "[????????????????????????]: ${it}???????????????"
                        )
                    }
                }
            }
            item {
                CheckActionCard(isPassed = seekToNext.value) {
                    Crossfade(targetState = currentHandlerText) {
                        Text(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            text = "[????????????????????????]: ${it}???????????????"
                        )
                    }
                }
            }
            item {
                CheckActionCard(isPassed = playPause.value) {
                    Crossfade(targetState = seekbarHandlerValue) {
                        Text(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            text = "[??????/??????]: ???????????????${if (it == 0) "??????" else ""}"
                        )
                    }
                }
            }
            item {
                CheckActionCard(isPassed = showLyric.value) {
                    Crossfade(targetState = seekbarHandlerValue) {
                        Text(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            text = "[???????????????]: ???????????????${if (it == 2) "??????" else ""}"
                        )
                    }
                }
            }
            item {
                CheckActionCard(isPassed = seekToPosition.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[????????????]: ????????????"
                    )
                }
            }
            item {
                CheckActionCard(isPassed = cancelSeekToPosition.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[??????????????????]: ????????????????????????????????????"
                    )
                }
            }
            item {
                CheckActionCard(isPassed = expendLibrary.value) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        text = "[????????????]: ????????????????????????????????????"
                    )
                }
            }
            item {
                ActionCard(
                    confirmTitle = "??????",
                    onConfirm = complete
                ) {
                    """
                    ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    
                    ?????????????????????
                    ?????????->??????->??????->???????????????
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
                            if (seekBarHandler.clickHandleMode != SeekBarHandler.CLICK_HANDLE_MODE_CLICK) {
                                seekBarHandler.onPlayPause()
                                return
                            }
                            seekBarHandler.handle(clickPart)
                        }

                        override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.haptic(this@apply)
                            if (seekBarHandler.clickHandleMode != SeekBarHandler.CLICK_HANDLE_MODE_LONG_CLICK || clickPart == CLICK_PART_MIDDLE) {
                                delayReUpdate(scope, showLyric, reUpdateDelay)
                                return
                            }
                            seekBarHandler.handle(clickPart)
                        }

                        override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                            HapticUtils.doubleHaptic(this@apply)
                            if (seekBarHandler.clickHandleMode != SeekBarHandler.CLICK_HANDLE_MODE_DOUBLE_CLICK) return
                            seekBarHandler.handle(clickPart)
                        }
                    })
                }
            })
    }
}

fun delayReUpdate(scope: CoroutineScope, updateValue: MutableState<Boolean>, delay: Long) =
    scope.safeLaunch {
        if (updateValue.value) {
            updateValue.value = false
            delay(delay)
        }
        updateValue.value = true
    }