package com.lalilu.lmedia.lyric.parser

import android.text.format.DateUtils
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.LyricParser
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

object TtmlParser : LyricParser {
    private val REGEX_TIME = Regex("(?:(\\d+):)?(\\d+)\\.(\\d{3})")

    private val xml = XML {
        autoPolymorphic = true
        fast_0_90_2()
    }

    override fun parse(lyric: String): List<LyricItem> {
        if (lyric.isBlank()) return emptyList()
        var actualLyric = lyric

        val randomKeyPrefix = System.currentTimeMillis()
        val ttml = runCatching { xml.decodeFromString<TTML>(actualLyric) }.getOrNull()
            ?: return emptyList()
        val duration = parseTime(ttml.body.dur)

        return ttml.body.div.p.map { sentence ->
            val sentenceStart = parseTime(sentence.begin)
            val sentenceEnd = parseTime(sentence.end)

            val translations = sentence.spans.filter { it.isTranslation() }.mapNotNull {
                if (it.content.isBlank()) return@mapNotNull null

                LyricItem.WordsLyric.Translation(
                    content = it.content,
                    lang = it.lang ?: "unknown"
                )
            }

            val words = sentence.spans.filter { !it.isTranslation() }.map { word ->
                LyricItem.WordsLyric.WordWithTiming(
                    startTime = parseTime(word.begin),
                    endTime = parseTime(word.end),
                    content = word.content
                )
            }

            val fixedWords = words.mapIndexed { index, word ->
                if (word.startTime == word.endTime && word.startTime == 0L) {
                    return@mapIndexed word.copy(
                        startTime = words.getOrNull(index - 1)?.endTime ?: sentenceStart,
                        endTime = words.getOrNull(index + 1)?.startTime ?: sentenceEnd
                    )
                }
                word
            }

            LyricItem.WordsLyric(
                key = "${randomKeyPrefix}_${sentence.key}",
                agent = sentence.agent,
                startTime = sentenceStart,
                endTime = sentenceEnd,
                translation = translations,
                words = fixedWords
            )
        }.sorted()
    }

    private fun parseTime(time: String?): Long {
        if (time.isNullOrBlank()) return 0

        val matcher = REGEX_TIME.matchEntire(time)
        if (matcher == null) return 0

        val minute = matcher.groups[1]?.value?.toLongOrNull() ?: 0L
        val second = matcher.groups[2]?.value?.toLongOrNull() ?: 0L
        val milString = matcher.groups[3]?.value ?: "0"
        var mil = milString.toLongOrNull() ?: 0L
        when (milString.length) {
            1 -> mil *= 100
            2 -> mil *= 10
            4 -> mil /= 10
            5 -> mil /= 100
            6 -> mil /= 1000
        }

        return minute * DateUtils.MINUTE_IN_MILLIS + second * DateUtils.SECOND_IN_MILLIS + mil
    }
}