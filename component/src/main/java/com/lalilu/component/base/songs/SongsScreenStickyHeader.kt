package com.lalilu.component.base.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.StickyHeaderOffsetHelper
import com.lalilu.lmedia.extension.GroupIdentity


@Composable
fun SongsScreenStickyHeader(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    group: GroupIdentity,
    minOffset: () -> Int,
    onClickGroup: (GroupIdentity) -> Unit
) {
    StickyHeaderOffsetHelper(
        modifier = modifier,
        key = group,
        listState = listState,
        minOffset = minOffset,
    ) { modifierFromHelper, isFloating ->
        Box(
            modifier = modifierFromHelper
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(min = 64.dp)
                .height(IntrinsicSize.Max)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClickGroup(group) }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onBackground.copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(color = MaterialTheme.colors.background)
        ) {
            Text(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                text = group.text
            )

            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .padding(start = 6.dp)
                    .width(2.dp)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(color = Color(0xFF0088FF)) }
            )
        }
    }
}