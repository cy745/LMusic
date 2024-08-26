package com.lalilu.lmusic.compose.screen.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.lmusic.compose.presenter.DetailScreenAction
import com.lalilu.lmusic.compose.presenter.DetailScreenIsPlayingPresenter
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.pauseLine
import com.lalilu.remixicon.media.playLine


@OptIn(ExperimentalMaterialApi::class)
fun provideSongPlayAction(mediaId: String): ScreenAction.Dynamic {
    return ScreenAction.Dynamic { actionContext ->
        val state = DetailScreenIsPlayingPresenter(mediaId)
        val color = Color(0xFF006E7C)

        Surface(
            color = color.copy(0.2f),
            onClick = { state.onAction(DetailScreenAction.PlayPause) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    modifier = Modifier
                        .fillMaxHeight(),
                    targetState = state.isPlaying,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = ""
                ) { isPlaying ->
                    val icon = if (isPlaying) RemixIcon.Media.pauseLine
                    else RemixIcon.Media.playLine

                    Image(
                        modifier = Modifier.size(24.dp),
                        imageVector = icon,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = color)
                    )
                }

                if (actionContext.isFullyExpanded) {
                    Text(
                        text = stringResource(id = R.string.text_button_play),
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}