package com.lalilu.lmusic.utils

class DurationUtils {
    companion object {
        fun durationToString(duration: Number): String {
            val temp =
                if (duration.toLong() > 10000) duration.toLong().div(1000) else duration.toLong()

            val min = temp.div(60)
            val sec = temp % 60
            return "${if (min < 10) "0" else ""}$min:${if (sec < 10) "0" else ""}$sec"
        }
    }
}