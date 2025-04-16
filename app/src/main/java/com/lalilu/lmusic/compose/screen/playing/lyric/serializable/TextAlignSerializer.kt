package com.lalilu.lmusic.compose.screen.playing.lyric.serializable

import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class TextAlignSerializer : KSerializer<TextAlign> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TextAlign", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TextAlign {
        val string = decoder.decodeString()
        return when (string) {
            "Left" -> TextAlign.Left
            "Right" -> TextAlign.Right
            "Center" -> TextAlign.Center
            "Justify" -> TextAlign.Justify
            "Start" -> TextAlign.Start
            "End" -> TextAlign.End
            "Unspecified" -> TextAlign.Unspecified
            else -> TextAlign.Unspecified
        }
    }

    override fun serialize(encoder: Encoder, value: TextAlign) {
        encoder.encodeString(value.toString())
    }
}