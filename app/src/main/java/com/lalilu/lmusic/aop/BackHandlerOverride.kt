package com.lalilu.lmusic.aop

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.lalilu.component.base.LocalBottomSheetNavigator

/**
 * 全局替换所有的BackHandler
 */
@AndroidAopReplaceClass(
    value = "androidx.activity.compose.BackHandlerKt",
    type = MatchType.SELF
)
object BackHandlerOverride {

    @JvmStatic
    @Composable
    @AndroidAopReplaceMethod(
        value = "void BackHandler(boolean, kotlin.jvm.functions.Function0, androidx.compose.runtime.Composer, int, int)"
    )
    fun BackHandlerOverride(enabled: Boolean = true, onBack: () -> Unit) {
        val sheetNavigator = LocalBottomSheetNavigator.current
        // 若可获取到SheetNavigator，则说明其处于BottomSheet内，则为其关联isVisible控制
        if (sheetNavigator == null) {
            BackHandler(enabled, onBack)
            return
        }

        BackHandler(
            enabled = sheetNavigator.isVisible && enabled,
            onBack = onBack
        )
    }
}