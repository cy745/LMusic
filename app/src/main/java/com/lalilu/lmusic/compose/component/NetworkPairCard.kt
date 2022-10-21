package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
    onClick: () -> Unit = {},
    onDownloadLyric: () -> Unit = {},
    onDownloadCover: () -> Unit = {}
) {
    NetworkPairCard(
        modifier = modifier,
        title = { item()?.title },
        subTitle = { item()?.songId },
        platform = { item()?.platform },
        onClick = onClick,
        buttonExtra = {
            IconTextButton(
                text = "歌词",
                shape = RoundedCornerShape(20.dp),
                iconPainter = painterResource(id = R.drawable.ic_download_cloud_2_line),
                showIcon = { item()?.lyric == null },
                onClick = onDownloadLyric
            )
            AnimatedVisibility(visible = item()?.platform == PLATFORM_NETEASE) {
                IconTextButton(
                    text = "封面",
                    shape = RoundedCornerShape(20.dp),
                    iconPainter = painterResource(id = R.drawable.ic_download_cloud_2_line),
                    showIcon = { item()?.cover == null },
                    onClick = onDownloadCover
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NetworkPairCard(
    modifier: Modifier = Modifier,
    title: () -> String?,
    subTitle: () -> String?,
    platform: () -> Int?,
    onClick: () -> Unit = {},
    buttonExtra: @Composable RowScope.() -> Unit = {}
) {
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
                buttonExtra = buttonExtra,
                iconExtra = {
                    when (platform()) {
                        PLATFORM_KUGOU -> R.drawable.kugou
                        PLATFORM_NETEASE -> R.drawable.ic_netease_cloud_music_line
                        else -> null
                    }?.let {
                        Icon(
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Bottom),
                            painter = painterResource(id = it),
                            tint = dayNightTextColor(0.1f),
                            contentDescription = "Platform Icon"
                        )
                    }
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
    val titleR = title()
    val subTitleR = subTitle()

    if (titleR.isNullOrEmpty() || subTitleR.isNullOrEmpty()) {
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
                text = subTitleR,
                fontSize = 10.sp,
                color = dayNightTextColor(0.3f),
            )
            Text(
                text = titleR,
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