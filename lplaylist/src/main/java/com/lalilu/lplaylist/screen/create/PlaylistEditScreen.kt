package com.lalilu.lplaylist.screen.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.screenVM
import com.lalilu.lplaylist.viewmodel.PlaylistEditAction
import com.lalilu.lplaylist.viewmodel.PlaylistEditVM
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.System
import com.lalilu.remixicon.design.editBoxFill
import com.lalilu.remixicon.system.deleteBinLine
import com.zhangke.krouter.annotation.Destination
import org.koin.core.parameter.parametersOf


/**
 * [playlistId]   目标操作歌单的Id
 */
@Destination("/pages/playlist/edit")
data class PlaylistEditScreen(
    private val playlistId: String? = null
) : Screen, ScreenInfoFactory, ScreenActionFactory {
    override val key: ScreenKey = playlistId.toString()

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { "歌单创建编辑页" },
            icon = RemixIcon.Design.editBoxFill
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val vm = screenVM<PlaylistEditVM>(
            parameters = { parametersOf(playlistId) }
        )

        return remember {
            listOfNotNull(
                if (vm.playlist.value != null) {
                    ScreenAction.Static(
                        title = { "删除歌单" },
                        icon = { RemixIcon.System.deleteBinLine },
                        longClick = { true },
                        color = { Color(0xFFF5381D) },
                        onAction = { vm.intent(PlaylistEditAction.Delete) }
                    )
                } else null,
                ScreenAction.Static(
                    title = { if (vm.playlist.value == null) "创建歌单" else "更新歌单" },
                    icon = { RemixIcon.Design.editBoxFill },
                    longClick = { true },
                    color = { Color(0xFF0074FF) },
                    onAction = { vm.intent(PlaylistEditAction.Confirm) }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val vm = screenVM<PlaylistEditVM>(
            parameters = { parametersOf(playlistId) }
        )

        PlaylistEditScreenContent(
            isEditing = { vm.playlist.value != null },
            titleHint = { vm.playlist.value?.title ?: "" },
            subTitleHint = { vm.playlist.value?.subTitle ?: "" },
            titleValue = { vm.titleState.value },
            onUpdateTitle = { vm.titleState.value = it },
            subTitleValue = { vm.subTitleState.value },
            onUpdateSubTitle = { vm.subTitleState.value = it }
        )
    }
}