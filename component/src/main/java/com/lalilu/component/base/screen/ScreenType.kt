package com.lalilu.component.base.screen

sealed interface ScreenType {
    interface ListHost : ScreenType
    interface Empty : ScreenType
    interface List : ScreenType
    interface Detail : ScreenType
}