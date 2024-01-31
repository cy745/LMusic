package com.lalilu.lmusic.extension

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.singleViewModel
import com.lalilu.extension_core.Content
import com.lalilu.extension_core.Ext
import com.lalilu.extension_core.Extension
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.viewmodel.LibraryViewModel


@Ext
class ExtDailyRecommend : Extension {
    override fun getContentMap(): Map<String, @Composable (Map<String, String>) -> Unit> = mapOf(
        Content.COMPONENT_HOME to { Content() }
    )

    @Composable
    private fun Content(
        vm: LibraryViewModel = singleViewModel(),
    ) {
        Column {
            Text(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
                    .fillMaxWidth(),
                text = "每日推荐",
                style = MaterialTheme.typography.h6,
                color = dayNightTextColor()
            )
            RecommendRow(
                items = { vm.dailyRecommends.value },
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
}