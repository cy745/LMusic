package com.lalilu.media.enum

enum class MediaMIMEType(val position: Int, val value: String) {
    MIME_KEY_FLAC(1, "flac"),
    MIME_KEY_WAV(2, "wav"),
    MIME_KEY_MP3(3, "mp3"),
    MIME_KEY_APE(4, "ape");

    @Throws(NoWhenBranchMatchedException::class)
    fun of(position: Int): MediaMIMEType {
        return when (position) {
            0 -> MIME_KEY_FLAC
            1 -> MIME_KEY_WAV
            2 -> MIME_KEY_MP3
            3 -> MIME_KEY_APE
            else -> throw NoWhenBranchMatchedException("No Match MediaMIMEType Set In.")
        }
    }
}