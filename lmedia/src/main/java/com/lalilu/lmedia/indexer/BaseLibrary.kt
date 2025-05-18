package com.lalilu.lmedia.indexer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lalilu.lmedia.entity.Item
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LFolder
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.markBlockedByChildren
import com.lalilu.lmedia.entity.resetBlockState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus

sealed interface LibraryState {
    data object Idle : LibraryState
    data object Loading : LibraryState
    data class Ready(val readyTime: Long = System.currentTimeMillis()) : LibraryState

    interface Error : LibraryState
    data class NotGranted(val throwable: Throwable) : Error
    data class Unknown(val throwable: Throwable) : Error
}

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseLibrary {
    val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private var readyCallbacks = listOf<() -> Unit>()
    var libraryState: LibraryState by mutableStateOf(LibraryState.Idle)
        private set

    fun whenReady(callback: () -> Unit) {
        if (libraryState is LibraryState.Ready) {
            callback()
        } else {
            readyCallbacks += callback
        }
    }

    internal fun updateState(state: LibraryState) {
        when (state) {
            is LibraryState.Error -> {
                libraryState = state
            }

            is LibraryState.Idle -> {
                libraryState = state
            }

            is LibraryState.Loading -> {
                libraryState = state
            }

            is LibraryState.Ready -> {
                if (libraryState is LibraryState.Ready) return
                libraryState = state
                readyCallbacks.forEach { it.invoke() }
                readyCallbacks = emptyList()
            }
        }
    }

    private val _songsFlow = MutableStateFlow<Map<String, LSong>>(emptyMap())
    private val _albumsFlow = MutableStateFlow<Map<String, LAlbum>>(emptyMap())
    private val _artistsFlow = MutableStateFlow<Map<String, LArtist>>(emptyMap())
    private val _genresFlow = MutableStateFlow<Map<String, LGenre>>(emptyMap())
    private val _foldersFlow = MutableStateFlow<Map<String, LFolder>>(emptyMap())

    suspend inline fun <reified T : Item> set(data: Map<String, T>) {
        getSourceFlowByClass(T::class.java)?.emit(data)
    }

    fun <T : Item> getSourceFlowByClass(clazz: Class<T>): MutableStateFlow<Map<String, T>>? {
        return when (clazz) {
            LSong::class.java -> _songsFlow
            LArtist::class.java -> _artistsFlow
            LAlbum::class.java -> _albumsFlow
            LGenre::class.java -> _genresFlow
            LFolder::class.java -> _foldersFlow
            else -> null
        } as MutableStateFlow<Map<String, T>>?
    }

    fun <T : Item> getResultFlowByClass(clazz: Class<T>): Flow<Map<String, T>> =
        getSourceFlowByClass(clazz) ?: flowOf(emptyMap())

    inline fun <reified T : Item> get(
        id: String?,
        blockFilter: Boolean = true,
    ): T? = getSourceFlowByClass(T::class.java)
        ?.value?.let { it[id] }
        ?.blockFilter(blockFilter)

    inline fun <reified T : Item> get(
        blockFilter: Boolean = true,
    ): List<T> = getSourceFlowByClass(T::class.java)
        ?.value?.values
        ?.blockFilter(blockFilter)
        ?: emptyList()

    inline fun <reified T : Item> getFlow(
        id: String?,
        blockFilter: Boolean = true,
    ): SharedFlow<T?> = getResultFlowByClass(T::class.java)
        .mapLatest { it[id]?.blockFilter(blockFilter) }
        .shareIn(coroutineScope, SharingStarted.Eagerly, 1)

    inline fun <reified T : Item> getFlow(
        blockFilter: Boolean = true,
    ): SharedFlow<List<T>> = getResultFlowByClass(T::class.java)
        .mapLatest { it.values.blockFilter(blockFilter) }
        .shareIn(coroutineScope, SharingStarted.Eagerly, 1)

    inline fun <reified T : Item> getCount(
        blockFilter: Boolean = true,
    ): Int = getSourceFlowByClass(T::class.java)
        ?.value?.values
        ?.blockFilter(blockFilter)
        ?.count() ?: 0

    inline fun <reified T : Item> flowMapBy(
        ids: List<String>,
        blockFilter: Boolean = true,
    ): Flow<List<T>> = getResultFlowByClass(T::class.java)
        .mapLatest { map ->
            ids.mapNotNull { map[it] }
                .blockFilter(blockFilter)
        }

    inline fun <reified T : Item> mapBy(
        ids: List<String>,
        blockFilter: Boolean = true,
    ): List<T> = getSourceFlowByClass(T::class.java)
        ?.value?.let { map -> ids.mapNotNull { map[it] } }
        ?.blockFilter(blockFilter)
        ?: emptyList()

    fun resetBlockState() {
        _songsFlow.value.values.resetBlockState()
        _artistsFlow.value.values.resetBlockState()
        _albumsFlow.value.values.resetBlockState()
        _genresFlow.value.values.resetBlockState()
        _foldersFlow.value.values.resetBlockState()
    }

    fun markBlockByChildren() {
        _artistsFlow.value.values.markBlockedByChildren()
        _albumsFlow.value.values.markBlockedByChildren()
        _genresFlow.value.values.markBlockedByChildren()
        _foldersFlow.value.values.markBlockedByChildren()
    }

    fun <T : Item> Collection<T>.blockFilter(enable: Boolean): List<T> =
        if (!enable) this.toList() else filter { !it.blocked }

    fun <T : Item> T?.blockFilter(enable: Boolean): T? =
        if (!enable) this else this?.takeIf { !it.blocked }
}