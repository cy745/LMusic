package com.lalilu.lmusic.screen.component.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.screen.bean.SORT_BY_TEXT
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME

@Composable
fun LazyListSortToggleButton(
    sortByState: Int,
    onClick: () -> Unit = {}
) {
    val title = when (sortByState) {
        SORT_BY_TIME -> "添加时间"
        SORT_BY_TEXT -> "标题"
        else -> ""
    }
    LazyListSortToggleButton(
        title = title,
        onClick = onClick
    )
}

@Composable
fun LazyListSortToggleButton(
    title: String,
    onClick: () -> Unit = {}
) {
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    TextButton(
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = color.copy(alpha = 0.8f)
        ),
        onClick = onClick
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SortToggleButton(
    sortDesc: Boolean,
    onClick: () -> Unit = {}
) {
    IconButton(onClick = onClick) {
        val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        val colorFilter = ColorFilter.tint(color = color.copy(0.8f))
        Crossfade(targetState = sortDesc) { desc ->
            if (desc) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sort_desc),
                    contentDescription = "",
                    colorFilter = colorFilter
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_sort_asc),
                    contentDescription = "",
                    colorFilter = colorFilter
                )
            }
        }
    }
}