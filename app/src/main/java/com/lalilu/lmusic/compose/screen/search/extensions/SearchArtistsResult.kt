package com.lalilu.lmusic.compose.screen.search.extensions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.LazyGridContent
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lartist.component.ArtistCard
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lplayer.MPlayer

class SearchArtistsResult(
    private val artistsResult: () -> List<LArtist>
) : LazyGridContent {

    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        return fun LazyGridScope.() {
            if (artistsResult().isNotEmpty()){
                item(
                    key = "${this@SearchArtistsResult::class.java.name}_Header",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Text(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = "艺术家搜索结果",
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            itemsIndexed(
                items = artistsResult(),
                key = { _, item -> item.id },
                contentType = { _, item -> item::class.java },
                span = { _, _ -> GridItemSpan(maxLineSpan) }
            ) { index, item ->
                ArtistCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    title = item.name,
                    subTitle = "#$index",
                    songCount = item.songs.size.toLong(),
                    imageSource = { item.songs.firstOrNull() },
                    isPlaying = { item.songs.any { MPlayer.isItemPlaying(it.id) } },
                    onClick = {
                        AppRouter.route("/pages/artist/detail")
                            .with("artistName", item.id)
                            .push()
                    }
                )
            }
        }
    }
}