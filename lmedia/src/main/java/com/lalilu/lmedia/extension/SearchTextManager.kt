package com.lalilu.lmedia.extension

import android.content.Context
import com.cm55.kanhira.KakasiDictReader
import com.cm55.kanhira.Kanhira
import com.lalilu.lmedia.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.CoroutineContext

/**
 * 用于实现搜索过滤功能的工具类
 */
object SearchTextManager {

    interface TextTransformer {
        fun containTargetText(str: String): Boolean
        fun transform(str: String): String?
    }

    fun createPatternString(source: String): String {
        var resultStr = source
        ChineseToPinyinTransformer.transform(source)?.let {
            resultStr += " $it"
        }
        var temp = source
        KanjiToHiraTransformer.transform(source)?.let {
            resultStr += " $it"
            temp = it
        }
        HiraToRomajiTransformer.transform(temp)?.let {
            resultStr += " $it"
        }
        return resultStr.uppercase(Locale.getDefault())
    }
}

object ChineseToPinyinTransformer : SearchTextManager.TextTransformer {
    override fun containTargetText(str: String): Boolean {
        return str.any { it in '\u4e00'..'\u9fa5' || it in '\u30a0'..'\u30ff' }
    }

    override fun transform(str: String): String? {
        if (!containTargetText(str)) return null
        val result = PinyinUtils.ccs2Pinyin(str)
        if (result == str) return null
        return result
    }
}

object HiraToRomajiTransformer : SearchTextManager.TextTransformer {
    private val kanaToRomaji = KanaToRomaji()

    override fun containTargetText(str: String): Boolean {
        return str.any { it in '\u3040'..'\u309f' || it in '\u30a0'..'\u30ff' }
    }

    override fun transform(str: String): String? {
        if (!containTargetText(str)) return null
        val result = kanaToRomaji.convert(str)
        if (result == str) return null
        return result
    }
}

object KanjiToHiraTransformer : CoroutineScope, SearchTextManager.TextTransformer {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var mKanhira: Kanhira? = null

    fun init(context: Context) = launch {
        mKanhira = Kanhira(
            KakasiDictReader.load(
                context.resources.openRawResource(R.raw.kakasidict_utf_8),
                Charsets.UTF_8.name()
            )
        )
    }

    override fun containTargetText(str: String): Boolean {
        return str.any { it in '\u4e00'..'\u9fa5' || it in '\u30a0'..'\u30ff' }
    }

    override fun transform(str: String): String? {
        if (!containTargetText(str) || mKanhira == null) return null
        val result = mKanhira?.convert(str) ?: return null
        if (result == str) return null
        return result
    }
}