package com.lalilu.lmusic.compose.screen.playing.lyric.serializable

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class TextUnitSerializer : KSerializer<TextUnit> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TextUnit") {
        element<Float>("value")
        element<String>("type")
    }

    override fun deserialize(decoder: Decoder): TextUnit {
        return decoder.decodeStructure(descriptor) {
            var value = 0f
            var type = ""

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> value = decodeFloatElement(descriptor, 0)
                    1 -> type = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            when (type.lowercase()) {
                "sp" -> value.sp
                "em" -> value.em
                else -> 0.sp
            }
        }
    }

    override fun serialize(encoder: Encoder, value: TextUnit) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.value)
            encodeStringElement(descriptor, 1, value.type.toString())
        }
    }
}