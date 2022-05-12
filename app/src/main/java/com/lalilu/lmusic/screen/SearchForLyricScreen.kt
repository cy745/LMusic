package com.lalilu.lmusic.screen

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.databinding.FragmentSearchForLyricHeaderBinding
import com.lalilu.lmusic.apis.bean.SongSearchSong
import com.lalilu.lmusic.viewmodel.SearchForLyricViewModel

@Composable
fun SearchForLyricScreen(
    mediaItem: MediaItem,
    navigateUp: () -> Unit = {},
    expendScaffold: () -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp,
    context: Context = LocalContext.current,
    viewModel: SearchForLyricViewModel = hiltViewModel()
) {
    val keyword = "${mediaItem.mediaMetadata.title} ${mediaItem.mediaMetadata.artist}"
    val lyrics = remember { mutableStateListOf<SongSearchSong>() }
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidViewBinding(factory = { inflater, parent, attachToParent ->
            FragmentSearchForLyricHeaderBinding.inflate(inflater, parent, attachToParent).apply {
                val activity = context.getActivity()!!
                searchForLyricCancel.setOnClickListener { navigateUp() }
                searchForLyricConfirm.setOnClickListener {
                    viewModel.saveSongLyric(
                        songId = lyrics[selectedIndex].id,
                        mediaId = mediaItem.mediaId,
                        toastTips = ToastUtils::showShort,
                        success = navigateUp
                    )
                }
                searchForLyricKeyword.setOnEditorActionListener { textView, _, _ ->
                    viewModel.getSongResult(
                        binding = this,
                        keyword = textView.text.toString(),
                        items = lyrics
                    )
                    textView.clearFocus()
                    KeyboardUtils.hideSoftInput(textView)
                    return@setOnEditorActionListener true
                }
                KeyboardUtils.registerSoftInputChangedListener(activity) {
                    if (searchForLyricKeyword.isFocused && it > 0) {
                        expendScaffold()
                        return@registerSoftInputChangedListener
                    }
                    if (searchForLyricKeyword.isFocused) {
                        searchForLyricKeyword.onEditorAction(0)
                    }
                }
                searchForLyricKeyword.setText(keyword)
                viewModel.getSongResult(
                    binding = this,
                    keyword = searchForLyricKeyword.text.toString(),
                    items = lyrics
                )
            }
        }) {
            searchForLyricKeyword.setText(keyword)
        }
//        Divider()
        LazyColumn(contentPadding = PaddingValues(bottom = contentPaddingForFooter)) {
            itemsIndexed(lyrics) { index, item ->
                LyricCard(
                    songSearchSong = item,
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index }
                )
            }
        }
    }
}

@Composable
fun LyricCard(
    songSearchSong: SongSearchSong,
    selected: Boolean,
    onClick: () -> Unit
) = LyricCard(
    title = songSearchSong.name,
    artist = songSearchSong.artists.joinToString("/") { it.name },
    albumTitle = songSearchSong.album?.name,
    duration = TimeUtils.millis2String(songSearchSong.duration, "mm:ss"),
    selected = selected,
    onClick = onClick
)

@Composable
fun LyricCard(
    title: String,
    artist: String,
    albumTitle: String?,
    duration: String?,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val color: Color by animateColorAsState(
        if (selected) contentColorFor(
            backgroundColor = MaterialTheme.colors.background
        ).copy(0.2f) else Color.Transparent
    )

    Surface(
        color = color
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                duration?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = artist,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                albumTitle?.let {
                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySearchForLyricScreen() {
    Text(text = "无法获取该歌曲信息", modifier = Modifier.padding(20.dp))
}