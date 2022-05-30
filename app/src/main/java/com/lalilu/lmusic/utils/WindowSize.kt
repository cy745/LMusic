/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lalilu.lmusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.lalilu.common.DeviceUtils

/**
 * Opinionated set of viewport breakpoints
 *     - Compact: Most phones in portrait mode
 *     - Medium: Most foldables and tablets in portrait mode
 *     - Expanded: Most tablets in landscape mode
 *
 * More info: https://material.io/archive/guidelines/layout/responsive-ui.html
 */
enum class WindowSize { Compact, Medium, Expanded }
enum class DeviceType { Phone, Pad }

data class WindowSizeClass(
    var windowSize: WindowSize,
    var deviceType: DeviceType
) {
    companion object {
        @Volatile
        var instance: WindowSizeClass? = null
            get() = field ?: synchronized(WindowSizeClass::class) {
                field ?: WindowSizeClass(
                    windowSize = WindowSize.Compact,
                    deviceType = DeviceType.Phone
                ).also { field = it }
            }
    }
}


@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    remember(LocalConfiguration.current) { 0 }
    val dpSize = LocalDensity.current.run {
        DeviceUtils.getMetricsRect(LocalContext.current)
            .toComposeRect().size.toDpSize()
    }
    return WindowSizeClass(getWindowSize(dpSize), getDeviceType(dpSize)).also {
        if (WindowSizeClass.instance?.windowSize != it.windowSize || WindowSizeClass.instance?.deviceType != it.deviceType) {
            WindowSizeClass.instance = it
        }
    }
}

fun getWindowSize(windowDpSize: DpSize): WindowSize = when {
    windowDpSize.width < 0.dp -> throw IllegalArgumentException("Dp value cannot be negative")
    windowDpSize.width < 600.dp -> WindowSize.Compact
    windowDpSize.width < 840.dp -> WindowSize.Medium
    else -> WindowSize.Expanded
}

fun getDeviceType(windowDpSize: DpSize): DeviceType = when {
    windowDpSize.width < 0.dp -> throw IllegalArgumentException("Dp value cannot be negative")
    minOf(windowDpSize.width, windowDpSize.height) < 600.dp -> DeviceType.Phone
    else -> DeviceType.Pad
}
