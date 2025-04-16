package com.lalilu.lmusic.compose.screen.playing.lyric.serializable

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class DpSerializer : KSerializer<Dp> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Dp", PrimitiveKind.FLOAT)

    override fun deserialize(decoder: Decoder): Dp {
        return decoder.decodeFloat().dp
    }

    override fun serialize(encoder: Encoder, value: Dp) {
        encoder.encodeFloat(value.value)
    }
}