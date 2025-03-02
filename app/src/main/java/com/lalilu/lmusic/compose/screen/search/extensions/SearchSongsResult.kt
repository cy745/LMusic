package com.lalilu.lmusic.compose.screen.search.extensions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.RemixIcon
import com.lalilu.component.LazyGridContent
import com.lalilu.component.card.SongCard
import com.lalilu.component.state
import com.lalilu.lmedia.entity.LSong
import com.lalilu.remixicon.Arrows
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.arrows.arrowDownSLine
import com.lalilu.remixicon.arrows.arrowUpSLine
import com.lalilu.remixicon.media.music2Line

class SearchSongsResult(
    private val songsResult: () -> List<LSong>,
) : LazyGridContent {

    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        val collapsed = remember { mutableStateOf(false) }
        val favouriteIds = state("favourite_ids", emptyList<String>())

        return fun LazyGridScope.() {
            if (songsResult().isNotEmpty()) {
                stickyHeader(
                    key = "${this@SearchSongsResult::class.java.name}_Header",
                    contentType = "sticky"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { collapsed.value = !collapsed.value }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = RemixIcon.Media.music2Line,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onBackground
                        )

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            text = "歌曲搜索结果 (${songsResult().size})",
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold
                        )

                        AnimatedContent(
                            targetState = collapsed.value,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = ""
                        ) { collapsedValue ->
                            Icon(
                                imageVector = if (collapsedValue) RemixIcon.Arrows.arrowDownSLine
                                else RemixIcon.Arrows.arrowUpSLine,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                }
            }

            if (!collapsed.value) {
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
                        song = { it },
                        isFavour = { favouriteIds.value.contains(it.id) }
                    )
                }
            }
        }
    }
}