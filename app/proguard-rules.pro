# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson反射所需混淆规则
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# AndroidAop必备混淆规则
-keep class * { @androidx.annotation.Keep <fields>; }
-keepnames class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut
-keepnames class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod
-keep class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
    public <init>();
}
-keep class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
    public <init>();
}

-dontwarn com.bumptech.glide.Glide
-dontwarn com.bumptech.glide.RequestBuilder
-dontwarn com.bumptech.glide.RequestManager
-dontwarn com.bumptech.glide.request.BaseRequestOptions
-dontwarn com.bumptech.glide.request.target.ViewTarget
-dontwarn com.squareup.picasso.Picasso
-dontwarn com.squareup.picasso.RequestCreator

# 墨 · 状态栏歌词
-keep class StatusBarLyric.API.StatusBarLyric { *; }

-printmapping ../mapping.txt