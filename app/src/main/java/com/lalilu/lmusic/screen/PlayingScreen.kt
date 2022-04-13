package com.lalilu.lmusic.screen

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import com.lalilu.common.HapticUtils
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.event.GlobalData
import com.lalilu.lmusic.screen.viewmodel.MainViewModel
import com.lalilu.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val CLICK_HANDLE_MODE_CLICK = 0
const val CLICK_HANDLE_MODE_LONG_CLICK = 1
const val CLICK_HANDLE_MODE_DOUBLE_CLICK = 2

@IntDef(
    CLICK_HANDLE_MODE_CLICK,
    CLICK_HANDLE_MODE_LONG_CLICK,
    CLICK_HANDLE_MODE_DOUBLE_CLICK
)
@Retention(AnnotationRetention.SOURCE)
annotation class ClickHandleMode

@Composable
@ExperimentalMaterialApi
fun PlayingScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
    scaffoldShow: suspend () -> Unit = {},
    scaffoldHide: suspend () -> Unit = {},
    onSongSelected: suspend (MediaItem) -> Unit = {},
    onSongMoveToNext: suspend (MediaItem) -> Unit = {},
    onSongRemoved: suspend (MediaItem) -> Unit = {},
    onSongShowDetail: suspend (MediaItem) -> Unit = {},
    onPlayNext: suspend () -> Unit = {},
    onPlayPrevious: suspend () -> Unit = {},
    onPlayPause: suspend () -> Unit = {},
    onSeekToPosition: suspend (Float) -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel(),
    @ClickHandleMode clickHandleMode: Int = CLICK_HANDLE_MODE_CLICK
) {
    fun playHandle(@ClickPart clickPart: Int) {
        when (clickPart) {
            CLICK_PART_LEFT -> onPlayPrevious
            CLICK_PART_MIDDLE -> onPlayPause
            CLICK_PART_RIGHT -> onPlayNext
            else -> null
        }?.let {
            scope.launch { it() }
        }
    }

    val context = LocalContext.current
    AndroidViewBinding(factory = { inflater, parent, attachToParent ->
        FragmentPlayingBinding.inflate(inflater, parent, attachToParent).apply {
            val activity = context.getActivity()!!
            val haptic = { HapticUtils.haptic(this.root) }
            val doubleHaptic = { HapticUtils.doubleHaptic(this.root) }
            adapter = PlayingAdapter().apply {
                onItemClick = { item, _ -> scope.launch { onSongSelected(item) } }
                onItemLongClick = { item, _ -> scope.launch { onSongShowDetail(item) } }
            }
            maSeekBar.scrollListeners.add(object : OnSeekBarScrollToThresholdListener({ 300f }) {
                override fun onScrollToThreshold() {
                    scope.launch { scaffoldShow() }
                    haptic()
                }

                override fun onScrollRecover() {
                    scope.launch { scaffoldHide() }
                    haptic()
                }
            })
            maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
                override fun onClick(@ClickPart clickPart: Int, action: Int) {
                    haptic()
                    if (clickHandleMode != CLICK_HANDLE_MODE_CLICK) {
                        scope.launch { onPlayPause() }
                    } else {
                        playHandle(clickPart)
                    }
                }

                override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                    haptic()
                    if (clickHandleMode != CLICK_HANDLE_MODE_LONG_CLICK) return
                    playHandle(clickPart)
                }

                override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                    doubleHaptic()
                    if (clickHandleMode != CLICK_HANDLE_MODE_DOUBLE_CLICK) return
                    playHandle(clickPart)
                }
            })
            maSeekBar.seekToListeners.add(object : OnSeekBarSeekToListener {
                override fun onSeekTo(value: Float) {
                    scope.launch { onSeekToPosition(value) }
                }
            })
            maSeekBar.cancelListeners.add(object : OnSeekBarCancelListener {
                override fun onCancel() {
                    haptic()
                }
            })
            activity.setSupportActionBar(fmToolbar)
            fmTopPic.palette.observe(activity, this::setPalette)

            GlobalData.currentPlaylistLiveData.observe(activity) {
                adapter?.setDiffNewData(it.toMutableList())
            }
            GlobalData.currentMediaItemLiveData.observe(activity) {
                maSeekBar.maxValue = (it?.mediaMetadata?.getDuration()
                    ?.coerceAtLeast(0) ?: 0f).toFloat()
                song = it
            }
            GlobalData.currentPositionLiveData.observe(activity) {
                maSeekBar.updateValue(it.toFloat())
                fmLyricViewX.updateTime(it)
            }
            mainViewModel.songLyric.observe(activity) {
                fmLyricViewX.setLyricEntryList(emptyList())
                fmLyricViewX.loadLyric(it?.first, it?.second)
            }
        }
    })
}

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}