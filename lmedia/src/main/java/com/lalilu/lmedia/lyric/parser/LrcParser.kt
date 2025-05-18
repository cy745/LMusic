package com.lalilu.lmedia.lyric.parser

import android.text.format.DateUtils
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.LyricParser
import java.util.regex.Pattern

/**
 * 解析Lrc歌词
 */
object LrcParser : LyricParser {
    private val PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}])+)(.+)")
    private val PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]")

    override fun parse(lyric: String): List<LyricItem> {
        if (lyric.isBlank()) return emptyList()

        // 首先将所有句子按单句进行解析
        val mainEntryList = parseLrc(lyric)
            ?.mapNotNull { it as? LyricItem.NormalLyric }
            ?.takeIf(Collection<*>::isNotEmpty)
            ?: return emptyList()

        // 合并相同时间的单句，其中第一个单句作为主句子，第二个单句作为翻译
        return mainEntryList.groupBy { it.time }
            .toSortedMap()
            .mapValues { (time, list) ->
                val first = list.getOrNull(0) ?: return@mapValues null
                val second = list.getOrNull(1) ?: return@mapValues first

                first.copy(translation = second.content)
            }.values.mapIndexedNotNull { index, item ->
                item?.copy(key = "$index${item.key}")
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
        val lineMatcher = PATTERN_LINE.matcher(lyricLine)
        if (!lineMatcher.matches()) {
            return null
        }

        val times = lineMatcher.group(1)!!
        val text = lineMatcher.group(3)!!
        val entryList: MutableList<LyricItem> = ArrayList()

        // [00:17.65]
        val timeMatcher = PATTERN_TIME.matcher(times)
        while (timeMatcher.find()) {
            val min = timeMatcher.group(1)!!.toLong()
            val sec = timeMatcher.group(2)!!.toLong()
            val milString = timeMatcher.group(3)!!
            var mil = milString.toLong()
            // 如果毫秒是两位数，需要乘以 10，when 新增支持 1 - 6 位毫秒，很多获取的歌词存在不同的毫秒位数
            when (milString.length) {
                1 -> mil *= 100
                2 -> mil *= 10
                4 -> mil /= 10
                5 -> mil /= 100
                6 -> mil /= 1000
            }
            val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
            entryList.add(
                LyricItem.NormalLyric(
                    content = text,
                    time = time,
                    key = "$time"
                )
            )
        }
        return entryList
    }
}