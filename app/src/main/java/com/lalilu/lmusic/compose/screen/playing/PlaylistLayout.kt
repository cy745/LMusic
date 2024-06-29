package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import coil3.compose.AsyncImage
import com.lalilu.common.base.Playable
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lplayer.LPlayer
import org.koin.compose.koinInject


data class Item<T>(
    val data: T,
    val key: String
)

fun <T : Any> List<Item<T>>.diff(
    items: List<T>,
    getId: (T) -> String
): List<Item<T>> {
    val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize(): Int = this@diff.size
        override fun getNewListSize(): Int = items.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return this@diff[oldItemPosition].data == items[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return this@diff[oldItemPosition].data == items[newItemPosition]
        }
    }, false)

    val tempList: MutableList<Item<T>?> = this.toMutableList()
    result.dispatchUpdatesTo(object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            repeat(count) { tempList.add(position, null) }
        }

        override fun onRemoved(position: Int, count: Int) {
            repeat(count) { tempList.removeAt(position) }
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
        }
    })

    val newGenerationId = System.currentTimeMillis().toString()
    (0 until maxOf(items.size, tempList.size)).forEach { index ->
        val oldItem = tempList.getOrNull(index)
        val newItem = items.getOrNull(index)
        if (oldItem == null && newItem != null) {
            tempList[index] = Item(
                key = "${newGenerationId}_${getId(newItem)}",
                data = newItem
            )
        }
    }

    return tempList.filterNotNull()
}

@Composable
fun PlaylistLayout(
    modifier: Modifier = Modifier,
    playingVM: IPlayingViewModel = koinInject()
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val listState = rememberLazyListState()

    val items by LPlayer.runtime.info.listFlow.collectAsState(initial = emptyList())
    var actualItems by remember { mutableStateOf(emptyList<Item<Playable>>()) }

    LaunchedEffect(items) {
        val newList = actualItems.diff(items) { it.mediaId }
        val newListFirst = newList.firstOrNull()
        val oldListFirst = actualItems.firstOrNull()

        // 若无法获取新列表的首元素，则说明新列表为空，及时返回
        if (newListFirst == null) {
            actualItems = emptyList()
            return@LaunchedEffect
        }

        // 判断新列表的首元素是否处于可视范围内
        val isNewListTopVisible = listState.layoutInfo.visibleItemsInfo
            .any { it.key == newListFirst.key }

        // 判断旧列表的首元素是否处于可视范围内
        val isOldListTopVisible = oldListFirst?.let { item ->
            listState.layoutInfo.visibleItemsInfo
                .any { it.key == item.key }
        } ?: false

        if (isNewListTopVisible || isOldListTopVisible) {
            actualItems = emptyList()
            view.post { actualItems = newList }
        } else {
            actualItems = newList
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = actualItems,
            key = { it.key },
            contentType = { Playable::class.java }
        ) { item ->
            MediaCard(
                modifier = Modifier.animateItem(),
                item = item.data,
                onPlayItem = {
                    playingVM.play(mediaId = item.data.mediaId, playOrPause = true)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    GlobalNavigatorImpl.goToDetailOf(mediaId = item.data.mediaId)
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
                model = item,
                contentDescription = null
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
