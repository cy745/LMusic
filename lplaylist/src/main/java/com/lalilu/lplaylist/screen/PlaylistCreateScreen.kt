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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DialogScreen
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import org.koin.compose.koinInject
import java.util.UUID


class PlaylistCreateScreenModel : ScreenModel {
    val title = mutableStateOf("")
    val subTitle = mutableStateOf("")
}

/**
 * [targetPlaylistId]   目标操作歌单的Id
 * [mediaIdsToAdd]           需要添加进歌单里的媒体元素的Id
 */
data class PlaylistCreateScreen(
    private val targetPlaylistId: String? = null,
    private val mediaIdsToAdd: List<String> = emptyList()
) : DynamicScreen(), DialogScreen {
    override val key: ScreenKey
        get() = mediaIdsToAdd.toString()

    @Composable
    override fun Content() {
        val navigator = koinInject<GlobalNavigator>()
        val playlistRepo: PlaylistRepository = koinInject()
        val createSM = rememberScreenModel { PlaylistCreateScreenModel() }

        RegisterActions {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.playlist_action_create_playlist,
                    fitImePadding = true,
                    color = Color.Green
                ) {
                    val title = createSM.title.value

                    if (title.isBlank()) {
                        ToastUtils.showShort("歌单名称不可为空")
                    } else {
                        playlistRepo.save(
                            LPlaylist(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                subTitle = "",
                                coverUri = "",
                                mediaIds = mediaIdsToAdd
                            )
                        )
                        navigator.goBack()
                    }
                }
            )
        }

        PlaylistCreateScreen(
            createSM = createSM
        )
    }
}

@Composable
private fun DynamicScreen.PlaylistCreateScreen(
    createSM: PlaylistCreateScreenModel
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
                title = stringResource(id = R.string.playlist_action_create_playlist),
                subTitle = "创建一个自定义的歌单"
            )
        }

        editTextFor(
            title = "主标题",
            minLines = 1,
            value = createSM.title,
            onInit = { scrollToHelper.doRecord(it) },
            onFocus = onFocusCallback
        )

        editTextFor(
            title = "简介/备注",
            minLines = 3,
            value = createSM.subTitle,
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
    val borderColor = animateColorAsState(
        targetValue = if (focused.value) Color(0xFF135CB6) else Color.DarkGray,
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
                    color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                        .copy(alpha = 0.5f)
                )
            }
        }
    }
}