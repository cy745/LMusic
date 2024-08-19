package com.lalilu.lmusic.extension

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.compose.screen.songs.SongsScreen
import com.lalilu.lmusic.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterialApi::class)
fun LazyListScope.dailyRecommend(
    libraryVM: LibraryViewModel,
) {
    item {
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
    item {
        AnimatedContent(
            targetState = LocalWindowSize.current.widthSizeClass,
            label = ""
        ) { windowWidthSizeClass ->
            when (windowWidthSizeClass) {
                WindowWidthSizeClass.Medium -> RecommendRowForSizeMedium(libraryVM)
                WindowWidthSizeClass.Expanded -> RecommendRowForSizeExpanded(libraryVM)
                else -> RecommendRow(
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
    }
}


@Composable
fun RecommendRowForSizeMedium(libraryVM: LibraryViewModel) {
    val items by remember { derivedStateOf { libraryVM.dailyRecommends.value.take(3) } }

    Row(
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.getOrNull(0)?.let {
            RecommendCard2(
                item = { it },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", it.id)
                        .jump()
                }
            )
        }

        items.getOrNull(1)?.let {
            RecommendCard2(
                item = { it },
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight(),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", it.id)
                        .jump()
                }
            )
        }

        items.getOrNull(2)?.let {
            RecommendCard2(
                item = { it },
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight(),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", it.id)
                        .jump()
                }
            )
        }
    }
}

@Composable
fun RecommendRowForSizeExpanded(libraryVM: LibraryViewModel) {
    val items by remember { derivedStateOf { libraryVM.dailyRecommends.value.take(3) } }

    Row(
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.getOrNull(0)?.let {
            RecommendCard2(
                item = { it },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", it.id)
                        .jump()
                }
            )
        }

        Column(
            modifier = Modifier
                .width(275.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.getOrNull(1)?.let {
                RecommendCard2(
                    item = { it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClick = {
                        AppRouter.route("/pages/songs/detail")
                            .with("mediaId", it.id)
                            .jump()
                    }
                )
            }

            items.getOrNull(2)?.let {
                RecommendCard2(
                    item = { it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClick = {
                        AppRouter.route("/pages/songs/detail")
                            .with("mediaId", it.id)
                            .jump()
                    }
                )
            }
        }
    }
}
