package com.lalilu.lmusic.compose.screen.playing.lyric.serializable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class PaddingValueSerializer : KSerializer<PaddingValues> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PaddingValues") {
        element<Float>("left")
        element<Float>("right")
        element<Float>("top")
        element<Float>("bottom")
    }

    override fun deserialize(decoder: Decoder): PaddingValues {
        return decoder.decodeStructure(descriptor) {
            var left = 0f
            var right = 0f
            var top = 0f
            var bottom = 0f

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> left = decodeFloatElement(descriptor, 0)
                    1 -> right = decodeFloatElement(descriptor, 1)
                    2 -> top = decodeFloatElement(descriptor, 2)
                    3 -> bottom = decodeFloatElement(descriptor, 3)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            PaddingValues(
                start = left.dp,
                end = right.dp,
                top = top.dp,
                bottom = bottom.dp
            )
        }
    }

    override fun serialize(encoder: Encoder, value: PaddingValues) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(
                descriptor, 0,
                value.calculateLeftPadding(LayoutDirection.Ltr).value
            )
            encodeFloatElement(
                descriptor, 1,
                value.calculateRightPadding(LayoutDirection.Ltr).value
            )
            encodeFloatElement(descriptor, 2, value.calculateTopPadding().value)
            encodeFloatElement(descriptor, 3, value.calculateBottomPadding().value)
        }
    }
}