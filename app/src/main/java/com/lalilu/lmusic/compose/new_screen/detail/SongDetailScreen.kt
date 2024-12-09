package com.lalilu.lmusic.compose.new_screen.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.R
import com.lalilu.common.ext.requestFor
import com.lalilu.component.base.LocalEnhanceSheetState
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.component.extension.DynamicTipsItem
import com.lalilu.component.override.ModalBottomSheetValue
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.screen.detail.provideSongPlayAction
import com.lalilu.lplayer.extensions.QueueAction
import com.zhangke.krouter.annotation.Destination
import com.zhangke.krouter.annotation.Param
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

@Destination("/pages/songs/detail")
data class SongDetailScreen(
    @Param val mediaId: String
) : Screen, ScreenActionFactory, ScreenInfoFactory, ScreenType.Detail {
    override val key: ScreenKey = "${super.key}:$mediaId"

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.screen_title_song_detail) }
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> = remember(this) {
        listOfNotNull<ScreenAction>(
            requestFor<ScreenAction>(
                qualifier = named("like_action"),
                parameters = { parametersOf(mediaId) }
            ),
            provideSongPlayAction(mediaId),
            ScreenAction.Static(
                title = { stringResource(id = R.string.button_set_song_to_next) },
                color = { Color(0xFF00AC84) },
                onAction = {
                    val song = LMedia.get<LSong>(id = mediaId) ?: return@Static

                    QueueAction.AddToNext(song.id).action()
                    DynamicTipsItem.Static(
                        title = song.metadata.title,
                        subTitle = "下一首播放",
                        imageData = song
                    ).show()
                }
            ),
        )
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
                    val enhanceSheetState = LocalEnhanceSheetState.current
                    val progress by remember(enhanceSheetState) {
                        derivedStateOf {
                            enhanceSheetState?.progress(
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