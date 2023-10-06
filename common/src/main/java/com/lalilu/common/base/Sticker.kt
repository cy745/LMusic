package com.lalilu.common.base

sealed class Sticker(val name: String) {
    open class ExtSticker(ext: String) : Sticker(ext)
    open class SourceSticker(source: String) : Sticker(source)
    data object HasLyricSticker : Sticker("LRC")
    data object HiresSticker : Sticker("HIRES")
}


data object FlacSticker : Sticker.ExtSticker("FLAC")
data object DSDSticker : Sticker.ExtSticker("DSD")
data object WavSticker : Sticker.ExtSticker("WAV")
data object Mp3Sticker : Sticker.ExtSticker("MP3")
data object Mp4Sticker : Sticker.ExtSticker("MP4")


data object LocalSticker : Sticker.SourceSticker("LOCAL")
data object WebDavSticker : Sticker.SourceSticker("WEBDAV")
data object CloudSticker : Sticker.SourceSticker("CLOUD")

