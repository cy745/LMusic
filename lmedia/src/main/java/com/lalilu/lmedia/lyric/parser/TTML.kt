package com.lalilu.lmedia.lyric.parser


import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.dom2.Node
import nl.adaptivity.xmlutil.dom2.Text
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

/**
 * TTML 示例
 * 详见：<a href="https://github.com/Steve-xmh/applemusic-like-lyrics">applemusic-like-lyrics</a>
 *
 * ```xml
 * <tt xmlns:amll="http://www.example.com/ns/amll"
 *     xmlns:itunes="http://music.apple.com/lyric-ttml-internal"
 *     xmlns:ttm="http://www.w3.org/ns/ttml#metadata" xmlns="http://www.w3.org/ns/ttml">
 *     <head>
 *         <metadata>
 *             <ttm:agent type="person" xml:id="v1" />
 *             <amll:meta key="ncmMusicId" value="2083872223" />
 *             ......
 *         </metadata>
 *     </head>
 *     <body dur="03:01.212">
 *         <div begin="00:00.000" end="03:01.212">
 *             <p begin="00:31.110" end="00:35.104" itunes:key="L6" ttm:agent="v1">
 *                 <span begin="00:31.110" end="00:33.107">ほほえむ</span>
 *                 <span begin="00:33.107" end="00:35.104">君がいる</span>
 *                 <span ttm:role="x-translation" xml:lang="zh-CN">你微笑着 站在此处</span>
 *             </p>
 *             ......
 *         </div>
 *     </body>
 * </tt>
 * ```
 *
 * ```kotlin
 * // 需要按照如下格式创建XML对象实例
 * XML {
 *     autoPolymorphic = true
 *     fast_0_90_2()
 * }
 * ```
 */
@Serializable
@XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
data class TTML(
    @XmlSerialName(
        value = "timing",
        namespace = "http://music.apple.com/lyric-ttml-internal",
        prefix = "itunes"
    )
    val timing: String? = null,
    @XmlSerialName(
        value = "lang",
        namespace = "http://www.w3.org/XML/1998/namespace",
        prefix = "xml"
    )
    val lang: String? = null,
    @XmlSerialName(value = "head")
    val head: TTMLHead,
    @XmlSerialName(value = "body")
    val body: TTMLBody
)

@Serializable
data class TTMLHead(
    @XmlChildrenName(value = "metadata")
    val metadata: List<Element> = emptyList()
)

@Serializable
data class TTMLBody(
    @XmlSerialName(value = "dur")
    val dur: String,
    @XmlValue
    @XmlSerialName(value = "div")
    val div: List<TTMLDiv> = emptyList()
)

@Serializable
data class TTMLDiv(
    @XmlSerialName("begin")
    val begin: String,
    @XmlSerialName("end")
    val end: String,
    @XmlSerialName(
        value = "songPart",
        prefix = "itunes",
        namespace = "http://music.apple.com/lyric-ttml-internal"
    )
    val songPart: String? = null,
    @XmlSerialName(
        value = "agent",
        namespace = "http://www.w3.org/ns/ttml#metadata",
        prefix = "ttm"
    )
    val agent: String? = null,
    @XmlValue
    val p: List<TTMLP> = emptyList()
)

@Serializable
data class TTMLP(
    @XmlSerialName("begin")
    val begin: String,
    @XmlSerialName("end")
    val end: String,
    @XmlSerialName(
        value = "key",
        prefix = "itunes",
        namespace = "http://music.apple.com/lyric-ttml-internal"
    )
    val key: String? = null,
    @XmlSerialName(
        value = "agent",
        namespace = "http://www.w3.org/ns/ttml#metadata",
        prefix = "ttm"
    )
    val agent: String? = null,
    @XmlValue
    val span: List<TTMLSpan> = emptyList()
)

@Serializable
data class TTMLSpan(
    @XmlSerialName("begin")
    val begin: String? = null,
    @XmlSerialName("end")
    val end: String? = null,
    @XmlSerialName(
        value = "role",
        prefix = "ttm",
        namespace = "http://www.w3.org/ns/ttml#metadata",
    )
    val role: String? = null,
    @XmlSerialName(
        value = "lang",
        prefix = "xml",
        namespace = "http://www.w3.org/XML/1998/namespace",
    )
    val lang: String? = null,
    @XmlValue
    private val value: List<Node>? = null,
) {
    fun isTranslation(): Boolean = role == "x-translation"

    fun content(): String? {
        return value?.firstOrNull()?.takeIf { it is Text }
            ?.getTextContent()
    }

    fun children(): List<TTMLSpan>? {
        return value?.mapNotNull {
            if (it !is Element) return@mapNotNull null

            TTMLSpan(
                begin = it.getAttribute("begin"),
                end = it.getAttribute("end"),
                role = it.getAttribute("role"),
                lang = it.getAttribute("lang"),
                value = it.getChildNodes().toList()
            )
        }
    }
}