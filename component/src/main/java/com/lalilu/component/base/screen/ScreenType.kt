package com.lalilu.component.base.screen

sealed interface ScreenType {
    interface List : ScreenType
    interface Empty : ScreenType
}