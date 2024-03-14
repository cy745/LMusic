package com.lalilu.lmusic.extension

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.viewmodel.LibraryViewModel

fun LazyListScope.dailyRecommend(
    libraryVM: LibraryViewModel,
) {
    item {
        Text(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
                .fillMaxWidth(),
            text = "每日推荐",
            style = MaterialTheme.typography.h6,
            color = dayNightTextColor()
        )
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


fun LazyListScope.dailyRecommendVertical(
    libraryVM: LibraryViewModel,
) {
    item {
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = "每日推荐",
            style = MaterialTheme.typography.h6,
            color = dayNightTextColor()
        )
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