package com.lalilu.lmusic.aop

import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

//@AndroidAopReplaceClass("android.util.Log")
class LogOverride {

    companion object {
        const val isEnableOriginOutput = false

        @AndroidAopReplaceMethod("int v(java.lang.String,java.lang.String)")
        @JvmStatic
        fun v(tag: String, msg: String): Int {
            LogUtils.vTag(tag, msg)
            return if (isEnableOriginOutput) Log.v(tag, msg) else 0
        }

        @AndroidAopReplaceMethod("int v(java.lang.String,java.lang.String,java.lang.Throwable)")
        @JvmStatic
        fun v(tag: String, msg: String, tr: Throwable): Int {
            LogUtils.vTag(tag, msg, tr)
            return if (isEnableOriginOutput) Log.v(tag, msg, tr) else 0
        }

        @AndroidAopReplaceMethod("int d(java.lang.String,java.lang.String)")
        @JvmStatic
        fun d(tag: String, msg: String): Int {
            LogUtils.dTag(tag, msg)
            return if (isEnableOriginOutput) Log.d(tag, msg) else 0
        }

        @AndroidAopReplaceMethod("int d(java.lang.String,java.lang.String,java.lang.Throwable)")
        @JvmStatic
        fun d(tag: String, msg: String, tr: Throwable): Int {
            LogUtils.dTag(tag, msg, tr)
            return if (isEnableOriginOutput) Log.d(tag, msg, tr) else 0
        }

        @AndroidAopReplaceMethod("int i(java.lang.String,java.lang.String)")
        @JvmStatic
        fun i(tag: String, msg: String): Int {
            LogUtils.iTag(tag, msg)
            return if (isEnableOriginOutput) Log.i(tag, msg) else 0
        }

        @AndroidAopReplaceMethod("int i(java.lang.String,java.lang.String,java.lang.Throwable)")
        @JvmStatic
        fun i(tag: String, msg: String, tr: Throwable): Int {
            LogUtils.iTag(tag, msg, tr)
            return if (isEnableOriginOutput) Log.i(tag, msg, tr) else 0
        }

        @AndroidAopReplaceMethod("int w(java.lang.String,java.lang.String)")
        @JvmStatic
        fun w(tag: String, msg: String): Int {
            LogUtils.wTag(tag, msg)
            return if (isEnableOriginOutput) Log.w(tag, msg) else 0
        }

        @AndroidAopReplaceMethod("int w(java.lang.String,java.lang.String,java.lang.Throwable)")
        @JvmStatic
        fun w(tag: String, msg: String, tr: Throwable): Int {
            LogUtils.wTag(tag, msg, tr)
            return if (isEnableOriginOutput) Log.w(tag, msg, tr) else 0
        }

        @AndroidAopReplaceMethod("int w(java.lang.String,java.lang.Throwable)")
        @JvmStatic
        fun w(tag: String, tr: Throwable): Int {
            LogUtils.wTag(tag, tr)
            return if (isEnableOriginOutput) Log.w(tag, tr) else 0
        }

        @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
        @JvmStatic
        fun e(tag: String, msg: String): Int {
            LogUtils.eTag(tag, msg)
            return if (isEnableOriginOutput) Log.e(tag, msg) else 0
        }

        @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String,java.lang.Throwable)")
        @JvmStatic
        fun e(tag: String, msg: String, tr: Throwable): Int {
            LogUtils.eTag(tag, msg, tr)
            return if (isEnableOriginOutput) Log.e(tag, msg, tr) else 0
        }
    }
}