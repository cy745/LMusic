package com.lalilu.lalbum.screen.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lalbum.R
import com.lalilu.lalbum.viewModel.AlbumsSM
import com.lalilu.lalbum.viewModel.AlbumsScreenAction
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.System
import com.lalilu.remixicon.design.editBoxLine
import com.lalilu.remixicon.design.focus3Line
import com.lalilu.remixicon.editor.formatClear
import com.lalilu.remixicon.editor.sortDesc
import com.lalilu.remixicon.editor.text
import com.lalilu.remixicon.media.albumFill
import com.lalilu.remixicon.system.menuSearchLine
import com.zhangke.krouter.annotation.Destination

@Destination("/pages/albums")
data class AlbumsScreen(
    val albumsIds: List<String> = emptyList()
) : Screen, ScreenInfoFactory, ScreenActionFactory {

    private var albumsSM: AlbumsSM? = null

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.album_screen_title) },
            icon = RemixIcon.Media.albumFill
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> = remember {
        listOf(
            ScreenAction.Static(
                title = { "显示标题" },
                icon = { if (albumsSM?.showTitle?.value == true) RemixIcon.Editor.text else RemixIcon.Editor.formatClear },
                color = { Color(0xFFFFB217) },
                onAction = { albumsSM?.showTitle?.value = !(albumsSM?.showTitle?.value ?: false) }
            ),
            ScreenAction.Static(
                title = { "排序" },
                icon = { RemixIcon.Editor.sortDesc },
                color = { Color(0xFF1793FF) },
                onAction = { albumsSM?.showSortPanel?.value = true }
            ),
            ScreenAction.Static(
                title = { "选择" },
                icon = { RemixIcon.Design.editBoxLine },
                color = { Color(0xFF009673) },
                onAction = {
//                    albumsSM?.selector?.isSelecting?.value = true
                }
            ),
            ScreenAction.Static(
                title = { "搜索" },
                subTitle = {
                    val isSearching = albumsSM?.searcher?.isSearching

                    if (isSearching?.value == true) "搜索中： ${albumsSM?.searcher?.keywordState?.value}"
                    else null
                },
                icon = { RemixIcon.System.menuSearchLine },
                color = { Color(0xFF8BC34A) },
                dotColor = {
                    val isSearching = albumsSM?.searcher?.isSearching

                    if (isSearching?.value == true) Color.Red
                    else null
                },
                onAction = {
                    albumsSM?.showSearcherPanel?.value = true
                    DialogWrapper.dismiss()
                }
            ),
            ScreenAction.Static(
                title = { "定位当前播放所属" },
                icon = { RemixIcon.Design.focus3Line },
                color = { Color(0xFF8700FF) },
                onAction = { albumsSM?.doAction(AlbumsScreenAction.LocaleToPlayingItem) }
            ),
        )
    }

    @Composable
    override fun Content() {
        val sm = rememberScreenModel { AlbumsSM(albumsIds) }
            .also { albumsSM = it }

        AlbumsScreenContent(
            albumsSM = sm,
        )
    }
}