package com.lalilu.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun Navigator.previousScreen(): State<Screen?> {
    return remember(this) {
        derivedStateOf { items.getOrNull(items.size - 2) }
    }
}
