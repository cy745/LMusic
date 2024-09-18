package com.lalilu.lmusic.extension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.LazyGridContent
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.compose.screen.songs.SongsScreen
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import org.koin.compose.koinInject

object DailyRecommend : LazyGridContent {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        val libraryVM: LibraryViewModel = koinInject()
        val windowWidthClass = LocalWindowSize.current.widthSizeClass

        return fun LazyGridScope.() {
            item(
                key = "daily_recommend_header",
                contentType = "daily_recommend_header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                RecommendTitle(
                    modifier = Modifier.padding(vertical = 8.dp),
                    title = "每日推荐",
                    onClick = {
                        val ids = libraryVM.dailyRecommends.value.map { it.mediaId }
                        AppRouter.intent(NavIntent.Push(SongsScreen(mediaIds = ids)))
                    }
                ) {
                    Chip(onClick = { libraryVM.forceUpdate() }) {
                        Text(
                            style = MaterialTheme.typography.caption,
                            text = "换一换"
                        )
                    }
                }
            }

            when (windowWidthClass) {
                WindowWidthSizeClass.Compact -> dailyRecommendForSideCompat()
                WindowWidthSizeClass.Medium -> dailyRecommendForSideMedium()
                WindowWidthSizeClass.Expanded -> dailyRecommendForSideExpanded(libraryVM)
            }
        }
    }
}

fun LazyGridScope.dailyRecommendForSideCompat() {
    item(
        key = "daily_recommend",
        contentType = "daily_recommend",
        span = { GridItemSpan(maxLineSpan) }
    ) {
        val libraryVM: LibraryViewModel = koinInject()

        RecommendRow(
            items = { libraryVM.dailyRecommends.value },
            getId = { it.id }
        ) {
            RecommendCard2(
                item = { it },
                modifier = Modifier.size(width = 250.dp, height = 250.dp),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", it.id)
                        .jump()
                }
            )
        }
    }
}

fun LazyGridScope.dailyRecommendForSideMedium() {
    dailyRecommendForSideCompat()
}

fun LazyGridScope.dailyRecommendForSideExpanded(
    libraryVM: LibraryViewModel
) {
    item(
        key = "daily_recommend_left",
        contentType = "daily_recommend_left",
        span = { GridItemSpan(8) }
    ) {
        val item = libraryVM.dailyRecommends.value.getOrNull(0)
            ?: return@item

        Row(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            RecommendCard2(
                item = { item },
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", item.id)
                        .jump()
                }
            )
        }
    }

    item(
        key = "daily_recommend_right",
        contentType = "daily_recommend_right",
        span = { GridItemSpan(4) }
    ) {
        val item = libraryVM.dailyRecommends.value.getOrNull(1)
            ?: return@item
        val item2 = libraryVM.dailyRecommends.value.getOrNull(2)
            ?: return@item

        Column(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecommendCard2(
                item = { item },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", item.id)
                        .jump()
                }
            )

            RecommendCard2(
                item = { item2 },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", item2.id)
                        .jump()
                }
            )
        }
    }
}