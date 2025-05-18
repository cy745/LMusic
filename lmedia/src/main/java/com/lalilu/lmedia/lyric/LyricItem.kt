package com.lalilu.lmedia.lyric

sealed class LyricItem(
    open val time: Long = 0,
    open val key: String = "",
) : Comparable<LyricItem> {

    override fun compareTo(other: LyricItem): Int {
        return time.compareTo(other.time)
    }

    data class NormalLyric(
        val content: String,
        val translation: String? = null,
        override val time: Long,
        override val key: String
    ) : LyricItem()

    data class WordsLyric(
        val agent: String = "",
        val words: List<WordWithTiming>,
        val translation: List<Translation>,
        val startTime: Long,
        val endTime: Long,
        override val key: String,
    ) : LyricItem(time = startTime) {

        data class Translation(
            val content: String,
            val lang: String
        )

        data class WordWithTiming(
            val content: String,
            val startTime: Long,
            val endTime: Long,
        ) : Comparable<WordWithTiming> {
            override fun compareTo(other: WordWithTiming): Int {
                return startTime.compareTo(other.startTime)
            }
        }
    }
}

fun List<LyricItem>.findPlayingIndex(time: Long): Int {
    if (isEmpty()) return Int.MAX_VALUE

    var low = 0
    var high = size - 1
    var result = Int.MAX_VALUE

    while (low <= high) {
        val mid = (low + high) ushr 1
        val midVal = get(mid).time

        when {
            midVal < time -> {
                // 记录最后一个小于目标时间的索引
                result = mid
                low = mid + 1
            }

            midVal > time -> {
                high = mid - 1
            }

            else -> return mid // 找到精确匹配
        }
    }

    // 处理边界情况：
    return when {
        // 所有元素的时间都大于目标时间
        result == Int.MAX_VALUE -> Int.MAX_VALUE

        // 检查找到的索引是否有效（下一个元素时间是否超过当前时间）
        result == lastIndex || get(result + 1).time > time -> result

        // 理论上不会到达这里
        else -> Int.MAX_VALUE
    }
}

fun List<LyricItem.WordsLyric.WordWithTiming>.findPlayingIndexForWords(time: Long): Int {
    var left = 0
    var right = size - 1

    while (left <= right) {
        val mid = left + (right - left) / 2
        val midItem = this[mid]

        if (midItem.startTime <= time && midItem.endTime >= time) {
            return mid
        } else if (midItem.endTime < time) {
            left = mid + 1
        } else {
            right = mid - 1
        }
    }

    return Int.MAX_VALUE
}

fun List<LyricItem>.findPlayingItem(time: Long): LyricItem? {
    return this.getOrNull(findPlayingIndex(time))
}

fun LyricItem.WordsLyric.getSentenceContent(): String {
    return words.joinToString(separator = "") { it.content }
}

fun LyricItem.toNormal(): LyricItem.NormalLyric? {
    if (this is LyricItem.NormalLyric) return this
    if (this is LyricItem.WordsLyric) apply {
        val translation = translation.firstOrNull { it.content.isNotBlank() }?.content
        val sentence = getSentenceContent()
            .takeIf { it.isNotBlank() }
            ?: return null

        return LyricItem.NormalLyric(
            content = sentence,
            translation = translation,
            time = this.time,
            key = this.key
        )
    }

    return null
}
