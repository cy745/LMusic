package com.lalilu.lhistory

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.LazyGridContent
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.card.SongCard
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lhistory.viewmodel.HistoryVM
import com.lalilu.lplayer.MPlayer
import org.koin.compose.koinInject
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single


@Named("history_panel")
@Single(binds = [LazyGridContent::class])
class HistoryPanel : LazyGridContent {

    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        val historyVM = koinInject<HistoryVM>()
        val widthSizeClass = LocalWindowSize.current.widthSizeClass
        val items by historyVM.historyState

        return fun LazyGridScope.() {
            // 若列表为空，则不显示
            if (items.isEmpty()) return

            items(
                items = items,
                key = { it.id },
                contentType = { "History_item" },
                span = {
                    if (widthSizeClass == WindowWidthSizeClass.Expanded) GridItemSpan(maxLineSpan / 2)
                    else GridItemSpan(maxLineSpan)
                }
            ) { item ->
                SongCard(
                    modifier = Modifier
                        .animateItem()
                        .padding(bottom = 5.dp),
                    song = { item },
                    isPlaying = { MPlayer.isItemPlaying(item.id) },
                    onClick = {

                    },
                    onLongClick = {
                        AppRouter.route("/pages/songs/detail")
                            .with("mediaId", item.id)
                            .jump()
                    }
                )
            }
        }
    }
}