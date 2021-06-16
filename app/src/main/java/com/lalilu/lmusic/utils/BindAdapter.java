package com.lalilu.lmusic.utils;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lalilu.common.GlobalCommon;
import com.lalilu.common.TextUtils;
import com.lalilu.lmusic.R;

public class BindAdapter {
    @BindingAdapter("iconRec")
    public static void setIcon(ImageView imageView, String string) {
        int result;
        String[] strings = string.split("/");
        switch (strings[strings.length - 1].toUpperCase()) {
            case "FLAC":
                result = R.drawable.ic_flac;
                break;
            case "WAV":
            case "X-WAV":
                result = R.drawable.ic_wav;
                break;
            case "APE":
                result = R.drawable.ic_ape;
                break;
            case "MPEG":
            case "MP3":
            default:
                result = R.drawable.ic_mp3;
        }
        imageView.setImageResource(result);
    }

    @BindingAdapter("pictureUri")
    public static void setPictureUri(SimpleDraweeView simpleDraweeView, Uri uri) {
        simpleDraweeView.setImageURI(uri);
    }

    public static String getDurationFromExtra(Bundle extra) {
        if (extra == null) return "null";
        return TextUtils.Companion.durationToString(extra.getLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0));
    }

    public static String getArtistFromExtra(Bundle extra) {
        if (extra == null) return "null";
        return extra.getString(MediaMetadataCompat.METADATA_KEY_ARTIST, "someone...");
    }

    public static String getSongTypeFromExtra(Bundle extra) {
        if (extra == null) return "null";
        return extra.getString(GlobalCommon.MEDIA_MIME_TYPE, "mp3");
    }
}
