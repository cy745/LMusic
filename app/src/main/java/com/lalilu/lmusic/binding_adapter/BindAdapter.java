package com.lalilu.lmusic.binding_adapter;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lalilu.R;

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

    public static String durationToString(Number duration) {
        long temp = duration.longValue() / 1000;
        if (temp < 1) {
            temp *= 1000;
        }

        long min = temp / 60;
        long sec = temp % 60;
        return (min < 10 ? "0" : " ") + min + ":" +
                (sec < 10 ? "0" : " ") + sec;
    }

    @BindingAdapter(value = {"pictureUri", "callerContext"}, requireAll = false)
    public static void setPictureUri(SimpleDraweeView simpleDraweeView, Uri uri, Context callerContext) {
        simpleDraweeView.setImageURI(uri, callerContext);
    }
}
