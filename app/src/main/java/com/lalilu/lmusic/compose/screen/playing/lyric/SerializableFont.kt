package com.lalilu.lmusic.compose.screen.playing.lyric

import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
sealed interface SerializableFont {
    fun toFont(
        weight: FontWeight = FontWeight.Normal,
        style: FontStyle = FontStyle.Normal,
        variationSettings: FontVariation.Settings = FontVariation.Settings()
    ): Font?

    data class LoadedFont(val fontPath: String) : SerializableFont {
        override fun toFont(
            weight: FontWeight,
            style: FontStyle,
            variationSettings: FontVariation.Settings
        ): Font? {
            return if (fontPath.isBlank()) null
            else runCatching {
                Font(
                    file = File(fontPath),
                    weight = weight,
                    style = style,
                    variationSettings = variationSettings
                )
            }.getOrNull()
        }
    }

    data class DeviceFont(val fontName: String) : SerializableFont {
        override fun toFont(
            weight: FontWeight,
            style: FontStyle,
            variationSettings: FontVariation.Settings
        ): Font? {
            return if (fontName.isBlank()) null
            else runCatching {
                Font(
                    familyName = DeviceFontFamilyName(fontName),
                    weight = weight,
                    style = style,
                    variationSettings = variationSettings
                )
            }.getOrNull()
        }
    }
}