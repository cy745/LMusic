package com.lalilu.lmusic.compose.new_screen.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.R
import com.lalilu.component.IconButton
import com.lalilu.component.base.LocalBottomSheetNavigator
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.DynamicTipsItem
import com.lalilu.component.override.ModalBottomSheetValue
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.base.IconCheckButton
import com.lalilu.lmusic.compose.presenter.DetailScreenAction
import com.lalilu.lmusic.compose.presenter.DetailScreenIsPlayingPresenter
import com.lalilu.lmusic.compose.presenter.DetailScreenLikeBtnPresenter
import com.lalilu.lplayer.extensions.QueueAction
import com.zhangke.krouter.annotation.Destination
import com.zhangke.krouter.annotation.Param

@Destination("/song/detail")
data class SongDetailScreen(
    @Param val mediaId: String
) : Screen, ScreenActionFactory, ScreenInfoFactory {
    override val key: ScreenKey = "${super.key}:$mediaId"

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(title = R.string.screen_title_song_detail)
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        return remember {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.button_set_song_to_next,
                    color = Color(0xFF00AC84),
                    onAction = {
                        val song = LMedia.get<LSong>(id = mediaId) ?: return@StaticAction
                        QueueAction.AddToNext(song.mediaId).action()
                        DynamicTipsItem.Static(
                            title = song.title,
                            subTitle = "下一首播放",
                            imageData = song.imageSource
                        ).show()
                    }
                ),
                ScreenAction.ComposeAction {
                    val state = DetailScreenLikeBtnPresenter(mediaId)

                    IconCheckButton(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(4f / 3f),
                        shape = RectangleShape,
                        getIsChecked = { state.isLiked },
                        onCheckedChange = { state.onAction(if (it) DetailScreenAction.Like else DetailScreenAction.UnLike) },
                        checkedColor = MaterialTheme.colors.primary,
                        checkedIconRes = R.drawable.ic_heart_3_fill,
                        normalIconRes = R.drawable.ic_heart_3_line
                    )
                },
                ScreenAction.ComposeAction {
                    val state = DetailScreenIsPlayingPresenter(mediaId)

                    AnimatedContent(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(3f / 2f),
                        targetState = state.isPlaying,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = ""
                    ) { isPlaying ->
                        val icon =
                            if (isPlaying) R.drawable.ic_pause_line else R.drawable.ic_play_line
                        IconButton(
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF006E7C),
                            shape = RectangleShape,
                            text = stringResource(id = R.string.text_button_play),
                            icon = painterResource(id = icon),
                            onClick = { state.onAction(DetailScreenAction.PlayPause) }
                        )
                    }
                },
            )
        }
    }

    @Composable
    override fun Content() {
        val song = LMedia.getFlow<LSong>(id = mediaId)
            .collectAsState(initial = null)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when {
                song.value != null -> {
                    val bottomSheet = LocalBottomSheetNavigator.current
                    val progress by remember(bottomSheet?.sheetState) {
                        derivedStateOf {
                            bottomSheet?.sheetState?.progress(
                                ModalBottomSheetValue.HalfExpanded,
                                ModalBottomSheetValue.Expanded,
                            ) ?: 0f
                        }
                    }

                    SongDetailContent(
                        song = song.value!!,
                        progress = progress
                    )
                }

                else -> {}
            }
        }
    }
}