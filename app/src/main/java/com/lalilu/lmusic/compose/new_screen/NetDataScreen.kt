package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.apis.NetworkSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.LyricCard
import com.lalilu.lmusic.compose.component.card.SearchInputBar
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.lalilu.lmusic.viewmodel.NetDataViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@Destination
@Composable
fun NetDataScreen(
    mediaId: String,
    mediaVM: LMediaViewModel = get(),
    netDataVM: NetDataViewModel = get(),
    navigator: DestinationsNavigator
) {
    val song = mediaVM.requireSong(mediaId = mediaId) ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "[Error]加载失败 #$mediaId")
        }
        return
    }

    val keyword = "${song.name} ${song._artist}"
    val msg = remember { mutableStateOf("") }
    val showActionBar = remember { mutableStateOf(true) }
    val netSongs = remember { mutableStateListOf<NetworkSong>() }
    var selectedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        netDataVM.getSongResult(
            keyword = keyword,
            items = netSongs,
            msg = msg
        )
    }

    SmartBar.RegisterExtraBarContent(showState = showActionBar) {
        SearchInputBar(
            modifier = Modifier.padding(vertical = 5.dp),
            value = keyword,
            onSearchFor = {
                if (it.isNotEmpty()) {
                    netDataVM.getSongResult(
                        keyword = it,
                        items = netSongs,
                        msg = msg
                    )
                }
            },
            onChecked = {
                netDataVM.saveMatchNetworkData(
                    mediaId = song.id,
                    networkSong = netSongs.getOrNull(selectedIndex),
                    success = { navigator.navigateUp() }
                )
            }
        )
    }

    SmartContainer.LazyColumn {
        item {
            NavigatorHeader(
                title = stringResource(id = R.string.destination_label_match_network_data),
                subTitle = netSongs.size.takeIf { it > 0 }
                    ?.let { "共搜索到 ${netSongs.size} 条结果" }
                    ?: msg.value,
            )
        }
        itemsIndexed(netSongs) { index, item ->
            LyricCard(
                song = item,
                selected = index == selectedIndex,
                onClick = { selectedIndex = index }
            )
        }
    }
}