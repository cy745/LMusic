package com.lalilu.lmusic.compose.screen.playing

import androidx.annotation.OptIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.lalilu.common.base.Sticker
import com.lalilu.component.card.SongCard
import com.lalilu.component.card.StickerRow
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.state
import com.lalilu.lmusic.compose.screen.playing.util.DiffUtil
import com.lalilu.lmusic.compose.screen.playing.util.ListUpdateCallback
import com.lalilu.lplayer.action.PlayerAction
import kotlinx.coroutines.launch


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
    forceRefresh: () -> Boolean = { false },
    items: () -> List<MediaItem> = { emptyList() }
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val favouriteIds = state("favourite_ids", emptyList<String>())

    var actualItems by remember { mutableStateOf(emptyList<Item<MediaItem>>()) }

    LaunchedEffect(items()) {
        val newList = actualItems.diff(items()) { it.mediaId }
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

        if (isNewListTopVisible || isOldListTopVisible || forceRefresh()) {
            actualItems = emptyList()
            view.post {
                actualItems = newList
                scope.launch { listState.animateScrollToItem(0) }
            }
        } else {
            actualItems = newList
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 200.dp),
        overscrollEffect = null
    ) {
        items(
            items = actualItems,
            key = { it.key },
        ) { item ->
            SongCardReverse(
                modifier = Modifier.animateItem(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                song = { item.data },
                isFavour = { favouriteIds.value.contains(item.data.mediaId) },
                onClick = { PlayerAction.PlayById(item.data.mediaId).action() },
                onLongClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", item.data.mediaId)
                        .jump()
                }
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun SongCardReverse(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(20.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    song: () -> MediaItem,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isFavour: () -> Boolean,
    hasLyric: () -> Boolean = { false },
    isPlaying: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    showPrefix: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false },
    stickerContent: @Composable RowScope.() -> Unit = {
        StickerRow(
            isFavour = isFavour,
            hasLyric = hasLyric,
            extSticker = Sticker.ExtSticker(song().localConfiguration?.mimeType ?: "")
        )
    },
    prefixContent: @Composable (Modifier) -> Unit = {}
) {
    SongCard(
        modifier = modifier,
        dragModifier = dragModifier,
        horizontalArrangement = horizontalArrangement,
        interactionSource = interactionSource,
        paddingValues = PaddingValues(16.dp),
        title = { song().mediaMetadata.title.toString() },
        subTitle = { song().mediaMetadata.artist.toString() },
        duration = { song().mediaMetadata.durationMs ?: 0L },
        imageData = song,
        onClick = onClick,
        onLongClick = onLongClick,
        onDoubleClick = null,
        onEnterSelect = onLongClick,
        isPlaying = isPlaying,
        fixedHeight = fixedHeight,
        reverseLayout = { true },
        isSelected = isSelected,
        showPrefix = showPrefix,
        stickerContent = stickerContent,
        prefixContent = prefixContent
    )
}