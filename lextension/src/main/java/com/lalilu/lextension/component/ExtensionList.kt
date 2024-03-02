package com.lalilu.lextension.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.navigation.BackHandler
import com.lalilu.extension_core.Content
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.extension_core.Place
import com.lalilu.lextension.repository.ExtensionSp
import kotlinx.coroutines.flow.combine
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState


class ExtensionsScreenModel(val extensionSp: ExtensionSp) : ScreenModel {
    val isEditing = mutableStateOf(false)

    val extensions = extensionSp.orderList.flow(true)
        .combine(ExtensionManager.extensionsFlow) { orderList, extensions ->
            orderList?.mapNotNull { order -> extensions.firstOrNull { it.extId == order } }
                ?: emptyList()
        }

    fun requireExtensions() {
        extensionSp.orderList.value = ExtensionManager.extensionsFlow.value.map { it.extId }
        extensionSp.orderList.save()
    }

    fun onMove(from: LazyListItemInfo, to: LazyListItemInfo) {
        extensionSp.orderList.value = extensionSp.orderList.value.toMutableList().apply {
            val toIndex = indexOfFirst { it == to.key }
            val fromIndex = indexOfFirst { it == from.key }
            if (toIndex < 0 || fromIndex < 0) return

            add(toIndex, removeAt(fromIndex))
        }
        extensionSp.orderList.save()
    }

    /**
     * 将某插件的顺序前移
     *
     * [extId] 插件ID
     */
    fun onOrderUp(extId: String) {
        extensionSp.orderList.value = extensionSp.orderList.value.toMutableList().apply {
            val itemIndex = indexOfFirst { it == extId }
            if (itemIndex < 0) return

            val targetIndex = itemIndex - 1
            if (targetIndex < 0) return

            add(targetIndex, removeAt(itemIndex))
        }
        extensionSp.orderList.save()
    }

    /**
     * 将某插件的顺序后移
     *
     * [extId] 插件ID
     */
    fun onOrderDown(extId: String) {
        extensionSp.orderList.value = extensionSp.orderList.value.toMutableList().apply {
            val itemIndex = indexOfFirst { it == extId }
            if (itemIndex < 0) return

            val targetIndex = itemIndex + 1
            if (targetIndex < 0 || targetIndex >= this.size) return

            add(targetIndex, removeAt(itemIndex))
        }
        extensionSp.orderList.save()
    }

    fun isVisible(extId: String): Boolean {
        return !extensionSp.hidingList.value.contains(extId)
    }

    fun onVisibleChange(extId: String, visible: Boolean) {
        extensionSp.hidingList.value = extensionSp.hidingList.value.toMutableList().apply {
            if (visible) {
                removeAll { it == extId }
                return@apply
            }

            if (!contains(extId)) {
                add(extId)
            }
        }
        extensionSp.hidingList.save()
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Screen.ExtensionList(
    extensionsSM: ExtensionsScreenModel = getScreenModel(),
    headerContent: LazyListScope.(List<ExtensionLoadResult>) -> Unit = {},
    footerContent: LazyListScope.(List<ExtensionLoadResult>) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val orderList = extensionsSM.extensionSp.orderList

    val extensionsState = extensionsSM.extensions.collectAsLoadingState()
    val reorderableState = rememberReorderableLazyColumnState(
        lazyListState = listState,
        onMove = extensionsSM::onMove
    )

    LoadingScaffold(
        modifier = Modifier.fillMaxSize(),
        targetState = extensionsState
    ) { extensions ->
        LaunchedEffect(Unit) {
            if (orderList.value.isEmpty()) {
                extensionsSM.requireExtensions()
            }
        }

        LLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            headerContent(extensions)

            items(
                items = extensions,
                key = { it.extId },
                contentType = { ExtensionLoadResult::class.java }
            ) { extension ->
                ReorderableItem(
                    reorderableLazyListState = reorderableState,
                    key = extension.extId
                ) { isDragging ->
                    ExtensionCard(
                        onVisibleChange = { extensionsSM.onVisibleChange(extension.extId, it) },
                        onUpClick = { extensionsSM.onOrderUp(extension.extId) },
                        onDownClick = { extensionsSM.onOrderDown(extension.extId) },
                        isVisible = { extensionsSM.isVisible(extension.extId) },
                        isEditing = { extensionsSM.isEditing.value },
                        isDragging = { isDragging },
                        draggableModifier = Modifier.draggableHandle()
                    ) {
                        extension.Place(contentKey = Content.COMPONENT_HOME)
                    }
                }
            }

            footerContent(extensions)
        }

        if (extensionsSM.isEditing.value) {
            BackHandler {
                extensionsSM.isEditing.value = false
            }
        }
    }
}