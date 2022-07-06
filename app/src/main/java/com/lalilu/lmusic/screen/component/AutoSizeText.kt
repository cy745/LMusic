package com.lalilu.lmusic.screen.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import kotlin.math.absoluteValue
import kotlin.math.ceil

/**
 * @author https://stackoverflow.com/users/1208270/embmicro
 * @from https://stackoverflow.com/a/71416530/18440392
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    acceptableError: Dp = 5.dp,
    maxFontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    BoxWithConstraints(modifier = modifier) {
        var shrunkFontSize = if (maxFontSize.isSpecified) maxFontSize else 100.sp

        val calculateIntrinsics = @Composable {
            val mergedStyle = style.merge(
                TextStyle(
                    color = color,
                    fontSize = shrunkFontSize,
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    fontFamily = fontFamily,
                    textDecoration = textDecoration,
                    fontStyle = fontStyle,
                    letterSpacing = letterSpacing
                )
            )
            Paragraph(
                text = text,
                style = mergedStyle,
                constraints = Constraints(maxWidth = ceil(LocalDensity.current.run { maxWidth.toPx() }).toInt()),
                density = LocalDensity.current,
                fontFamilyResolver = LocalFontFamilyResolver.current,
                spanStyles = listOf<androidx.compose.ui.text.AnnotatedString.Range<androidx.compose.ui.text.SpanStyle>>(),
                placeholders = listOf<androidx.compose.ui.text.AnnotatedString.Range<androidx.compose.ui.text.Placeholder>>(),
                maxLines = maxLines,
                ellipsis = false
            )
        }

        var intrinsics = calculateIntrinsics()

        val targetWidth = maxWidth - acceptableError / 2f

        with(LocalDensity.current) {
            if (maxFontSize.isUnspecified || targetWidth < intrinsics.minIntrinsicWidth.toDp()) {
                while ((targetWidth - intrinsics.minIntrinsicWidth.toDp()).toPx().absoluteValue.toDp() > acceptableError / 2f) {
                    shrunkFontSize *= targetWidth.toPx() / intrinsics.minIntrinsicWidth
                    intrinsics = calculateIntrinsics()
                }
                while (intrinsics.didExceedMaxLines || maxHeight < intrinsics.height.toDp()) {
                    shrunkFontSize *= 0.9f
                    intrinsics = calculateIntrinsics()
                }
            }
        }

        if (maxFontSize.isSpecified && shrunkFontSize > maxFontSize)
            shrunkFontSize = maxFontSize

        Text(
            text = text,
            color = color,
            fontSize = shrunkFontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            onTextLayout = onTextLayout,
            maxLines = maxLines,
            style = style
        )
    }
}