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
#-keep class * { @androidx.annotation.Keep <fields>; }
-keepnames class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut
-keepnames class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod
-keep class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
    public <init>();
}
-keep class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
    public <init>();
}

# AndroidFilePicker相关
-dontwarn com.bumptech.glide.Glide
-dontwarn com.bumptech.glide.RequestBuilder
-dontwarn com.bumptech.glide.RequestManager
-dontwarn com.bumptech.glide.request.BaseRequestOptions
-dontwarn com.bumptech.glide.request.target.ViewTarget
-dontwarn com.squareup.picasso.Picasso
-dontwarn com.squareup.picasso.RequestCreator

# 针对KRouter，保留所需的类的构造函数
-keepclassmembers @com.zhangke.krouter.annotation.Destination public class * { public <init>(*); }

# 墨 · 状态栏歌词
-keep class StatusBarLyric.API.StatusBarLyric { *; }

-printmapping ../mapping.txt

-dontwarn org.gradle.api.Action
-dontwarn org.gradle.api.Named
-dontwarn org.gradle.api.Plugin
-dontwarn org.gradle.api.Task
-dontwarn org.gradle.api.artifacts.Dependency
-dontwarn org.gradle.api.artifacts.ExternalModuleDependency
-dontwarn org.gradle.api.attributes.Attribute
-dontwarn org.gradle.api.attributes.AttributeCompatibilityRule
-dontwarn org.gradle.api.attributes.AttributeContainer
-dontwarn org.gradle.api.attributes.AttributeDisambiguationRule
-dontwarn org.gradle.api.attributes.HasAttributes
-dontwarn org.gradle.api.component.SoftwareComponent
-dontwarn org.gradle.api.plugins.ExtensionAware
-dontwarn org.gradle.api.tasks.util.PatternFilterable


-dontwarn coil3.PlatformContext
-dontwarn libcore.icu.NativePluralRules
-dontwarn org.jetbrains.kotlin.library.BaseKotlinLibrary
-dontwarn org.jetbrains.kotlin.library.BaseWriter
-dontwarn org.jetbrains.kotlin.library.IrKotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.IrLibrary
-dontwarn org.jetbrains.kotlin.library.IrWriter
-dontwarn org.jetbrains.kotlin.library.KotlinLibrary
-dontwarn org.jetbrains.kotlin.library.KotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.KotlinLibraryProperResolverWithAttributes
-dontwarn org.jetbrains.kotlin.library.MetadataKotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.MetadataLibrary
-dontwarn org.jetbrains.kotlin.library.MetadataWriter
-dontwarn org.jetbrains.kotlin.library.SearchPathResolver
-dontwarn org.jetbrains.kotlin.library.impl.BaseLibraryAccess
-dontwarn org.jetbrains.kotlin.library.impl.ExtractingKotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.impl.FromZipBaseLibraryImpl
-dontwarn org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutForWriter
-dontwarn org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
-dontwarn org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImplKt