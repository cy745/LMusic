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

        return result
    }
}