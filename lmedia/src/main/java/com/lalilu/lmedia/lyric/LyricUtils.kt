package com.lalilu.lmedia.lyric

import com.lalilu.lmedia.lyric.parser.LrcParser
import com.lalilu.lmedia.lyric.parser.TtmlParser


object LyricUtils {

    /**
     * 从文本解析双语歌词
     */
    fun parseLrc(vararg lrcTexts: String?): List<LyricItem>? {
        val mainLrcText = lrcTexts.getOrNull(0)
            ?: return null

        var result: List<LyricItem>? = null

        if (mainLrcText.contains("xmlns:amll")) {
            result = TtmlParser.parse(mainLrcText)
        }

        if (result.isNullOrEmpty()) {
            result = LrcParser.parse(mainLrcText)
        }

        return addStartingTips(result)
    }

    fun addStartingTips(list: List<LyricItem>): MutableList<LyricItem> {
        val newList = mutableListOf<LyricItem>()

        for (index in list.indices) {
            val previous = if (index > 0) list[index - 1] else null
            val current = list[index]

            val startTime = previous?.endTime() ?: 0L
            val endTime = current.time

            // 当时间间隔大于7秒时，添加一个开始提示
            if (endTime - startTime >= 7000) {
                newList.add(
                    LyricItem.StartTips(
                        focusTime = startTime,
                        startTime = endTime - 3000,
                        endTime = endTime,
                        key = "pre_${current.key}"
                    )
                )
            }

            newList.add(current)
        }

        return newList
    }

    private fun LyricItem.endTime(): Long {
        return when (this) {
            is LyricItem.NormalLyric -> time + 7000L
            is LyricItem.StartTips -> endTime
            is LyricItem.WordsLyric -> endTime
        }
    }
}