package com.lalilu.value_cat

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.startup.Initializer
import com.hjq.window.EasyWindow
import com.hjq.window.WindowLayout


class StartUp : Initializer<Unit>, Application.ActivityLifecycleCallbacks {

    override fun create(context: Context) {
        val application = context as Application

        application.registerActivityLifecycleCallbacks(this)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val viewTreeLifecycleOwner = activity as? LifecycleOwner ?: return
        val savedStateRegistryOwner = activity as? SavedStateRegistryOwner ?: return

        val mDecorView = WindowLayout(activity)
        val composeView = ComposeView(activity)

        composeView.setContent(ValueCat.content)

        mDecorView.setViewTreeLifecycleOwner(viewTreeLifecycleOwner)
        mDecorView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)

        EasyWindow.with(activity)
            .setDecorView(mDecorView)
            .setContentView(composeView)
            .setDraggable()
            .show()
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }
}

