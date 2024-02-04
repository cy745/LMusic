package com.lalilu.lplaylist.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.toCachedFlow
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DialogScreen
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.component.extension.toMutableState
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import java.util.UUID
import com.lalilu.component.R as componentR

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistCreateOrEditScreenModel(
    private val navigator: GlobalNavigator,
    private val playlistRepo: PlaylistRepository
) : ScreenModel {
    private val playlistId = MutableStateFlow("")
    private val playlist = playlistId
        .combine(playlistRepo.getPlaylistsFlow()) { playlistId, playlists ->
            playlists.firstOrNull { it.id == playlistId }
        }.toCachedFlow()

    val title = playlist.mapLatest { it?.title ?: "" }
        .toMutableState(defaultValue = "", scope = screenModelScope)
    val subTitle = playlist.mapLatest { it?.subTitle ?: "" }
        .toMutableState(defaultValue = "", scope = screenModelScope)

    val createPlaylistAction = ScreenAction.StaticAction(
        title = R.string.playlist_action_create_playlist,
        icon = componentR.drawable.ic_check_line,
        isLongClickAction = true,
        fitImePadding = true,
        color = Color(0xFF008521)
    ) {
        val title = title.value
        val subTitle = subTitle.value

        if (title.isBlank()) {
            ToastUtils.showShort("歌单名称不可为空")
        } else {
            playlistRepo.save(
                LPlaylist(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    subTitle = subTitle,
                    coverUri = "",
                    mediaIds = emptyList()
                )
            )
            navigator.goBack()
        }
    }

    val updatePlaylistAction = ScreenAction.StaticAction(
        title = R.string.playlist_action_update_playlist,
        icon = componentR.drawable.ic_check_line,
        isLongClickAction = true,
        fitImePadding = true,
        color = Color(0xFF008521)
    ) {
        val playlistId = playlistId.value
        val title = title.value
        val subTitle = subTitle.value

        if (title.isBlank()) {
            ToastUtils.showShort("歌单名称不可为空")
        } else {
            val playlist = playlist.get()

            playlistRepo.save(
                LPlaylist(
                    id = playlistId,
                    title = title,
                    subTitle = subTitle,
                    coverUri = playlist?.coverUri ?: "",
                    mediaIds = playlist?.mediaIds ?: emptyList()
                )
            )
            navigator.goBack()
        }
    }

    fun updateTargetPlaylistId(playlistId: String) {
        this.playlistId.tryEmit(playlistId)
    }
}

/**
 * [targetPlaylistId]   目标操作歌单的Id
 */
data class PlaylistCreateOrEditScreen(
    private val targetPlaylistId: String? = null
) : DynamicScreen(), DialogScreen {
    override val key: ScreenKey = targetPlaylistId.toString()

    @Composable
    override fun Content() {
        val createOrEditSM: PlaylistCreateOrEditScreenModel = getScreenModel()

        if (targetPlaylistId != null) {
            LaunchedEffect(Unit) {
                createOrEditSM.updateTargetPlaylistId(playlistId = targetPlaylistId)
            }
        }

        RegisterActions {
            listOf(
                if (targetPlaylistId == null) createOrEditSM.createPlaylistAction
                else createOrEditSM.updatePlaylistAction
            )
        }

        PlaylistCreateOrEditScreen(
            targetPlaylistId = targetPlaylistId,
            createOrEditSM = createOrEditSM
        )
    }
}

@Composable
private fun DynamicScreen.PlaylistCreateOrEditScreen(
    targetPlaylistId: String?,
    createOrEditSM: PlaylistCreateOrEditScreenModel
) {
    val state = rememberLazyListState()
    val scrollToHelper = rememberLazyListScrollToHelper(listState = state)

    val onFocusCallback: (String) -> Unit = remember {
        {
//            scrollToHelper.scrollToItem(
//                key = it,
//                animateTo = true,
//                scrollOffset = -300,
//                delay = 100L
//            )
        }
    }

    val headerTitleRes = remember(targetPlaylistId) {
        if (targetPlaylistId == null) R.string.playlist_action_create_playlist else R.string.playlist_action_update_playlist
    }
    val headerSubTitleRes = remember(targetPlaylistId) {
        if (targetPlaylistId == null) R.string.playlist_action_create_playlist else R.string.playlist_action_update_playlist
    }

    LLazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = state,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        scrollToHelper.startRecord()

        item(key = "header") {
            scrollToHelper.doRecord("header")
            NavigatorHeader(
                modifier = Modifier.statusBarsPadding(),
                title = stringResource(id = headerTitleRes),
                subTitle = stringResource(id = headerSubTitleRes),
            )
        }

        editTextFor(
            title = "主标题",
            minLines = 1,
            value = createOrEditSM.title,
            onInit = { scrollToHelper.doRecord(it) },
            onFocus = onFocusCallback
        )

        editTextFor(
            title = "简介/备注",
            minLines = 3,
            value = createOrEditSM.subTitle,
            onInit = { scrollToHelper.doRecord(it) },
            onFocus = onFocusCallback
        )
    }
}

private fun LazyListScope.editTextFor(
    title: String,
    minLines: Int = 1,
    value: MutableState<String>,
    onInit: (key: String) -> Unit = {},
    onFocus: (key: String) -> Unit = {}
) {
    item(key = title) {
        onInit(title)

        EditText(
            title = title,
            value = value,
            minLines = minLines,
            onFocus = { onFocus(title) }
        )
    }
}

@Composable
fun EditText(
    title: String = "",
    minLines: Int = 1,
    value: MutableState<String>,
    onFocus: () -> Unit = {}
) {
    val focusRequest = remember { FocusRequester() }
    val focused = remember { mutableStateOf(false) }
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        .copy(alpha = 0.3f)
    val borderColor = animateColorAsState(
        targetValue = if (focused.value) Color(0xFF135CB6) else color,
        label = "TextField border color with focus"
    )

    BasicTextField(
        modifier = Modifier
            .focusRequester(focusRequest)
            .onFocusChanged {
                focused.value = it.hasFocus && it.isFocused
                if (it.hasFocus && it.isFocused) {
                    onFocus()
                }
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        minLines = minLines,
        textStyle = TextStyle.Default.copy(
            color = dayNightTextColor(),
            fontSize = 18.sp
        ),
        cursorBrush = SolidColor(dayNightTextColor()),
        value = value.value,
        onValueChange = { value.value = it }
    ) { innerTextField ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                border = BorderStroke(2.dp, borderColor.value),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    innerTextField()
                }
            }
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = borderColor.value
                )
            }
        }
    }
}