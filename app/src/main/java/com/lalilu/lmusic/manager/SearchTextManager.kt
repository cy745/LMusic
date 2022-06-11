package com.lalilu.lmusic.manager

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.cm55.kanhira.KakasiDictReader
import com.cm55.kanhira.Kanhira
import com.lalilu.R
import com.lalilu.common.KanaToRomaji
import com.lalilu.common.PinyinUtils
import com.lalilu.lmusic.Config
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * 用于实现搜索过滤功能的工具类
 */
@Singleton
class SearchTextManager @Inject constructor(
    private val kanjiToHiraTransformer: KanjiToHiraTransformer
) {
    private val chineseToPinyinTransformer = ChineseToPinyinTransformer()
    private val hiraToRomajiTransformer = HiraToRomajiTransformer()

    interface TextTransformer {
        fun containTargetText(str: String): Boolean
        fun transform(str: String): String?
    }

    /**
     * 关键词匹配
     *
     * @param keyword 关键词
     * @param list 所需要进行查询匹配的一系列item
     * @param getString 从item中获取所需要进行查询的原始字符串
     */
    fun <Item> filter(
        keyword: String?,
        list: List<Item>,
        getString: (Item) -> String
    ): List<Item> {
        if (keyword == null || TextUtils.isEmpty(keyword)) return list
        val keywords = keyword.split(" ")

        return list.filter { item ->
            val originStr = getString(item)
            var resultStr = originStr

            chineseToPinyinTransformer.transform(originStr)?.let {
                resultStr += " $it"
            }
            var temp = originStr
            kanjiToHiraTransformer.transform(originStr)?.let {
                resultStr += " $it"
                temp = it
            }
            hiraToRomajiTransformer.transform(temp)?.let {
                resultStr += " $it"
            }
            checkKeywords(resultStr, keywords)
        }
    }

    /**
     * 检查字符串[str]与[keywords]的每一个item有没有相似的部分
     */
    private fun checkKeywords(str: CharSequence?, keywords: List<String>): Boolean {
        keywords.forEach { keyword ->
            if (!checkKeyword(str, keyword)) return false
        }
        return true
    }

    /**
     * 检查字符串[str]与[keyword]有没有相似的部分
     */
    private fun checkKeyword(str: CharSequence?, keyword: String): Boolean {
        str ?: return false
        return str.toString().uppercase(Locale.getDefault()).contains(
            keyword.uppercase(Locale.getDefault())
        )
    }
}

class ChineseToPinyinTransformer : SearchTextManager.TextTransformer {
    override fun containTargetText(str: String): Boolean {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find()
    }

    override fun transform(str: String): String? {
        if (!containTargetText(str)) return null
        val result = PinyinUtils.ccs2Pinyin(str)
        if (result == str) return null
        return result
    }
}

class HiraToRomajiTransformer : SearchTextManager.TextTransformer {
    private val kanaToRomaji = KanaToRomaji()

    override fun containTargetText(str: String): Boolean {
        return Pattern.compile("[\u3040-\u309f]").matcher(str).find() ||
                Pattern.compile("[\u30a0-\u30ff]").matcher(str).find()
    }

    override fun transform(str: String): String? {
        if (!containTargetText(str)) return null
        val result = kanaToRomaji.convert(str)
        if (result == str) return null
        return result
    }
}


@Singleton
class KanjiToHiraTransformer @Inject constructor(
    @ApplicationContext context: Context
) : CoroutineScope, SearchTextManager.TextTransformer {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var mKanhira: Kanhira? = null

    override fun containTargetText(str: String): Boolean {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find()
    }

    override fun transform(str: String): String? {
        if (!containTargetText(str) || mKanhira == null) return null
        val result = mKanhira?.convert(str)
        if (result == str) return null
        return result
    }

    init {
        if (Build.VERSION.SDK_INT > 23) {
            SpManager.listen(Config.KEY_SETTINGS_KANHIRA_ENABLE,
                SpManager.SpBoolListener(Config.DEFAULT_SETTINGS_KANHIRA_ENABLE) {
                    if (!it) {
                        mKanhira = null
                        return@SpBoolListener
                    }

                    launch {
                        mKanhira = Kanhira(
                            KakasiDictReader.load(
                                context.resources.openRawResource(R.raw.kakasidict_utf_8),
                                Charsets.UTF_8.name()
                            )
                        )
                    }
                })
        }
    }
}