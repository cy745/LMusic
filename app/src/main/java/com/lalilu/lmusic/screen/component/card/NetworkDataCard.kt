package com.lalilu.lmusic.screen.component.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.R
import com.lalilu.lmusic.screen.component.button.TextWithIconButton
import com.lalilu.lmusic.viewmodel.NetworkDataViewModel

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun NetworkDataCard(
    mediaId: String,
    networkDataViewModel: NetworkDataViewModel = hiltViewModel(),
    onClick: () -> Unit = {}
) {
    val networkData = networkDataViewModel.getNetworkDataFlowByMediaId(mediaId)
        .collectAsState(null)

    Surface(
        elevation = 0.dp,
        shape = RoundedCornerShape(10.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NetworkDataCardHeader()
            if (networkData.value != null) {
                NetworkDataCardDetail(
                    songId = networkData.value!!.songId,
                    songTitle = networkData.value!!.title
                ) {
                    TextWithIconButton(
                        text = "歌词",
                        shape = RoundedCornerShape(20.dp),
                        iconPainter = painterResource(id = R.drawable.ic_download_cloud_2_line),
                        showIcon = networkData.value!!.lyric == null,
                        onClick = {
                            networkDataViewModel.saveLyricIntoNetworkData(
                                mediaId = mediaId,
                                songId = networkData.value!!.songId,
                                toastTips = ToastUtils::showShort
                            )
                        }
                    )
                    TextWithIconButton(
                        text = "封面",
                        shape = RoundedCornerShape(20.dp),
                        iconPainter = painterResource(id = R.drawable.ic_download_cloud_2_line),
                        showIcon = networkData.value!!.cover == null,
                        onClick = {
                            networkDataViewModel.saveCoverUrlIntoNetworkData(
                                mediaId = mediaId,
                                songId = networkData.value!!.songId,
                                toastTips = ToastUtils::showShort
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkDataCardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "网络匹配歌曲ID", fontSize = 14.sp)
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
            contentDescription = "more"
        )
    }
}

@Composable
fun NetworkDataCardDetail(
    songId: String,
    songTitle: String,
    buttonExtra: @Composable () -> Unit = {}
) {
    val contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "#$songId",
                fontSize = 10.sp,
                color = contentColor.copy(0.3f),
            )
            Text(
                fontSize = 16.sp,
                text = songTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) { buttonExtra() }
        }

        Icon(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.Bottom),
            painter = painterResource(id = R.drawable.ic_netease_cloud_music_line),
            tint = contentColor.copy(0.1f),
            contentDescription = "netease"
        )
    }
}