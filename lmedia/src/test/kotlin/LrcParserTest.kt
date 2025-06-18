import com.lalilu.lmedia.lyric.parser.LrcParser
import org.junit.Test

class LrcParserTest {

    @Test
    fun testParser() {
        val result = LrcParser.timeTagToTime("[00:29.605]")
        assert(result == 29605L)
        println(result)

        val lyricLine =
            "[00:00.000]ア[00:00.364]ン[00:00.728][00:00.729]ダ[00:01.093]ー[00:01.457]ブ[00:01.821][00:01.822]ー[00:02.186]ケ[00:02.550][00:02.551] - [00:02.915]明[00:03.279]透[00:03.643]"
        var findResult = Regex("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]")
            .findAll(lyricLine)
            .toList()

        val textSplits = mutableListOf<String>()
        for (i in findResult.indices) {
            val item = findResult[i]
            textSplits.add(item.value)

            val endIndex = findResult.getOrNull(i + 1)?.range?.first ?: lyricLine.lastIndex
            val startIndex = item.range.last + 1

            if (startIndex <= endIndex) {
                val text = lyricLine.substring(startIndex, endIndex)
                if (text.isNotEmpty()) {
                    textSplits.add(text)
                }
            }
        }

        textSplits.forEach { println("\"${it}\"") }
    }
}