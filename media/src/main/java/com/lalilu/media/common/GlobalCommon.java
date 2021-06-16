package com.lalilu.media.common;

public class GlobalCommon {
    public final static String MEDIA_MIME_TYPE = "media_mime_type";

    public static String DurationToString(Number duration) {
        long temp = duration.longValue() / 1000;

        long min = temp / 60;
        long sec = temp % 60;

        return (min < 10 ? "0" : " ") + min + ":" + (sec < 10 ? "0" : " ") + sec;
    }
}
