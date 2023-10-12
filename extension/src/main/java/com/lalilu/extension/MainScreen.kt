package com.lalilu.extension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.flow.Flow

@Composable
fun MainScreen(
    sentences: Flow<List<VitsSentence>>,
) {
    val imageApi =
        remember { "https://api.sretna.cn/layout/pc.php?seed=${System.currentTimeMillis() / 30000}" }
    val sentence by sentences.collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
                model = imageApi,
                contentDescription = ""
            )
        }
        items(items = sentence) {
            Surface {
                Column {
                    Text(text = it.title, style = MaterialTheme.typography.subtitle1)
                    Text(text = it.subTitle, style = MaterialTheme.typography.subtitle2)
                    Text(text = it.targetUri.toString(), style = MaterialTheme.typography.body2)
                    TextButton(onClick = {
                        LPlayer.runtime.queue.setCurrentId(it.mediaId)
                        LPlayer.runtime.queue.setIds(sentence.map { it.mediaId })
                        PlayerAction.PlayById(it.mediaId).action()
                    }) {
                        Text(text = "播放")
                    }
                }
            }
        }
    }
}