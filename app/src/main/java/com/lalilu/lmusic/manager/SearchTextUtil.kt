package com.lalilu.lmusic.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import com.cm55.kanhira.KakasiDictReader
import com.cm55.kanhira.Kanhira
import com.lalilu.R
import com.lalilu.common.KanaToRomaji
import com.lalilu.common.PinyinUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

/**
 * 关键词匹配
 *
 * @param keyword 关键词
 * @param list 所需要进行查询匹配的一系列item
 * @param getString 从item中获取所需要进行查询的原始字符串
 */
inline fun <I> SearchTextUtil.filter(
    keyword: String?,
    list: List<I>,
    getString: (I) -> String
): List<I> {
    if (keyword == null || TextUtils.isEmpty(keyword)) return list
    val keywords = keyword.split(" ")
    val kanhiraEnable = isKanhiraEnable()

    return list.filter {
        val originStr = getString(it)
        var resultStr = originStr
        val isContainChinese = isContainChinese(originStr)
        val isContainKatakanaOrHinagana = isContainKatakanaOrHinagana(originStr)
        if (isContainChinese || isContainKatakanaOrHinagana) {
            if (isContainChinese) {
                val chinese = toHanYuPinyinString(originStr)
                resultStr += " $chinese"
            }

            if (kanhiraEnable) {
                val japanese = toHiraString(originStr)
                val romaji = toRomajiString(japanese)
                resultStr += " $romaji"
            }
        }
        checkKeywords(resultStr, keywords)
    }
}

/**
 * 用于实现搜索功能的工具类
 *
 * 包含功能：汉字转拼音、汉字转假名、假名转罗马字
 *         字符串汉字检测，字符串匹配
 */
object SearchTextUtil : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    /**
     * 异步加载Kanhira组件
     */
    private val mKanhira = MutableStateFlow<Kanhira?>(null)
    private val kanaToRomaji = KanaToRomaji()
    private var sharedPreferences: SharedPreferences? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val isKanhiraInitialed = mKanhira.mapLatest { it != null }

    fun initKanhira(context: Context) = launch(Dispatchers.IO) {
        sharedPreferences = context.getSharedPreferences(
            context.applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        if (Build.VERSION.SDK_INT <= 23 || !isKanhiraEnable()) return@launch
        mKanhira.emit(
            Kanhira(
                KakasiDictReader.load(
                    context.resources.openRawResource(R.raw.kakasidict_utf_8),
                    Charsets.UTF_8.name()
                )
            )
        )
    }

    fun isKanhiraEnable(): Boolean {
        return sharedPreferences?.getBoolean("KEY_SETTINGS_kanhira_enable", false) == true
    }

    /**
     * 检查字符串[str]与[keywords]的每一个item有没有相似的部分
     */
    fun checkKeywords(str: CharSequence?, keywords: List<String>): Boolean {
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

    /**
     * 将汉字转为拼音
     */
    fun toHanYuPinyinString(text: String): String? {
        return PinyinUtils.ccs2Pinyin(text)
    }

    /**
     * 将日文汉字转为平假名或片假名
     */
    fun toHiraString(text: String): String {
        return runBlocking {
            val kanhira = mKanhira.first() ?: return@runBlocking ""
            return@runBlocking kanhira.convert(text)
        }
    }

    /**
     * 将片假名或平假名转为罗马字
     */
    fun toRomajiString(text: String): String? {
        return kanaToRomaji.convert(text)
    }

    /**
     * 判断是否包含汉字（日文汉字、简体汉字、繁体汉字）
     */
    fun isContainChinese(str: String): Boolean {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find()
    }

    /**
     * 判断是否包含日文片假名或平假名
     */
    fun isContainKatakanaOrHinagana(str: String): Boolean {
        return Pattern.compile("[\u3040-\u309f]").matcher(str).find() ||
                Pattern.compile("[\u30a0-\u30ff]").matcher(str).find()
    }
}