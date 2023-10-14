package com.lalilu.extension

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lalilu.common.base.Playable
import com.lalilu.extension_core.Ext
import com.lalilu.extension_core.Extension
import com.lalilu.extension_core.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Ext
class Main : Extension, Provider {
    private val baseUrl = "https://frp-gas.top:55244/voice/bert-vits2"
    private val baseParams = mapOf(
        "id" to "0",
        "format" to "wav",
        "length" to "1.2",
        "noisew" to "0.9"
    )

    private fun getUrlWithText(text: String): String {
        val list = baseParams.toList() + ("text" to text)
        return "$baseUrl?${list.joinToString(separator = "&") { "${it.first}=${it.second}" }}"
    }

    private val sentences = MutableStateFlow(
        listOf(
            VitsSentence(
                mediaId = "vits_1",
                title = "稻香",
                subTitle = "周杰伦",
                imageSource = "https://api.sretna.cn/layout/pc.php",
                targetUri = Uri.parse(getUrlWithText(Constants.lyric1))
            ),
            VitsSentence(
                mediaId = "vits_2",
                title = "星晴",
                subTitle = "周杰伦",
                targetUri = Uri.parse(getUrlWithText(Constants.lyric2))
            ),
            VitsSentence(
                mediaId = "vits_3",
                title = "再别康桥",
                subTitle = "徐志摩",
                targetUri = Uri.parse(getUrlWithText(Constants.lyric3))
            )
        )
    )

    override fun getContentMap(): Map<String, @Composable () -> Unit> {
        return mapOf(
            "home" to this.bannerContent,
            "main" to { MainScreen(sentences) },
            "banner" to this.bannerContent,
        )
    }

    override fun getProvider(): Provider = this

    override fun isSupported(mediaId: String): Boolean {
        return mediaId.startsWith("vits_")
    }

    override fun getById(mediaId: String): Playable? {
        return sentences.value.firstOrNull { it.mediaId == mediaId }
    }

    override fun getFlowById(mediaId: String): Flow<Playable?> {
        return sentences.mapLatest { list -> list.firstOrNull { it.mediaId == mediaId } }
    }

    private val bannerContent: @Composable () -> Unit = {
        val imageApi =
            remember { mutableStateOf("https://api.sretna.cn/layout/pc.php?seed=${System.currentTimeMillis() / 30000}") }
        val showBar = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    model = imageApi.value,
                    contentDescription = ""
                )
                IconButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onClick = { showBar.value = !showBar.value }
                ) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, "")
                }
            }

            AnimatedVisibility(visible = showBar.value) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    IconButton(onClick = { }) {
                        Text(text = "#${BuildConfig.VERSION_NAME}")
                    }
                    IconButton(
                        onClick = {
                            imageApi.value =
                                "https://api.sretna.cn/layout/pc.php?seed=${System.currentTimeMillis()}"
                        }
                    ) {
                        Text(text = "CHANGE")
                    }
                }
            }
        }
    }
}