package com.lalilu.lmusic.compose.screen.search.extensions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.LazyGridContent
import com.lalilu.component.card.SongCard
import com.lalilu.lmedia.entity.LSong

class SearchSongsResult(
    private val songsResult: () -> List<LSong>
) : LazyGridContent {

    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        return fun LazyGridScope.() {
            if (songsResult().isNotEmpty()) {
                item(
                    key = "${this@SearchSongsResult::class.java.name}_Header",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = "歌曲搜索结果",
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(
                items = songsResult(),
                key = { it.id },
                contentType = { it::class.java },
                span = { GridItemSpan(maxLineSpan) }
            ) {
                SongCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    song = { it }
                )
            }
        }
    }
}