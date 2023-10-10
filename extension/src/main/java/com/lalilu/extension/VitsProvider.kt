package com.lalilu.extension

import android.net.Uri
import com.lalilu.common.base.Playable
import com.lalilu.extension_core.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
object VitsProvider : Provider {
    val sentence = MutableStateFlow(
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

    override fun isSupported(mediaId: String): Boolean {
        return mediaId.startsWith("vits_")
    }

    override fun getPlayableByMediaId(mediaId: String): Playable? {
        return sentence.value.firstOrNull { it.mediaId == mediaId }
    }

    override fun getPlayableFlowByMediaId(mediaId: String): Flow<Playable?> {
        return sentence.mapLatest { list -> list.firstOrNull { it.mediaId == mediaId } }
    }

    override fun getPlayableFlowByMediaIds(mediaIds: List<String>): Flow<List<Playable>> {
        return sentence.mapLatest { list -> list.filter { it.mediaId in mediaIds } }
    }
}