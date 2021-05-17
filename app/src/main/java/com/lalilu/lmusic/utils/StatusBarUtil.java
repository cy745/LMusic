package com.lalilu.lmusic.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

public class StatusBarUtil {

    /**
     * 隐藏状态栏背景，同时根据暗黑模式自动状态栏文字自动反色
     *
     * @param activity 传入所需修改的Activity
     */
    public static void setStatusBarColor(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();

        window.setStatusBarColor(Color.TRANSPARENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            textColorChange_R(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textColorChange_M(activity);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void textColorChange_R(Activity activity) {
        if (isDarkMode(activity)) {
            activity.getWindow().getDecorView()
                    .getWindowInsetsController()
                    .setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            activity.getWindow().getDecorView()
                    .getWindowInsetsController()
                    .setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void textColorChange_M(Activity activity) {
        int uiOption = activity.getWindow().getDecorView().getSystemUiVisibility();
        if (isDarkMode(activity)) {
            //没有DARK_STATUS_BAR属性，通过位运算将LIGHT_STATUS_BAR属性去除
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOption & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            //这里是要注意的地方，如果需要补充新的FLAG，记得要带上之前的然后进行或运算
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOption | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


    public static boolean isDarkMode(Activity activity) {
        int mode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }
}
