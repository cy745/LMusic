package com.lalilu.component


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.koin.compose.currentKoinScope
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

fun interface SlotState<T : Any?> {
    @Composable
    fun state(): State<T>
}

@Composable
fun <T : Any?> state(key: String, defaultValue: T): State<T> {
    val state = koinInjectOrNull<SlotState<T>>(qualifier = named(key))
    return state?.state() ?: remember { mutableStateOf(defaultValue) }
}

@Composable
private inline fun <reified T> koinInjectOrNull(
    qualifier: Qualifier? = null,
    scope: Scope = currentKoinScope(),
): T? {
    return remember(qualifier, scope) {
        runCatching {
            scope.getOrNull<T>(T::class, qualifier)
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }
}