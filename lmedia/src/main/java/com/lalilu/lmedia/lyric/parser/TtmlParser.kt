package com.lalilu.lmedia.lyric.parser

import android.text.format.DateUtils
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.LyricParser
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.serialization.XML

object TtmlParser : LyricParser {
    private val REGEX_TIME = Regex("(?:(\\d+):)?(\\d+)\\.(\\d{3})")
    private val PATTERN_SPACE_IN_LINE = Regex("""</span>(\s+)<span""")

    @OptIn(ExperimentalXmlUtilApi::class)
    private val xml = XML(
        serializersModule = SerializersModule {
            polymorphic(Any::class) {
                defaultDeserializer { String.serializer() }
                subclass(String::class)
                subclass(Element::class)
            }
        }
    ) {
        defaultPolicy {
            autoPolymorphic = true
            ignoreUnknownChildren()
            fast_0_90_2 { }
        }
    }

    override fun parse(lyric: String): List<LyricItem> {
        if (lyric.isBlank()) return emptyList()
        var actualLyric = lyric

        // 匹配span元素间（词与词之间）的空格
        actualLyric = PATTERN_SPACE_IN_LINE.replace(actualLyric) { result ->
            val group = result.groups[1] ?: return@replace result.value

            result.value.replace(
                oldValue = group.value,
                newValue = """<span begin="00:00.000" end="00:00.000">${group.value}</span>"""
            )
        }

        val randomKeyPrefix = System.currentTimeMillis()
        val ttml = runCatching { xml.decodeFromString<TTML>(actualLyric) }.getOrNull()
            ?: return emptyList()

        val divs = ttml.body.div.firstOrNull()
            ?: return emptyList()

        return divs.p.map { sentence ->
            val sentenceStart = parseTime(sentence.begin)
            val sentenceEnd = parseTime(sentence.end)

            val translations = sentence.span.filter { it.isTranslation() }.mapNotNull {
                val content = it.content()
                if (content.isNullOrBlank()) return@mapNotNull null

                LyricItem.WordsLyric.Translation(
                    content = content,
                    lang = it.lang ?: "unknown"
                )
            }

            val words = sentence.span.filter { !it.isTranslation() }.mapNotNull { word ->
                val content = word.content()
                if (content.isNullOrEmpty()) return@mapNotNull null

                LyricItem.WordsLyric.WordWithTiming(
                    startTime = parseTime(word.begin),
                    endTime = parseTime(word.end),
                    content = content
                )
            }

            val xBgWords = sentence.span.filter { it.role == "x-bg" }
                .mapNotNull { it.children() }
                .filter { it.isNotEmpty() }
                .mapIndexed { index, spans ->
                    val words = spans.mapNotNull { word ->
                        val content = word.content()
                        if (content.isNullOrEmpty()) return@mapNotNull null

                        LyricItem.WordsLyric.WordWithTiming(
                            startTime = parseTime(word.begin),
                            endTime = parseTime(word.end),
                            content = content
                        )
                    }
                    val start = words
                        .filter { it.startTime > 0 }
                        .minOf { it.startTime }
                    val end = words.maxOf { it.endTime }

                    LyricItem.WordsLyric(
                        key = "${randomKeyPrefix}_${sentence.key}_xbg_$index",
                        agent = sentence.agent ?: "",
                        startTime = start,
                        endTime = end,
                        translation = emptyList(),
                        words = fixedWordsTime(start, end, words)
                    )
                }

            listOf(
                LyricItem.WordsLyric(
                    key = "${randomKeyPrefix}_${sentence.key}",
                    agent = sentence.agent ?: "",
                    startTime = sentenceStart,
                    endTime = sentenceEnd,
                    translation = translations,
                    words = fixedWordsTime(sentenceStart, sentenceEnd, words)
                )
            ) + xBgWords
        }.flatten().sorted()
    }

    private fun fixedWordsTime(
        sentenceStart: Long,
        sentenceEnd: Long,
        words: List<LyricItem.WordsLyric.WordWithTiming>
    ): List<LyricItem.WordsLyric.WordWithTiming> {
        return words.mapIndexed { index, word ->
            if (word.startTime == word.endTime && word.startTime == 0L) {
                return@mapIndexed word.copy(
                    startTime = words.getOrNull(index - 1)?.endTime ?: sentenceStart,
                    endTime = words.getOrNull(index + 1)?.startTime ?: sentenceEnd
                )
            }
            word
        }
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