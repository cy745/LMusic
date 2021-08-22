package com.lalilu.lmusic.utils;

import android.app.Activity;
import android.content.res.Configuration;


/**
 * 判断是否当前为深色模式的工具类
 */
public class DarkModeUtil {
    public static boolean isDarkMode(Activity activity) {
        int mode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }
}
