package com.lalilu.lplaylist.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DialogScreen
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.extension.toColorFilter
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistSp
import org.koin.compose.koinInject
import com.lalilu.component.R as componentR

data class PlaylistAddToScreen(
    private val ids: List<String>
) : DynamicScreen(), DialogScreen {
    override val key: ScreenKey = "${super<DynamicScreen>.key}:${ids.hashCode()}"

    @Composable
    override fun Content() {
        PlaylistAddToScreen(mediaIds = ids)
    }
}

@Composable
private fun DynamicScreen.PlaylistAddToScreen(
    mediaIds: List<String>,
    sp: PlaylistSp = koinInject(),
) {
    val navigator: GlobalNavigator = koinInject()
    val selector = rememberItemSelectHelper()
    val playlists = sp.obtainList<LPlaylist>("Playlist", autoSave = false)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                text = "添加到歌单 [${mediaIds.size}] -> [${selector.selected.value.size}]"
            )

            val contentColor = remember { Color(0xFFFFFFFF) }
            val bgColor = remember { contentColor.copy(0.15f) }

            TextButton(
                modifier = Modifier.fillMaxHeight(),
                shape = RectangleShape,
                contentPadding = PaddingValues(horizontal = 20.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = bgColor,
                    contentColor = contentColor
                ),
                onClick = {

                }
            ) {
                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = componentR.drawable.ic_check_line),
                    contentDescription = "confirm",
                    colorFilter = contentColor.toColorFilter()
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "确认",
                    fontSize = 14.sp
                )
            }
        }

        LLazyColumn(modifier = Modifier.fillMaxHeight(0.4f)) {
            items(
                items = playlists.value,
                key = { it.id },
                contentType = { LPlaylist::class.java }
            ) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    isSelected = { selector.isSelected(playlist) },
                    onClick = { selector.onSelect(playlist) }
                )
            }
        }
    }
}