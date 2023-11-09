package com.lalilu.register

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input

interface CollectParameter : InstrumentationParameters {
    @get:Input
    val registerMap: MapProperty<String, CollectSettings>
}