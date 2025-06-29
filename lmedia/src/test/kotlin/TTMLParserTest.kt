import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.dom2.Node
import nl.adaptivity.xmlutil.dom2.Text
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import nl.adaptivity.xmlutil.util.CompactFragment
import org.junit.Test


class TTMLParserTest {
    val xml = XML(
        serializersModule = SerializersModule {
            polymorphic(Any::class) {
                defaultDeserializer { String.serializer() }
                subclass(Element::class)
                subclass(String::class)
            }
        }
    ) {
        defaultPolicy {
            autoPolymorphic = true
            ignoreUnknownChildren()
            fast_0_90_2 { }
        }
    }

    private val testTT = """
        <tt xmlns="http://www.w3.org/ns/ttml" xmlns:itunes="http://music.apple.com/lyric-ttml-internal" xmlns:ttm="http://www.w3.org/ns/ttml#metadata" itunes:timing="Word" xml:lang="en">
          <head>
            <metadata>
              <ttm:agent type="person" xml:id="v1"/>
              <ttm:agent type="person" xml:id="v2"/>
              <ttm:agent type="person" xml:id="v3"/>
              <iTunesMetadata xmlns="http://music.apple.com/lyric-ttml-internal" leadingSilence="0">
                <translations/>
                <songwriters>
                  <songwriter>Dua Saleh</songwriter>
                  <songwriter>Ebony Oshunrinde</songwriter>
                  <songwriter>Jacques Bermon Webster II</songwriter>
                  <songwriter>Joseph Thornalley</songwriter>
                  <songwriter>Josiah Sherman</songwriter>
                  <songwriter>Justin Vernon</songwriter>
                  <songwriter>Phil Cook</songwriter>
                  <songwriter>Sampha Sisay</songwriter>
                  <songwriter>Wesley Glass</songwriter>
                </songwriters>
              </iTunesMetadata>
            </metadata>
          </head>
          <body dur="4:11.250">
            <div begin="17.800" end="47.207" itunes:songPart="PreChorus" ttm:agent="v2">
              <p begin="17.800" end="19.907" itunes:key="L1" ttm:agent="v2"><span begin="17.800" end="18.018">When</span> <span begin="18.018" end="18.118">I</span> <span begin="18.118" end="18.505">stare</span> <span begin="18.505" end="18.671">in</span> <span begin="18.671" end="18.886">your</span> <span begin="18.886" end="19.907">eyes</span></p>
              <p begin="21.850" end="23.915" itunes:key="L2" ttm:agent="v2"><span begin="21.850" end="21.950">You'll</span> <span begin="21.950" end="22.086">be</span> <span begin="22.086" end="22.561">there</span> <span begin="22.561" end="22.820">for</span><span begin="22.820" end="23.915">ever</span></p>
              <p begin="24.954" end="29.012" itunes:key="L3" ttm:agent="v2"><span begin="24.954" end="25.125">To</span> <span begin="25.125" end="25.487">watch</span> <span begin="25.487" end="25.921">our</span> <span begin="25.921" end="26.881">life</span> <span ttm:role="x-bg"><span begin="25.763" end="26.091">(To</span> <span begin="26.091" end="26.437">watch</span> <span begin="26.437" end="26.987">our</span> <span begin="26.987" end="27.837">life</span> <span begin="27.837" end="28.224">to</span><span begin="28.224" end="29.012">gether)</span></span></p>
              <p begin="29.851" end="32.954" itunes:key="L4" ttm:agent="v2"><span begin="29.851" end="30.191">You</span> <span begin="30.191" end="30.425">just</span> <span begin="30.425" end="30.991">like</span> <span begin="30.991" end="31.724">going</span> <span begin="31.724" end="31.921">to</span> <span begin="31.921" end="32.954">Heaven</span> <span ttm:role="x-bg"><span begin="29.854" end="30.136">(My</span> <span begin="30.136" end="30.739">heart)</span></span></p>
              <p begin="33.218" end="36.512" itunes:key="L5" ttm:agent="v2"><span begin="33.218" end="33.619">Oh,</span> <span begin="33.619" end="34.104">where</span> <span begin="34.104" end="34.571">are</span> <span begin="34.571" end="34.838">you</span> <span begin="34.838" end="35.387">taking</span> <span begin="35.387" end="36.512">me?</span></p>
              <p begin="41.120" end="44.393" itunes:key="L6" ttm:agent="v2"><span begin="41.120" end="41.404">I'm</span> <span begin="41.404" end="41.940">fallin',</span> <span begin="43.044" end="43.220">and</span> <span begin="43.220" end="43.380">I'm</span> <span begin="43.380" end="44.393">drownin'</span></p>
              <p begin="46.085" end="47.207" itunes:key="L7" ttm:agent="v2"><span begin="46.085" end="46.269">But</span> <span begin="46.269" end="46.369">you're</span> <span begin="46.369" end="46.648">takin'</span> <span begin="46.648" end="47.207">me</span></p>
            </div>
          </body>
        </tt>
    """.trimIndent()

    @Serializable
    @XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
    class TTML

    @Serializable
    @XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
    data class TTML2(
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
        val lang: String? = null
    )

    @Serializable
    @XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
    data class TTML3(
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
        val head: TTMLHead? = null
    ) {
        @Serializable
        data class TTMLHead(
            @XmlChildrenName(value = "metadata")
            val metadata: List<Element> = emptyList()
        )
    }

    @Serializable
    @XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
    data class TTML4(
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
        val head: TTMLHead? = null,
        @XmlSerialName(value = "body")
        val body: TTMLBody? = null
    ) {
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
        ) {
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
                @XmlChildrenName(value = "p")
                val p: List<Element> = emptyList()
            )
        }
    }

    @Serializable
    @XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
    data class TTML5(
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
    ) {
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
        ) {
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
            ) {
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
                    val span: List<Element> = emptyList()
                )
            }
        }
    }

    @Serializable
    @XmlSerialName(value = "tt", namespace = "http://www.w3.org/ns/ttml")
    data class TTML6(
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
    ) {
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
        ) {
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
            ) {
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
                ) {

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
                }
            }
        }
    }

    val text = "<tag>&amp;Content</tag>"

    @XmlSerialName(value = "tag")
    @Serializable
    data class TAG(
        @XmlValue
        val content: String
    )

    @XmlSerialName(value = "tag")
    @Serializable
    data class TAG2(
        @XmlValue
        val content: List<Node> = emptyList()
    )

    @XmlSerialName(value = "tag")
    @Serializable
    data class TAG3(
        @XmlValue
        val content: List<CompactFragment> = emptyList()
    )

    @Test
    fun testParser() {
        val ttml = xml.decodeFromString<TTML>(testTT)
        println(ttml)

        val ttml2 = xml.decodeFromString<TTML2>(testTT)
        assert(ttml2.toString() == "TTML2(timing=Word, lang=en)")
        println(ttml2)

        val ttml3 = xml.decodeFromString<TTML3>(testTT)
        println(ttml3)

        val ttml4 = xml.decodeFromString<TTML4>(testTT)
        ttml4.body?.div?.forEach { println(it) }

        val ttml5 = xml.decodeFromString<TTML5>(testTT)
        ttml5.body.div.map { it.p }.flatten().forEach { println(it) }

        val ttml6 = xml.decodeFromString<TTML6>(testTT)
        ttml6.body.div
            .map { it.p }.flatten()
            .map { it.span }.flatten()
            .forEach { item ->
                println(item.content())
                item.children()?.forEach {
                    println("$it ${it.content()}")
                }
            }

        val ampTestResult = xml.decodeFromString<TAG>(text)
        println(ampTestResult)

        val amp3TestResult = xml.decodeFromString<TAG3>(text)
        println(amp3TestResult) // TAG3(content=[{namespaces=[], content=&}, {namespaces=[], content=Content}])

//        val amp2TestResult = xml.decodeFromString<TAG2>(text)
//        println(amp2TestResult)
    }
}