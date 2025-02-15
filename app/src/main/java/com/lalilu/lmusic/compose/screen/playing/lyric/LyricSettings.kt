@file:UseSerializers(
    TextAlignSerializer::class,
    TextUnitSerializer::class,
    DpSerializer::class,
)

package com.lalilu.lmusic.compose.screen.playing.lyric

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.lmusic.compose.screen.playing.lyric.serializable.DpSerializer
import com.lalilu.lmusic.compose.screen.playing.lyric.serializable.TextAlignSerializer
import com.lalilu.lmusic.compose.screen.playing.lyric.serializable.TextUnitSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers


internal val DEFAULT_TEXT_SHADOW = Shadow(
    color = Color.Black.copy(alpha = 0.2f),
    offset = Offset(x = 0f, y = 1f),
    blurRadius = 1f
)

@Serializable
data class LyricSettings(
    // 布局样式配置
    val textAlign: TextAlign = TextAlign.Start,
    val containerPadding: PaddingValues = PaddingValues(horizontal = 40.dp, vertical = 15.dp),
    val gapSize: Dp = 10.dp,
    val scaleRange: ClosedRange<Float> = 0.85f..1f,
    val timeOffset: Long = 50L,

    // 字体样式配置
    val mainFontSize: TextUnit = 26.sp,
    val mainLineHeight: TextUnit = 28.sp,
    val mainFontWeight: Int = FontWeight.Black.weight,
    val mainFont: SerializableFont? = null,
    val translationFontSize: TextUnit = 22.sp,
    val translationLineHeight: TextUnit = 26.sp,
    val translationFontWeight: Int = FontWeight.Bold.weight,
    val translationFont: SerializableFont? = null,

    // 特殊效果开关
    val blurEffectEnable: Boolean = true,
    val translationVisible: Boolean = true,
    val variableFontWeightEnable: Boolean = false
) {
    val mainTextStyle: TextStyle by lazy {
        TextStyle.Default.copy(
            fontSize = mainFontSize,
            textAlign = textAlign,
            lineHeight = mainLineHeight,
            fontWeight = FontWeight(mainFontWeight),
            fontFamily = FontFamily(
                mainFont?.toFont(
                    variationSettings = FontVariation.Settings(FontVariation.weight(mainFontWeight))
                ) ?: Font(
                    familyName = DeviceFontFamilyName("FontFamily.Monospace"),
                    variationSettings = FontVariation.Settings(FontVariation.weight(mainFontWeight))
                )
            )
        )
    }

    val translationTextStyle: TextStyle by lazy {
        TextStyle.Default.copy(
            fontSize = translationFontSize,
            textAlign = textAlign,
            lineHeight = translationLineHeight,
            fontWeight = FontWeight(translationFontWeight),
            fontFamily = FontFamily(
                translationFont?.toFont(
                    variationSettings = FontVariation.Settings(FontVariation.weight(mainFontWeight))
                ) ?: Font(
                    familyName = DeviceFontFamilyName("FontFamily.Monospace"),
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(translationFontWeight)
                    )
                )
            )
        )
    }
}