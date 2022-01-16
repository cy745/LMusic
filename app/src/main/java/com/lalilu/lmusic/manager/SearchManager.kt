package com.lalilu.lmusic.manager

import android.content.Context
import com.cm55.kanhira.KakasiDictReader
import com.cm55.kanhira.Kanhira
import com.lalilu.R
import com.lalilu.lmusic.utils.KanaToRomaji
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class SearchManager @Inject constructor(
    @ApplicationContext context: Context
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val kanaToRomaji = KanaToRomaji()
    private val format = HanyuPinyinOutputFormat().also {
        it.caseType = HanyuPinyinCaseType.UPPERCASE
        it.toneType = HanyuPinyinToneType.WITHOUT_TONE
        it.vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    val keyword: MutableStateFlow<String?> = MutableStateFlow(null)
    val mKanhira = MutableStateFlow<Kanhira?>(null).also {
        launch {
            it.emit(
                Kanhira(
                    KakasiDictReader.load(
                        context.resources.openRawResource(R.raw.kakasidict_utf_8),
                        Charsets.UTF_8.name()
                    )
                )
            )
        }
    }

    fun searchFor(text: String?) = launch {
        keyword.emit(text)
    }

    fun checkKeywords(str: CharSequence?, keywords: List<String>): Boolean {
        keywords.forEach { keyword ->
            if (!checkKeyword(str, keyword)) return false
        }
        return true
    }

    private fun checkKeyword(str: CharSequence?, keyword: String): Boolean {
        str ?: return false
        return str.toString().uppercase(Locale.getDefault()).contains(
            keyword.uppercase(Locale.getDefault())
        )
    }

    fun toHanYuPinyinString(text: String): String? {
        return PinyinHelper.toHanYuPinyinString(text, format, "", true)
    }

    fun toHiraString(text: String): String {
        return runBlocking {
            val kanhira = mKanhira.first() ?: return@runBlocking ""
            return@runBlocking kanhira.convert(text)
        }
    }

    fun toRomajiString(text: String): String? {
        return kanaToRomaji.convert(text)
    }

    fun isContainChinese(str: String): Boolean {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find()
    }

    fun isContainKatakanaOrHinagana(str: String): Boolean {
        return Pattern.compile("[\u3040-\u309f]").matcher(str).find() ||
                Pattern.compile("[\u30a0-\u30ff]").matcher(str).find()
    }
}