package com.lalilu.lmusic.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterialApi::class)
fun LazyListScope.dailyRecommend(
    libraryVM: LibraryViewModel,
) {
    item {
        RecommendTitle(
            title = "每日推荐",
            onClick = {
                val ids = libraryVM.dailyRecommends.value.map { it.mediaId }
                GlobalNavigatorImpl.showSongs(ids)
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
        RecommendRow(
            items = { libraryVM.dailyRecommends.value },
            getId = { it.id }
        ) {
            RecommendCard2(
                item = { it },
                contentModifier = Modifier.size(width = 250.dp, height = 250.dp),
                onClick = { GlobalNavigatorImpl.goToDetailOf(mediaId = it.id) }
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
fun LazyListScope.dailyRecommendVertical(
    libraryVM: LibraryViewModel,
) {
    item {
        RecommendTitle(
            modifier = Modifier.width(250.dp),
            paddingValues = PaddingValues(),
            title = "每日推荐",
            onClick = {
                val ids = libraryVM.dailyRecommends.value.map { it.mediaId }
                GlobalNavigatorImpl.showSongs(ids)
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
    items(
        items = libraryVM.dailyRecommends.value,
        key = { it.id },
        contentType = { "dailyRecommendsCard" }
    ) {
        RecommendCard2(
            item = { it },
            contentModifier = Modifier.size(width = 250.dp, height = 250.dp),
            onClick = { GlobalNavigatorImpl.goToDetailOf(mediaId = it.id) }
        )
    }
}