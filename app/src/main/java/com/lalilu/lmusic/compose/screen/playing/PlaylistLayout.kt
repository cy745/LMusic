package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lalilu.common.base.Playable
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lplayer.LPlayer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun PlaylistLayout(
    modifier: Modifier = Modifier,
    playingVM: IPlayingViewModel = koinInject()
) {
    val items by LPlayer.runtime.info.listFlow.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    DisposableEffect(items) {
        scope.launch {
            println("items: ${items.firstOrNull()?.mediaId}")
            listState.animateScrollToItem(0)
        }
        onDispose { }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = items,
            key = { it.mediaId },
            contentType = { Playable::class.java }
        ) { item ->
            MediaCard(
                Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessVeryLow),
                ),
                item = item,
                onPlayItem = {
                    playingVM.play(mediaId = item.mediaId, playOrPause = true)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    GlobalNavigatorImpl.goToDetailOf(mediaId = item.mediaId)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCard(
    modifier: Modifier = Modifier,
    item: Playable,
    onPlayItem: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onPlayItem() },
                onLongClick = { onLongClick() }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colors.background.copy(0.15f),
            elevation = 2.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colors.onBackground.copy(0.1f)
            )
        ) {
            AsyncImage(
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop,
                model = item, contentDescription = null
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )
            Text(
                text = item.subTitle,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colors.onBackground.copy(0.7f)
            )
        }
    }
}
