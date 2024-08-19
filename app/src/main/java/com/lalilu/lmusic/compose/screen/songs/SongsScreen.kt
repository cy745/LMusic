package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.toState
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lplayer.extensions.PlayerAction
import com.zhangke.krouter.annotation.Destination
import kotlinx.coroutines.flow.Flow

@Destination("/pages/songs")
data class SongsScreen(
    private val title: String? = null,
    private val mediaIds: List<String> = emptyList()
) : Screen, ScreenInfoFactory, ScreenActionFactory, ScreenBarFactory, ScreenType.List {

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = R.string.screen_title_songs,
            icon = R.drawable.ic_music_2_line
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> = remember {
        listOf(
            ScreenAction.StaticAction(
                title = R.string.screen_action_sort,
                icon = R.drawable.ic_sort_desc,
                color = Color(0xFF1793FF),
                onAction = { }
            ),
            ScreenAction.StaticAction(
                title = R.string.screen_action_locate_playing_item,
                icon = R.drawable.ic_focus_3_line,
                color = Color(0xFF9317FF),
                onAction = { }
            ),
        )
    }

    @Transient
    private var songsSM: SongsSM? = null

    @Composable
    override fun Content() {
        val sm = rememberScreenModel { SongsSM(mediaIds) }
            .also { songsSM = it }

        SongsScreenContent(songsSM = sm)
    }
}

private class SongsSM(
    private val mediaIds: List<String>
) : ScreenModel {
    private fun flow(): Flow<List<LSong>> {
        return if (mediaIds.isEmpty()) LMedia.getFlow<LSong>()
        else LMedia.flowMapBy<LSong>(mediaIds)
    }

    val songs = flow().toState(emptyList(), screenModelScope)
}

@Composable
private fun SongsScreenContent(
    songsSM: SongsSM
) {
    val hapticFeedback = LocalHapticFeedback.current
    val listState: LazyListState = rememberLazyListState()
    val songs by songsSM.songs

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Text(text = "全部歌曲")
                Text(text = "${songs.size}首歌曲")
            }
        }

        items(
            items = songs,
            key = { it.mediaId },
            contentType = { it::class.java }
        ) {
            SongCard(
                song = { it },
                onClick = { PlayerAction.PlayById(it.mediaId).action() },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", it.mediaId)
                        .jump()
                },
            )
        }

        smartBarPadding()
    }
}