package com.lalilu.lmedia.lyric.parser

import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.LyricItem.WordsLyric.Translation
import com.lalilu.lmedia.lyric.LyricParser
import com.lalilu.lmedia.lyric.getSentenceContent

/**
 * 解析Lrc歌词
 */
object LrcParser : LyricParser {
    private val REGEX_TIME = Regex("\\[(\\d\\d):(\\d\\d)\\.(\\d{1,5})]")
    private val REGEX_TIME_EX = Regex("<(\\d\\d):(\\d\\d)\\.(\\d{1,5})>")

    override fun parse(lyric: String): List<LyricItem> {
        if (lyric.isBlank()) return emptyList()

        // 首先将所有句子按单句进行解析
        val mainEntryList = parseLrc(lyric)
            ?.takeIf(Collection<*>::isNotEmpty)
            ?: return emptyList()

        // 合并相同时间的单句，其中第一个单句作为主句子，第二个单句作为翻译
        return mainEntryList.groupBy { it.time }
            .toSortedMap()
            .mapValues { (time, list) ->
                val first = list.getOrNull(0) ?: return@mapValues null
                val second = list.getOrNull(1) ?: return@mapValues first
                val translationText = when (second) {
                    is LyricItem.WordsLyric -> second.getSentenceContent()
                    is LyricItem.NormalLyric -> second.content
                    else -> return return@mapValues null
                }

                when (first) {
                    is LyricItem.WordsLyric -> first.copy(
                        translation = listOf(
                            Translation(
                                translationText,
                                "unknown"
                            )
                        )
                    )

                    is LyricItem.NormalLyric -> first.copy(
                        translation = translationText
                    )

                    else -> return@mapValues null
                }
            }.values.mapIndexedNotNull { index, item ->
                item ?: return@mapIndexedNotNull null

                return@mapIndexedNotNull when (item) {
                    is LyricItem.WordsLyric -> item.copy(key = "$index${item.key}")
                    is LyricItem.NormalLyric -> item.copy(key = "$index${item.key}")
                    else -> null
                }
            }
    }

    /**
     * 从文本解析歌词
     */
    private fun parseLrc(lrcText: String): List<LyricItem>? {
        var lyricText = lrcText.trim()
        if (lyricText.isEmpty()) return null

        if (lyricText.startsWith("\uFEFF")) {
            lyricText = lyricText.replace("\uFEFF", "")
        }

        // 针对传入 Language="Media Monkey Format"; Lyrics="......"; 的情况
        lyricText = lyricText.substringAfter("Lyrics=\"")
            .substringBeforeLast("\";")

        return lyricText
            .split("\n")
            .toTypedArray()
            .mapNotNull { parseLine(it)?.takeIf(Collection<*>::isNotEmpty) }
            .flatten()
            .sorted()
    }

    /**
     * 解析一行歌词
     */
    private fun parseLine(line: String): List<LyricItem>? {
        var lyricLine = line
        if (lyricLine.isEmpty()) return null

        lyricLine = lyricLine.trim { it <= ' ' }

        // [00:17.65]让我掉下眼泪的
        // [00:17.65]让我掉下眼泪的[00:19.66]
        var findResult = REGEX_TIME
            .findAll(lyricLine)
            .toList()

        // 当歌词中有一个[00:00.000]类型的时间标签时尝试匹配<00:00.000>格式的时间标签
        if (findResult.size == 1) {
            val temp = REGEX_TIME_EX
                .findAll(lyricLine)
                .toList()

            // 当存在<00:00.000>格式的时间标签时，则使用<00:00.000>格式的时间标签
            if (temp.isNotEmpty()) {
                findResult = temp
            }
        }

        // 若没有时间标签，则返回 null
        if (findResult.isEmpty()) {
            return null
        }

        // 拆分歌词和时间标签
        val textSplits = mutableListOf<LrcContentItem>()
        for (i in findResult.indices) {
            val item = findResult[i]
            textSplits.add(LrcContentItem.TimeTag(timeTagToTime(item.value)))

            val endIndex = findResult.getOrNull(i + 1)?.range?.first ?: (lyricLine.lastIndex + 1)
            val startIndex = item.range.last + 1

            if (startIndex <= endIndex) {
                val text = lyricLine.substring(startIndex, endIndex)
                if (text.isNotEmpty()) {
                    textSplits.add(LrcContentItem.Text(text))
                }
            }
        }

        // 为歌词单词文本添加开始时间和结束时间
        val words = textSplits.mapIndexedNotNull { index, item ->
            if (item is LrcContentItem.TimeTag) return@mapIndexedNotNull null
            val text = item as? LrcContentItem.Text ?: return@mapIndexedNotNull null

            val startTime = (textSplits.getOrNull(index - 1) as? LrcContentItem.TimeTag)
                ?.time ?: return@mapIndexedNotNull null
            val endTime = (textSplits.getOrNull(index + 1) as? LrcContentItem.TimeTag)
                ?.time ?: startTime

            LyricItem.WordsLyric.WordWithTiming(
                content = text.text,
                startTime = startTime,
                endTime = endTime
            )
        }

        // 若无结果则尽早返回
        if (words.isEmpty()) return emptyList()
        val firstWord = words[0]
        val lastWord = words.last()

        // 若只有一个词/句，且其开始时间等于其结束时间，则认为其就是一个普通句子
        if (words.size == 1 && firstWord.startTime == firstWord.endTime) {
            return listOf(
                LyricItem.NormalLyric(
                    content = firstWord.content,
                    time = firstWord.startTime,
                    key = "${firstWord.startTime}"
                )
            )
        }

        // 否则将其输出为逐字歌词对象
        return listOf(
            LyricItem.WordsLyric(
                words = words,
                translation = emptyList(),
                startTime = firstWord.startTime,
                endTime = lastWord.endTime,
                key = "${firstWord.startTime}"
            )
        )
    }

    /**
     * 负责解析并转换`[00:00.000]`和`<00:00.000>`格式的时间标签
     */
    fun timeTagToTime(str: String): Long {
        val timeMatcher =
            // 匹配[00:00.00]格式的时间标签
            REGEX_TIME.matchEntire(str)
                ?.groupValues
                ?.takeIf { it.isNotEmpty() }
            // 尝试匹配<00:00.00>格式的时间标签
                ?: REGEX_TIME_EX.matchEntire(str)
                    ?.groupValues
                    ?.takeIf { it.isNotEmpty() }
                ?: return -1L

        val min = timeMatcher.getOrNull(1)!!.toLong()
        val sec = timeMatcher.getOrNull(2)!!.toLong()
        val milString = timeMatcher.getOrNull(3)!!

        var mil = milString.toLong()
        // 如果毫秒是两位数，需要乘以 10，when 新增支持 1 - 6 位毫秒，很多获取的歌词存在不同的毫秒位数
        when (milString.length) {
            1 -> mil *= 100
            2 -> mil *= 10
            4 -> mil /= 10
            5 -> mil /= 100
            6 -> mil /= 1000
        }

        return min * 60 * 1000 + sec * 1000 + mil
    }

    private sealed interface LrcContentItem {
        data class TimeTag(val time: Long) : LrcContentItem
        data class Text(val text: String) : LrcContentItem
    }
}