package com.lalilu.preference

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

@SuppressLint("RestrictedApi")
class UnPressableLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.preference.internal.PreferenceImageView(context, attrs)