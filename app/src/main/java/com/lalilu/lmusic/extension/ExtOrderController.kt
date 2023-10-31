package com.lalilu.lmusic.extension

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lalilu.common.base.BaseSp
import com.lalilu.extension_core.Content
import com.lalilu.extension_core.Ext
import com.lalilu.extension_core.Extension
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject

@Ext
class ExtOrderController : Extension {
    override fun getContentMap(): Map<String, @Composable (Map<String, String>) -> Unit> = mapOf(
        Content.COMPONENT_CATEGORY to { HomeEntry() },
        Content.COMPONENT_MAIN to { Content() },
    )

    @Composable
    fun HomeEntry() {
        Row { Text(text = "调整顺序") }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Content(
        vm: OrderViewModel = viewModel(viewModelStoreOwner = koinInject()),
    ) {
        val extensionResult by vm.extensionResult

        val state = rememberReorderableLazyListState(onMove = { from, to ->
            vm.onMoveData(from.index, to.index)
        })

        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state),
            contentPadding = PaddingValues(vertical = 100.dp),
        ) {
            items(
                items = extensionResult,
                key = { it.className },
                contentType = { it.packageInfo }
            ) {
                ReorderableItem(
                    defaultDraggingModifier = Modifier.animateItemPlacement(),
                    state = state,
                    key = it.className
                ) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colors.surface)
                    ) {
                        Text(
                            text = it.className,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
            }
        }
    }

    object OrderSp : BaseSp() {
        private val context: Application by inject(Application::class.java)
        override fun obtainSourceSp(): SharedPreferences {
            return context.getSharedPreferences("OrderSp", Application.MODE_PRIVATE)
        }

        val orderList = obtainList<String>("OrderList")
    }

    class OrderViewModel : ViewModel() {
        val data = OrderSp.orderList
        val extensionResult = ExtensionManager
            .requireExtensionByContentKey(contentKey = Content.COMPONENT_HOME)
            .combine(data.flow(true)) { extensions, orderList ->
                extensions.sortedBy {
                    orderList?.indexOf(it.className)?.takeIf { it != -1 } ?: Int.MAX_VALUE
                }
            }
            .toState(emptyList(), viewModelScope)

        fun onMoveData(from: Int, to: Int) = viewModelScope.launch {
            val newList = extensionResult.value.map { it.className }.toMutableList()
                .apply { add(to, removeAt(from)) }
            data.value = newList
        }
    }
}