package com.lalilu.lextension.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.extension_core.Content
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.extension_core.Place
import com.lalilu.lextension.R
import com.lalilu.lextension.component.ExtensionCard
import com.lalilu.lextension.repository.ExtensionSp
import kotlinx.coroutines.flow.combine
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.lalilu.component.R as ComponentR

object ExtensionsScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.extension_screen_title,
        icon = ComponentR.drawable.ic_shapes_line
    )

    @Composable
    override fun Content() {
        val extensionsSM = getScreenModel<ExtensionsScreenModel>()

        RegisterActions {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.extension_screen_title,
                    icon = ComponentR.drawable.ic_shapes_line,
                    color = Color(0xFF1B7E00)
                ) {
                    extensionsSM.isEditing.value = !extensionsSM.isEditing.value
                }
            )
        }

        ExtensionsScreen(extensionsSM = extensionsSM)
    }
}

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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DynamicScreen.ExtensionsScreen(
    extensionsSM: ExtensionsScreenModel,
    playingVM: IPlayingViewModel = koinInject()
) {
    val navigator = koinInject<GlobalNavigator>()
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
            item {
                NavigatorHeader(
                    modifier = Modifier.statusBarsPadding(),
                    title = stringResource(id = R.string.extension_screen_title),
                    subTitle = "共 ${extensions.size} 个扩展"
                )
            }

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
                        onUpClick = { extensionsSM.onOrderUp(extension.extId) },
                        onDownClick = { extensionsSM.onOrderDown(extension.extId) },
                        isEditing = { extensionsSM.isEditing.value },
                        isDragging = { isDragging },
                        draggableModifier = Modifier.draggableHandle()
                    ) {
                        extension.Place(contentKey = Content.COMPONENT_HOME)
                    }
                }
            }
        }
    }
}