package com.lalilu.lplaylist.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.component.base.DialogScreen
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.viewmodel.PlaylistViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

data class PlaylistCreateScreen(
    private val mediaIds: List<String> = emptyList()
) : DynamicScreen(), DialogScreen {
    override val key: ScreenKey
        get() = mediaIds.toString()

    @Composable
    override fun Content() {
        PlaylistCreateScreen()
    }
}

@Composable
private fun DynamicScreen.PlaylistCreateScreen(
    playlistVM: PlaylistViewModel = koinViewModel()
) {
    val navigator: GlobalNavigator = koinInject()

    Column(
        modifier = Modifier
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            .navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "创建歌单")
            TextButton(onClick = {
                navigator.navigateTo(
                    PlaylistAddToScreen(
                        ids = listOf(
                            "12312",
                            "1231231241"
                        )
                    )
                )
            }) {
                Text(text = "确认创建")
            }
        }
    }
}

