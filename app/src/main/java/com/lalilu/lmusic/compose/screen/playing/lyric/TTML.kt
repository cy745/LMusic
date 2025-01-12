package com.lalilu.lmusic.compose.screen.playing.lyric

import kotlinx.serialization.Serializable
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
class TTML(
    val head: TTMLHead,
    val body: TTMLBody
)

@Serializable
@XmlSerialName(value = "head")
class TTMLHead(
    @XmlChildrenName("metadata")
    val metadata: List<MetadataItem>
)

@Serializable
sealed class MetadataItem {

    @Serializable
    @XmlSerialName(
        value = "agent",
        namespace = "http://www.w3.org/ns/ttml#metadata",
        prefix = "ttm"
    )
    data class TTMLMetadataAgent(
        @XmlSerialName(value = "type")
        val type: String,
        @XmlSerialName(
            value = "id",
            namespace = "http://www.w3.org/XML/1998/namespace",
            prefix = "xml"
        )
        val id: String
    ) : MetadataItem()

    @Serializable
    @XmlSerialName(
        value = "meta",
        namespace = "http://www.example.com/ns/amll",
        prefix = "amll"
    )
    data class TTMLMetadataItem(
        val key: String,
        val value: String
    ) : MetadataItem()
}

@Serializable
@XmlSerialName(value = "body")
class TTMLBody(
    @XmlSerialName(value = "dur")
    val dur: String,
    @XmlSerialName(value = "div")
    val div: TTMLBodyDiv,
)

@Serializable
@XmlSerialName(value = "div")
data class TTMLBodyDiv(
    @XmlSerialName("begin")
    val begin: String,
    @XmlSerialName("end")
    val end: String,
    val p: List<TTMLBodyDivP>
)

@Serializable
@XmlSerialName(value = "p")
data class TTMLBodyDivP(
    @XmlSerialName("begin")
    val begin: String,

    @XmlSerialName("end")
    val end: String,

    @XmlSerialName(
        value = "key",
        prefix = "itunes",
        namespace = "http://music.apple.com/lyric-ttml-internal"
    )
    val key: String,

    @XmlSerialName(
        value = "agent",
        namespace = "http://www.w3.org/ns/ttml#metadata",
        prefix = "ttm"
    )
    val agent: String,
    val spans: List<TTMLSpan> = emptyList()
)

@Serializable
@XmlSerialName(value = "span")
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
    val content: String = ""
)