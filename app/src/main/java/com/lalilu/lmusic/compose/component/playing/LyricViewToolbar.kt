package com.lalilu.lmusic.compose.component.playing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState
import com.lalilu.R
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.split
import com.lalilu.component.extension.transform
import com.lalilu.component.settings.SettingFilePicker
import com.lalilu.component.settings.SettingProgressSeekBar
import com.lalilu.component.settings.SettingStateSeekBar
import com.lalilu.component.settings.SettingSwitcher
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings
import com.lalilu.lmusic.compose.screen.playing.lyric.SerializableFont
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.extension.SleepTimerSmallEntry
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

private val LyricViewActionDialog = DialogItem.Dynamic(backgroundColor = Color.Transparent) {
    val settingsSp: SettingsSp = koinInject()
    val settings: DataSaverMutableState<LyricSettings> = koinInject(named("LyricSettings"))
    val lyricTypefacePath = settings.split(
        getValue = { it.mainFont },
        setValue = { value.copy(mainFont = it) },
        transform = transform(
            from = { SerializableFont.LoadedFont(it) },
            to = { item ->
                when (item) {
                    is SerializableFont.DeviceFont -> item.fontName
                    is SerializableFont.LoadedFont -> item.fontPath
                    null -> ""
                }
            }
        )
    )

    Surface(
        modifier = Modifier
            .padding(15.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(modifier = Modifier) {
            SettingStateSeekBar(
                state = {
                    when (settings.value.textAlign) {
                        TextAlign.Start -> 0
                        TextAlign.Center -> 1
                        TextAlign.End -> 2
                        else -> -1
                    }
                },
                onStateUpdate = {
                    settings.value = settings.value.copy(
                        textAlign = when (it) {
                            0 -> TextAlign.Start
                            1 -> TextAlign.Center
                            2 -> TextAlign.End
                            else -> TextAlign.Start
                        }
                    )
                },
                selection = stringArrayResource(id = R.array.lyric_gravity_text).toList(),
                title = stringResource(R.string.preference_lyric_settings_text_gravity)
            )
            SettingProgressSeekBar(
                value = { settings.value.mainFontSize.value },
                onValueUpdate = { settings.value = settings.value.copy(mainFontSize = it.sp) },
                title = "歌词文字大小",
                valueRange = 14..36
            )
            SettingSwitcher(
                title = "歌词模糊效果",
                subTitle = "为歌词添加一点模糊效果",
                state = { settings.value.blurEffectEnable },
                onStateUpdate = { settings.value = settings.value.copy(blurEffectEnable = it) }
            )
            SettingSwitcher(
                title = "歌词页展开时隐藏其他组件",
                subTitle = "简化界面显示效果",
                state = settingsSp.autoHideSeekbar,
            )
            SettingFilePicker(
                state = lyricTypefacePath,
                title = "自定义字体",
                subTitle = "请选择TTF格式的字体文件",
                mimeType = "font/ttf"
            )
        }
    }
}

@Composable
fun LyricViewToolbar() {
    val settings: DataSaverMutableState<LyricSettings> = koinInject(named("LyricSettings"))

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val iconAlpha = animateFloatAsState(
            targetValue = if (settings.value.translationVisible) 1f else 0.5f,
            label = ""
        )

        SleepTimerSmallEntry()

        IconButton(onClick = { DialogWrapper.push(LyricViewActionDialog) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_text),
                contentDescription = "",
                tint = Color.White
            )
        }

        IconButton(onClick = {
            settings.value = settings.value.copy(
                translationVisible = !settings.value.translationVisible
            )
        }) {
            Icon(
                modifier = Modifier.graphicsLayer { alpha = iconAlpha.value },
                painter = painterResource(id = R.drawable.translate_2),
                contentDescription = "",
                tint = Color.White
            )
        }
    }
}