package com.lalilu.lmusic.extension

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.extension_core.EMPTY_CONTENT
import com.lalilu.extension_core.Extension
import com.lalilu.extension_core.Ext


@Ext
class ExtDailyRecommend : Extension {
    override val homeContent: @Composable () -> Unit = {
        Surface(
            shape = RoundedCornerShape(15.dp),
            elevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(20.dp)
            ) {
                Text(text = "lorem ")
            }
        }
    }
    override val mainContent: @Composable () -> Unit = EMPTY_CONTENT
    override val bannerContent: @Composable () -> Unit = EMPTY_CONTENT
}