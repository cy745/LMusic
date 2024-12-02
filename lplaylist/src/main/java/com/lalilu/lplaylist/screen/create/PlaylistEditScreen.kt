package com.lalilu.lplaylist.screen.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.RemixIcon
import com.lalilu.component.LongClickableTextButton
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.registerAndGetViewModel
import com.lalilu.lplaylist.viewmodel.PlaylistEditAction
import com.lalilu.lplaylist.viewmodel.PlaylistEditVM
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.design.editBoxFill
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
        val vm = registerAndGetViewModel<PlaylistEditVM>(
            parameters = { parametersOf(playlistId) }
        )

        return remember {
            listOfNotNull(
                ScreenAction.Dynamic {
                    val color = Color(0xFF0074FF)

                    LongClickableTextButton(
                        modifier = Modifier.fillMaxHeight(),
                        shape = RectangleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = color.copy(alpha = 0.15f),
                            contentColor = color
                        ),
                        enableLongClickMask = true,
                        onLongClick = { vm.intent(PlaylistEditAction.Confirm) },
                        onClick = { ToastUtils.showShort("请长按此按钮以继续") },
                    ) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            imageVector = RemixIcon.Design.editBoxFill,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (vm.playlist.value == null) "创建歌单" else "更新歌单",
                            fontSize = 14.sp
                        )
                    }
                }
            )
        }
    }

    @Composable
    override fun Content() {
        val vm = registerAndGetViewModel<PlaylistEditVM>(
            parameters = { parametersOf(playlistId) }
        )

        PlaylistEditScreenContent(
            titleValue = { vm.titleState.value },
            onUpdateTitle = { vm.titleState.value = it },
            subTitleValue = { vm.subTitleState.value },
            onUpdateSubTitle = { vm.subTitleState.value = it }
        )
    }
}