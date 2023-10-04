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

# 基础依赖遵循尽可能减少、并且不进行混淆的原则
-keep,allowoptimization class kotlin.** { public protected *; }
-keep,allowoptimization class kotlinx.coroutines.** { public protected *; }
-keep,allowoptimization class androidx.lifecycle.** { public protected *; }
-keep,allowoptimization class androidx.compose.** { public protected *; }
-keep,allowoptimization class coil.compose.** { public protected *; }

# 插件依赖则固定不混淆
-keep class com.lalilu.extension_core.** { public protected *; }
-keep class * extends com.lalilu.extension_core.Extension { *; }
-keep class lalilu.extension_ksp.ExtensionsConstants { *;}

# 复用宿主的mapping，避免混淆后名称和宿主的某个类的名称冲突
-applymapping ../app/mapping.txt
-printmapping mapping.txt