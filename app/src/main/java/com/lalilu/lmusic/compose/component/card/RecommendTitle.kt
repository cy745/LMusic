package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.recomposeHighlighter
import kotlinx.coroutines.delay

@Composable
fun RecommendTitle(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {},
    extraContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp)
            .recomposeHighlighter(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.h6,
            color = dayNightTextColor()
        )
        extraContent()
    }
}

@Composable
fun RecommendTitle(modifier: Modifier = Modifier, title: String, onClick: () -> Unit = {}) {
    RecommendTitle(modifier = modifier, title = title, onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
            contentDescription = "",
            tint = dayNightTextColor()
        )
    }
}

@Composable
fun <I> RecommendRow(
    items: List<I>,
    getId: (I) -> Any,
    scrollToStartWhenUpdate: Boolean = false,
    itemContent: @Composable LazyItemScope.(item: I) -> Unit
) {
    val rowState = rememberLazyListState()

    if (scrollToStartWhenUpdate) {
        LaunchedEffect(items) {
            delay(50L)
            rowState.animateScrollToItem(0)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = SpringSpec(stiffness = Spring.StiffnessLow)
            )
            .recomposeHighlighter(),
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        items(items = items, key = getId) {
            itemContent(it)
        }
    }
}
