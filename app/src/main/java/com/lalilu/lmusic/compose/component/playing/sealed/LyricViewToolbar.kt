package com.lalilu.lmusic.compose.component.playing.sealed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.compose.component.settings.FileSelectWrapper
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.LPlayer
import org.koin.compose.koinInject

@Composable
fun LyricViewToolbar(
    settingsSp: SettingsSp = koinInject(),
    playingVM: PlayingViewModel = singleViewModel(),
) {
    var isDrawTranslation by settingsSp.isDrawTranslation
    var isEnableBlurEffect by settingsSp.isEnableBlurEffect
    val song by LPlayer.runtime.info.playingFlow.collectAsState(null)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 25.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val iconAlpha1 = animateFloatAsState(
            targetValue = if (isEnableBlurEffect) 1f else 0.5f, label = ""
        )
        val iconAlpha2 = animateFloatAsState(
            targetValue = if (isDrawTranslation) 1f else 0.5f, label = ""
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = song?.title ?: stringResource(id = R.string.default_slogan),
            color = Color.White.copy(0.5f)
        )

        FileSelectWrapper(state = settingsSp.lyricTypefacePath) { launcher, _ ->
            IconButton(onClick = { launcher.launch("font/ttf") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_text),
                    contentDescription = "",
                    tint = Color.White
                )
            }
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