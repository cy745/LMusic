package com.lalilu.lextension.repository

import android.app.Application
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp

class ExtensionSp(private val context: Application) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences("EXTENSIONS", Application.MODE_PRIVATE)
    }

    val orderList = obtainList<String>("ORDER_LIST")
}