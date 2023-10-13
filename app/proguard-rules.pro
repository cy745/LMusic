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

-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

-dontwarn org.jetbrains.kotlin.compiler.**
-dontwarn org.jetbrains.kotlin.diagnostics.**

# retrofit 使用的所有bean都不可以混淆
-keep  class com.lalilu.lmusic.apis.bean.** { *; }

-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

-keep class com.simple.spiderman.** { *; }
-keepnames class com.simple.spiderman.** { *; }
-keep public class * extends android.app.Activity
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# support
-keep public class * extends android.support.annotation.** { *; }
# androidx
-keep public class * extends androidx.annotation.** { *; }
-keep public class * extends androidx.core.content.FileProvider

# 墨 · 状态栏歌词
-keep class StatusBarLyric.API.StatusBarLyric { *; }

# 为插件保留相应的依赖环境（既不混淆也不去除未使用的类）
-keep class kotlin.** { public protected *; }
-keep class kotlinx.coroutines.** { public protected *; }
-keep class androidx.lifecycle.** { public protected *; }
-keep class androidx.compose.** { public protected *; }
-keep class coil.compose.** { public protected *; }
-keep class com.lalilu.extension_core.** { public protected *; }
-keep class com.lalilu.common.** { public protected *; }
-keep class com.lalilu.lplayer.** { public protected *; }
-keep class android.support.v4.media.** { public protected *; }

-keepclassmembers class * implements com.lalilu.extension_core.Extension {
    <init>(...);
    com.lalilu.extension_core.Extension *;
}
-keep class lalilu.extension_ksp.ExtensionsConstants { *;}

-printmapping mapping.txt