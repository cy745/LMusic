package com.lalilu.lmedia.lyric

interface LyricParser {
    fun parse(lyric: String): List<LyricItem>
}