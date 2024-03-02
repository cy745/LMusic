package com.lalilu.lmusic.compose.screen.playing

import android.content.Context
import com.lalilu.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.coroutines.CoroutineContext

data class LyricContent(
    val name: String,
    val begin: Long,
    val end: Long,
    val duration: Long,
    val agent: List<Agent>,
    val sentences: List<Sentence>
)

data class Agent(
    val xmlId: String,
    val type: String,
)

data class Translation(
    val role: String,
    val lang: String,
    val text: String
)

sealed class Sentence {
    data class TTMLSentence(
        val begin: Long,
        val end: Long,
        val agent: String,
        val itunesKey: String,
        val characters: List<TTMLCharacter>,
        val translation: Translation? = null
    ) : Sentence()

    data class NormalSentence(
        val begin: Long,
        val text: String,
        val translation: String = ""
    ) : Sentence()

    data class EmptySentence(
        val begin: Long,
        val end: Long
    ) : Sentence()
}

data class TTMLCharacter(
    val begin: Long,
    val end: Long,
    val content: String
)

object TTMLParser : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    fun parse(context: Context) = launch {
//        val str = context.resources.openRawResource(R.raw.lyric)
//        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
//            .parse(str)
//
//        val result = retrieveLyricFromNode(doc)
    }
}

suspend fun retrieveLyricFromNode(doc: Document): LyricContent? = withContext(Dispatchers.IO) {
    val head = doc.getElementsByTagName("head").item(0)
    val body = doc.getElementsByTagName("body").item(0)
    val metadata = head.firstChild
    val div = body.firstChild

    val agent = metadata.childNodes.toList().mapNotNull {
        val type = it.getAttrByName("type") ?: return@mapNotNull null
        val id = it.getAttrByName("xml:id") ?: return@mapNotNull null
        Agent(xmlId = id, type = type)
    }

    val duration = body.getAttrByName("dur")?.let(::parseTimeSpan) ?: return@withContext null
    val begin = div.getAttrByName("begin")?.let(::parseTimeSpan) ?: return@withContext null
    val end = div.getAttrByName("end")?.let(::parseTimeSpan) ?: return@withContext null

    val sentences = div.childNodes.toList()
        .map { async { retrieveSentenceFromNode(it) } }
        .awaitAll()
        .filterNotNull()

    LyricContent(
        name = "",
        begin = begin,
        end = end,
        duration = duration,
        agent = agent,
        sentences = sentences
    )
}

suspend fun retrieveSentenceFromNode(node: Node): Sentence? = withContext(Dispatchers.IO) {
    val begin = node.getAttrByName("begin")?.let(::parseTimeSpan) ?: return@withContext null
    val end = node.getAttrByName("end")?.let(::parseTimeSpan) ?: return@withContext null
    val agent = node.getAttrByName("ttm:agent") ?: return@withContext null
    val itunesKey = node.getAttrByName("itunes:key") ?: return@withContext null
    var translation: Translation? = null

    val characters = node.childNodes.toList().mapNotNull { node ->
        retrieveCharacterFromNode(node).also {
            it ?: return@also
            translation = retrieveTranslationFromNode(node)
        }
    }

    Sentence.TTMLSentence(begin, end, agent, itunesKey, characters, translation)
}

private fun retrieveCharacterFromNode(node: Node): TTMLCharacter? {
    val begin = node.getAttrByName("begin")?.let(::parseTimeSpan) ?: return null
    val end = node.getAttrByName("end")?.let(::parseTimeSpan) ?: return null
    val text = node.textContent ?: return null

    return TTMLCharacter(begin, end, text)
}

private fun retrieveTranslationFromNode(node: Node): Translation? {
    val role = node.getAttrByName("ttm:role") ?: return null
    val lang = node.getAttrByName("xml:lang") ?: return null
    val text = node.textContent ?: return null

    return Translation(role, lang, text)
}

private val timeRegexp =
    Pattern.compile("^(?:([0-9]{2}):)?([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]+))?)?$")

private fun parseTimeSpan(string: String?): Long {
    string ?: return 0

    val matches = timeRegexp.matcher(string)
    if (matches.matches()) {
        val hour = matches.group(1)?.toIntOrNull() ?: 0
        val min = matches.group(2)?.toIntOrNull() ?: 0
        val sec = matches.group(3)?.toIntOrNull() ?: 0
        val millisecond = matches.group(4)?.toIntOrNull() ?: 0
        return ((hour * 3600f + min * 60f + sec + millisecond) * 1000f).toLong()
    }
    return 0
}

private fun Node.getAttrByName(name: String): String? {
    return attributes.getNamedItem(name)?.textContent
}

private fun NodeList.toList(): List<Node> {
    return (0 until length).map { item(it) }
}