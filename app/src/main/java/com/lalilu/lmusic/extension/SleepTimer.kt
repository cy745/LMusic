package com.lalilu.lmusic.extension

import android.os.CountDownTimer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.commandiron.wheel_picker_compose.core.WheelTextPicker
import com.lalilu.component.LongClickableTextButton
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.enableFor
import com.lalilu.component.settings.SettingSwitcher
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject
import java.time.LocalTime
import com.lalilu.component.R as ComponentR


data class CustomCountDownTimer(
    val millisInFuture: Long,
    val countDownInterval: Long = 1000L,
    val onTickCallback: (millisUntilFinished: Long) -> Unit = {},
    val onFinishCallback: () -> Unit = {}
) : CountDownTimer(millisInFuture, countDownInterval) {
    var startTime: Long = -1L
        private set

    override fun onTick(millisUntilFinished: Long) {
        onTickCallback(millisUntilFinished)
    }

    override fun onFinish() {
        onFinishCallback()
    }

    fun doStart() {
        startTime = System.currentTimeMillis()
        start()
    }

    fun calcMillisUntilFinished(): Long {
        return millisInFuture - (System.currentTimeMillis() - startTime)
    }
}


object SleepTimerContext {
    private var countDownTimer by mutableStateOf<CustomCountDownTimer?>(null)
    var millisUntilFinished by mutableLongStateOf(0L)
        private set

    fun start(
        millisInFuture: Long,
        countDownInterval: Long = 1000L,
        onFinish: () -> Unit = {}
    ) {
        countDownTimer?.cancel()
        countDownTimer = CustomCountDownTimer(
            millisInFuture = millisInFuture,
            countDownInterval = countDownInterval,
            onTickCallback = { millisUntilFinished = it },
            onFinishCallback = {
                onFinish()
                countDownTimer = null
            }
        )
        countDownTimer?.doStart()
    }

    fun stop() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun isRunning(): Boolean {
        return countDownTimer != null
    }
}

private val SleepTimerDialog = DialogItem.Dynamic(backgroundColor = Color.Transparent) {
    SleepTimer()
}

@Composable
fun SleepTimerSmallEntry() {
    IconButton(onClick = { DialogWrapper.push(SleepTimerDialog) }) {
        Icon(
            painter = painterResource(id = ComponentR.drawable.ic_time_line),
            contentDescription = "",
            tint = Color.White
        )
    }
}

@Composable
fun SleepTimer(
    settingsSp: SettingsSp = koinInject()
) {
    val isRunning = remember { derivedStateOf { SleepTimerContext.isRunning() } }
    val pauseWhenCompletion = remember { settingsSp.obtain<Boolean>("pauseWhenCompletion", false) }
    val defaultSecondToCountDown =
        remember { settingsSp.obtain<Int>("defaultSecondToCountDown", 0) }

    SleepTimer(
        isRunning = isRunning,
        millisUntilFinished = { SleepTimerContext.millisUntilFinished },
        pauseWhenCompletion = pauseWhenCompletion,
        defaultSecondToCountDown = defaultSecondToCountDown,
        onActionBtnLongClick = {
            if (isRunning.value) {
                LPlayer.controller.doAction(PlayerAction.PauseWhenCompletion(true))
                SleepTimerContext.stop()
            } else {
                val millisSecond = defaultSecondToCountDown.value.toLong()
                    .coerceAtLeast(0) * 1000
                SleepTimerContext.start(
                    millisInFuture = millisSecond,
                    onFinish = {
                        if (pauseWhenCompletion.value) {
                            PlayerAction.PauseWhenCompletion().action()
                        } else {
                            PlayerAction.Pause.action()
                        }
                    }
                )
            }
        }
    )
}


@Composable
fun SleepTimer(
    isRunning: State<Boolean>,
    millisUntilFinished: () -> Long,
    defaultSecondToCountDown: MutableState<Int>,
    pauseWhenCompletion: MutableState<Boolean>,
    onActionBtnLongClick: () -> Unit = {}
) {
    val tipsShow = remember { mutableLongStateOf(0L) }
    val stopColor = remember { Color(0xFFF82424) }
    val startColor = remember { Color(0xFF24A500) }
    val animateColor = animateColorAsState(
        targetValue = if (isRunning.value) stopColor else startColor,
        label = ""
    )

    LaunchedEffect(tipsShow.longValue) {
        delay(3000)

        if (!isActive) return@LaunchedEffect
        tipsShow.longValue = 0L
    }

    val textValue = remember {
        derivedStateOf {
            val timeStamp = millisUntilFinished()
            val hour = timeStamp / 3600000
            val minute = timeStamp / 60000 % 60
            val second = timeStamp / 1000 % 60

            if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
            else "%02d:%02d".format(minute, second)
        }
    }

    Surface(
        modifier = Modifier
            .padding(15.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = !isRunning.value) {
                TimeWheelPicker(
                    modifier = Modifier.padding(bottom = 5.dp),
                    startSeconds = { defaultSecondToCountDown.value },
                    onTimeSelect = { defaultSecondToCountDown.value = it }
                )
            }

            SettingSwitcher(
                modifier = Modifier.padding(bottom = 5.dp),
                state = pauseWhenCompletion,
                title = "播放完最后一首歌曲后暂停",
                subTitle = "计时结束前调整皆有效",
                enableContentClickable = false
            )

            LongClickableTextButton(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(8.dp),
                enableLongClickMask = true,
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = animateColor.value.copy(alpha = 0.15f),
                    contentColor = animateColor.value
                ),
                onClick = { tipsShow.longValue = System.currentTimeMillis() },
                onLongClick = {
                    tipsShow.longValue = 0
                    onActionBtnLongClick()
                }
            ) {
                AnimatedContent(
                    targetState = isRunning.value,
                    contentAlignment = Alignment.Center,
                    label = ""
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (it) {
                            NumberAnimation(numberStr = textValue.value)
                        } else {
                            Text(text = "START")
                        }

                        AnimatedVisibility(visible = tipsShow.longValue > 0) {
                            Text(
                                modifier = Modifier.alpha(0.6f),
                                text = "长按以" + if (isRunning.value) "取消" else "开始",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = animateColor.value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeWheelPicker(
    modifier: Modifier = Modifier,
    startSeconds: () -> Int,
    onTimeSelect: (Int) -> Unit,
) {
    val presets = remember {
        linkedMapOf(
            -1 to "自定义",
            5 * 60 to "5分钟",
            10 * 60 to "10分钟",
            15 * 60 to "15分钟",
            30 * 60 to "30分钟",
            45 * 60 to "45分钟",
            1 * 60 * 60 to "1小时",
            3 * 60 * 60 to "3小时",
            5 * 60 * 60 to "5小时",
            10 * 60 * 60 to "10小时",
            12 * 60 * 60 to "12小时"
        )
    }
    val presetsTextList = remember { presets.values.toList() }
    val startIndex = remember {
        val index = presets.keys.indexOf(startSeconds()).takeIf { it >= 0 } ?: 0
        mutableIntStateOf(index)
    }
    val startTime = remember {
        val time = LocalTime.ofSecondOfDay(startSeconds().toLong())
        mutableStateOf(time)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AnimatedContent(
            targetState = startIndex.intValue,
            label = ""
        ) { index ->
            WheelTextPicker(
                size = DpSize(96.dp, 128.dp),
                rowCount = 3,
                startIndex = index,
                texts = presetsTextList,
                color = dayNightTextColor(),
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    shape = RoundedCornerShape(8.dp),
                    border = null
                ),
                onScrollFinished = { snappedIndex ->
                    val seconds = presets.keys.toList().getOrNull(snappedIndex)
                        ?.takeIf { it >= 0 } ?: 0
                    if (seconds == startSeconds() || seconds == 0) return@WheelTextPicker null

                    println("onScrollFinished: $seconds")
                    onTimeSelect(seconds)
                    startTime.value = LocalTime.ofSecondOfDay(seconds.toLong())
                    null
                }
            )
        }

        AnimatedContent(
            targetState = startTime.value,
            label = ""
        ) { localTime ->
            WheelTimePicker(
                modifier = Modifier,
                rowCount = 3,
                startTime = localTime,
                textColor = dayNightTextColor(),
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    shape = RoundedCornerShape(8.dp),
                    border = null
                ),
                onSnappedTime = { snappedTime ->
                    val seconds = snappedTime.toSecondOfDay()
                    if (seconds == startSeconds()) return@WheelTimePicker

                    println("onSnappedTime: $seconds")
                    onTimeSelect(seconds)
                    val index = presets.keys.indexOf(seconds).takeIf { it >= 0 } ?: 0
                    startIndex.intValue = index
                }
            )
        }
    }
}

@Composable
fun NumberAnimation(
    numberStr: String
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (char in numberStr) {
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it }) togetherWith (fadeOut() + slideOutVertically { -it })
                },
                label = ""
            ) {
                Text(
                    modifier = Modifier.enableFor(
                        enable = { char.isDigit() },
                        forFalse = { widthIn(min = 15.dp) },
                        forTrue = { widthIn(min = 20.dp) }
                    ),
                    text = it.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
fun SleepTimerPreview() {
    SleepTimer(
        isRunning = remember { mutableStateOf(false) },
        pauseWhenCompletion = remember { mutableStateOf(false) },
        defaultSecondToCountDown = remember { mutableIntStateOf(0) },
        millisUntilFinished = { 0L }
    )
}

@Preview
@Composable
fun SleepTimerPreview2() {
    SleepTimer(
        isRunning = remember { mutableStateOf(true) },
        pauseWhenCompletion = remember { mutableStateOf(true) },
        defaultSecondToCountDown = remember { mutableIntStateOf(5 * 60) },
        millisUntilFinished = { 3000L }
    )
}