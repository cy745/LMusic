package com.lalilu.common.base

import androidx.compose.runtime.Stable

@Stable
sealed class Sticker(val name: String) {
    @Stable
    open class ExtSticker(ext: String) : Sticker(ext)

    @Stable
    open class SourceSticker(sourceType: SourceType) : Sticker(sourceType.name)
    data object HasLyricSticker : Sticker("LRC")
    data object HiresSticker : Sticker("HIRES")
}


data object FlacSticker : Sticker.ExtSticker("FLAC")
data object DSDSticker : Sticker.ExtSticker("DSD")
data object WavSticker : Sticker.ExtSticker("WAV")
data object Mp3Sticker : Sticker.ExtSticker("MP3")
data object Mp4Sticker : Sticker.ExtSticker("MP4")


data object LocalSticker : Sticker.SourceSticker(SourceType.Local)
data object WebDavSticker : Sticker.SourceSticker(SourceType.WebDAV)
data object CloudSticker : Sticker.SourceSticker(SourceType.Network)

