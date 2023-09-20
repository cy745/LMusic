package com.lalilu.crash

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class StartUp : Initializer<Unit> {
    override fun create(context: Context) {
        CrashHelper.init(context.applicationContext as Application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}