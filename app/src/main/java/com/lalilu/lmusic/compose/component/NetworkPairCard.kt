package com.lalilu.lmusic.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.lmusic.apis.PLATFORM_KUGOU
import com.lalilu.lmusic.apis.PLATFORM_NETEASE
import com.lalilu.lmusic.datasource.entity.MNetworkData
import com.lalilu.lmusic.utils.extension.dayNightTextColor

@Composable
fun NetworkPairCard(
    modifier: Modifier = Modifier,
    item: () -> MNetworkData?,
    onClick: () -> Unit = {}
) {
    NetworkPairCard(
        modifier = modifier,
        title = { item()?.title },
        subTitle = { item()?.songId },
        platform = { item()?.platform },
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NetworkPairCard(
    modifier: Modifier = Modifier,
    title: () -> String?,
    subTitle: () -> String?,
    platform: () -> Int?,
    onClick: () -> Unit = {}
) {
    // TODO title、subTitle数据不更新
    val platformR = remember {
        when (platform()) {
            PLATFORM_KUGOU -> R.drawable.kugou
            PLATFORM_NETEASE -> R.drawable.ic_netease_cloud_music_line
            else -> null
        }
    }

    Surface(
        modifier = modifier.padding(horizontal = 20.dp),
        elevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NetworkPairCardHeader()
            NetworkPairCardContent(
                title = title,
                subTitle = subTitle,
                iconExtra = {
                    platformR?.let {
                        Icon(
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Bottom),
                            painter = painterResource(id = it),
                            tint = dayNightTextColor(0.1f),
                            contentDescription = "netease"
                        )
                    }
                },
                buttonExtra = {

                }
            )
        }
    }
}

@Composable
fun NetworkPairCardHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "网络匹配歌曲ID", fontSize = 14.sp)
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
            contentDescription = "Network Pair Card Header"
        )
    }
}

@Composable
fun NetworkPairCardContent(
    modifier: Modifier = Modifier,
    title: () -> String?,
    subTitle: () -> String?,
    iconExtra: @Composable RowScope.() -> Unit = {},
    buttonExtra: @Composable RowScope.() -> Unit = {}
) {
    val titleR = remember { title() }
    val subTitleR = remember { subTitle() }

    if (titleR.isNullOrEmpty() || subTitleR.isNullOrBlank()) {
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = subTitleR ?: "",
                fontSize = 10.sp,
                color = dayNightTextColor(0.3f),
            )
            Text(
                text = titleR ?: "",
                fontSize = 16.sp,
                color = dayNightTextColor(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = buttonExtra
            )
        }
        iconExtra()
    }
}