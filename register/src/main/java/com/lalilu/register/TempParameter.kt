package com.lalilu.register

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface TempParameter : InstrumentationParameters {
    @get:Input
    val temp: Property<Long>
}