package com.lalilu.lmedia.extension.sortable

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.lalilu.lmedia.repository.LMediaSp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform

interface SimplePreference<T> {
    fun get(): T? = null
    fun set(value: T) {}
}

class SortActionPreference(
    prefix: String,
    private val defaultAction: SortAction? = null
) : SimplePreference<SortAction> {
    private val lMediaSp: LMediaSp by KoinPlatform.getKoin().inject<LMediaSp>()
    private val spItem = lMediaSp.obtain<String>("${prefix}sort_action")

    override fun get(): SortAction? = runCatching {
        spItem.value.let { KoinPlatform.getKoin().getOrNull<SortAction>(named(it)) }
            ?: defaultAction
    }.getOrNull()

    override fun set(value: SortAction) {
        spItem.value = value.key() ?: ""
    }
}

class SortConfigPreference(
    prefix: String,
    private val defaultConfig: SortConfig? = null
) : SimplePreference<SortConfig> {
    private val lMediaSp: LMediaSp by KoinPlatform.getKoin().inject<LMediaSp>()
    private val json: Json by KoinPlatform.getKoin().inject<Json>()
    private val spItem = lMediaSp.obtain<String>("${prefix}sort_config")

    override fun get(): SortConfig? = spItem.value
        .runCatching { json.decodeFromString<SortConfig>(this) }
        .getOrNull()
        ?: defaultConfig

    override fun set(value: SortConfig) {
        spItem.value = json.encodeToString(value)
    }
}

class SortManager(
    val prefix: String = "sort_manager_",
    val supportedActions: Collection<SortAction>,
    val defaultAction: SortAction? = supportedActions.firstOrNull(),
    val defaultConfig: SortConfig? = SortConfig(),
    private val sortActionPf: SortActionPreference = SortActionPreference(prefix, defaultAction),
    private val sortConfigPf: SortConfigPreference = SortConfigPreference(prefix, defaultConfig),
) {
    val sortConfig = MutableStateFlow(
        sortConfigPf.get()
            ?: defaultConfig
            ?: SortConfig()
    )
    val selectedAction = MutableStateFlow(
        sortActionPf.get()
            ?.takeIf { it in supportedActions }
            ?: defaultAction
            ?: supportedActions.firstOrNull()
    )

    suspend fun setConfig(config: SortConfig) {
        sortConfig.emit(config)
        sortConfigPf.set(config)
    }

    suspend fun setAction(action: SortAction) {
        if (action in supportedActions) {
            selectedAction.emit(action)
            sortActionPf.set(action)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : Sortable> Flow<List<T>>.doSort(
    sortManager: SortManager,
): Flow<SortResult<T>> = sortManager.selectedAction.flatMapLatest { action ->
    sortManager.sortConfig.flatMapLatest { config ->
        action?.doSort(items = this@doSort, config)
            ?: this@doSort.mapLatest { SortResult.Flat(it) }
    }
}

inline fun <reified T : Sortable> Flow<List<T>>.doSortState(
    sortManager: SortManager,
    coroutineScope: CoroutineScope,
): State<SortResult<T>> = mutableStateOf(SortResult.empty<T>())
    .also { state ->
        this@doSortState.doSort(sortManager)
            .onEach { state.value = it }
            .launchIn(coroutineScope)
    }