package com.lalilu.extension

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
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

    private val sentences = MutableStateFlow(
        listOf(
            VitsSentence(
                mediaId = "vits_1",
                title = "影娱安利",
                subTitle = "utf-8",
                imageSource = "https://api.sretna.cn/layout/pc.php",
                targetUri = Uri.parse("http://192.168.1.159:23456/voice/bert-vits2?text=影娱安利不停歇！投稿得20万！ 本视频参加过 [ 星动安利大作战·春日篇 ] 活动，该活动已结束~&id=0&format=wav&length=1.2&noisew=0.9")
            ),
            VitsSentence(
                mediaId = "vits_2",
                title = "无限",
                subTitle = "无限，21年的电影，这都23年了还今年最震撼",
                targetUri = Uri.parse("http://192.168.1.159:23456/voice/bert-vits2?text=无限，21年的电影，这都23年了还今年最震撼&id=0&length=1.2&noisew=0.9")
            ),
            VitsSentence(
                mediaId = "vits_3",
                title = "好莱坞",
                subTitle = "现在好莱坞动作大片质量确实不行了，以前老片子故事  演技  场面都是顶级，现在就剩场面了",
                targetUri = Uri.parse("http://192.168.1.159:23456/voice/bert-vits2?text=现在好莱坞动作大片质量确实不行了，以前老片子故事  演技  场面都是顶级，现在就剩场面了&id=0&length=1.2&noisew=0.9")
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
            remember { "https://api.sretna.cn/layout/pc.php?seed=${System.currentTimeMillis() / 30000}" }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
                model = imageApi,
                contentDescription = ""
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Text(text = stringResource(id = R.string.plugin_name) + " " + BuildConfig.VERSION_NAME)
            }
        }
    }
}