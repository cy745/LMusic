package com.lalilu.lmusic.compose.component.playing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.compose.DialogItem
import com.lalilu.lmusic.compose.DialogWrapper
import com.lalilu.lmusic.compose.component.settings.SettingFilePicker
import com.lalilu.lmusic.compose.component.settings.SettingProgressSeekBar
import com.lalilu.lmusic.compose.component.settings.SettingStateSeekBar
import com.lalilu.lmusic.datastore.SettingsSp
import org.koin.compose.koinInject

private val LyricViewActionDialog = DialogItem.Dynamic {
    val settingsSp: SettingsSp = koinInject()

    Column(modifier = Modifier.navigationBarsPadding()) {
        SettingStateSeekBar(
            state = settingsSp.lyricGravity,
            selection = stringArrayResource(id = R.array.lyric_gravity_text).toList(),
            titleRes = R.string.preference_lyric_settings_text_gravity
        )
        SettingProgressSeekBar(
            state = settingsSp.lyricTextSize,
            title = "歌词文字大小",
            valueRange = 14..36
        )
        SettingFilePicker(
            state = settingsSp.lyricTypefacePath,
            title = "自定义字体",
            subTitle = "请选择TTF格式的字体文件",
            mimeType = "font/ttf"
        )
    }
}

@Composable
fun LyricViewToolbar(
    settingsSp: SettingsSp = koinInject()
) {
    var isDrawTranslation by settingsSp.isDrawTranslation
    var isEnableBlurEffect by settingsSp.isEnableBlurEffect

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val iconAlpha1 = animateFloatAsState(
            targetValue = if (isEnableBlurEffect) 1f else 0.5f, label = ""
        )
        val iconAlpha2 = animateFloatAsState(
            targetValue = if (isDrawTranslation) 1f else 0.5f, label = ""
        )

        IconButton(onClick = { DialogWrapper.push(LyricViewActionDialog) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_text),
                contentDescription = "",
                tint = Color.White
            )
        }

        AnimatedContent(
            targetState = isEnableBlurEffect,
            transitionSpec = { fadeIn() togetherWith fadeOut() }, label = ""
        ) { enable ->
            IconButton(onClick = { isEnableBlurEffect = !enable }) {
                Icon(
                    modifier = Modifier.graphicsLayer { alpha = iconAlpha1.value },
                    painter = painterResource(id = if (enable) R.drawable.drop_line else R.drawable.blur_off_line),
                    contentDescription = "",
                    tint = Color.White
                )
            }
        }

        IconButton(onClick = { isDrawTranslation = !isDrawTranslation }) {
            Icon(
                modifier = Modifier.graphicsLayer { alpha = iconAlpha2.value },
                painter = painterResource(id = R.drawable.translate_2),
                contentDescription = "",
                tint = Color.White
            )
        }
    }
}