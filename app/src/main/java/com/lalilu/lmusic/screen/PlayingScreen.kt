package com.lalilu.lmusic.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.blankj.utilcode.util.ConvertUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.lalilu.common.HapticUtils
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.screen.component.SmartModalBottomSheet
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.LMusicLyricManager
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.SeekBarHandler
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_CLICK
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_DOUBLE_CLICK
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_LONG_CLICK
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.SettingsViewModel
import com.lalilu.ui.*
import com.lalilu.ui.appbar.MyAppbarBehavior

@Composable
@ExperimentalMaterialApi
fun PlayingScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val density = LocalDensity.current
    val navController = LocalNavigatorHost.current
    val statusBarsInsets = WindowInsets.statusBars
    val windowInsetsCompat = remember(statusBarsInsets) {
        WindowInsetsCompat.Builder().setInsets(
            WindowInsetsCompat.Type.statusBars(),
            Insets.of(0, statusBarsInsets.getTop(density), 0, statusBarsInsets.getBottom(density))
        ).build()
    }

    AndroidViewBinding(factory = { inflater, parent, attachToParent ->
        FragmentPlayingBinding.inflate(inflater, parent, attachToParent).apply {
            val activity = parent.context.getActivity()!!
            val seekBarHandler = SeekBarHandler(
                onPlayNext = { LMusicBrowser.skipToNext() },
                onPlayPause = { LMusicBrowser.playPause() },
                onPlayPrevious = { LMusicBrowser.skipToPrevious() }
            )
            val behavior = fmAppbarLayout.behavior as MyAppbarBehavior
            activity.setSupportActionBar(fmToolbar)

            val adapter = PlayingAdapter().apply {
                onItemDragOrSwipedListener =
                    object : PlayingAdapter.OnItemDragOrSwipedListener {
                        override fun onDelete(song: LSong): Boolean {
                            return LMusicBrowser.removeById(song.id)
                        }

                        override fun onAddToNext(song: LSong): Boolean {
                            return LMusicBrowser.addToNext(song.id)
                        }
                    }
                onItemClick = { id, _ ->
                    LMusicBrowser.playById(id)
                }
                onItemLongClick = { id, _ ->
                    navController.navigate("${MainScreenData.SongsDetail.name}/$id") {
                        launchSingleTop = true
                    }
                    SmartModalBottomSheet.show()
                }
            }

//            val adapter = ComposeAdapter(
//                onSwipeToLeft = { LMusicBrowser.addToNext(it.id) },
//                onSwipeToRight = { LMusicBrowser.removeById(it.id) },
//                onItemClick = { LMusicBrowser.playById(it.id) },
//                onItemLongClick = {
//                    HapticUtils.haptic(this@apply.root)
//                    navController.navigate("${MainScreenData.SongsDetail.name}/${it.id}") {
//                        launchSingleTop = true
//                    }
//                    SmartModalBottomSheet.show()
//                }
//            )

//            behavior.addOnStateChangeListener(object :
//                StateHelper.OnScrollToStateListener(STATE_COLLAPSED, STATE_NORMAL) {
//                override fun onScrollToStateListener() {
//                    if (fmToolbar.hasExpandedActionView())
//                        fmToolbar.collapseActionView()
//                }
//            })
//            behavior.addOnStateChangeListener(object :
//                StateHelper.OnScrollToStateListener(STATE_NORMAL, STATE_EXPENDED) {
//                override fun onScrollToStateListener() {
//                    if (fmToolbar.hasExpandedActionView())
//                        fmToolbar.collapseActionView()
//                }
//            })

//            fmRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    if (dy <= 10) return
//                    if (!fmToolbar.hasExpandedActionView()) return
//                    if (!KeyboardUtils.isSoftInputVisible(activity)) return
//                    KeyboardUtils.hideSoftInput(activity)
//                }
//            })
            fmRecyclerView.adapter = adapter
            fmRecyclerView.layoutManager = calculateExtraLayoutSpace(this.root.context, 500)
            fmRecyclerView.setItemViewCacheSize(5)

            fmTopPic.palette.observe(activity, this::setPalette)

            maSeekBar.scrollListeners.add(object : OnSeekBarScrollToThresholdListener({ 300f }) {
                override fun onScrollToThreshold() {
                    HapticUtils.haptic(this@apply.root)
                    SmartModalBottomSheet.show()
                }

                override fun onScrollRecover() {
                    HapticUtils.haptic(this@apply.root)
                    SmartModalBottomSheet.hide()
                }
            })
            maSeekBar.clickListeners.add(object : OnSeekBarClickListener {
                override fun onClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.haptic(this@apply.root)
                    if (seekBarHandler.clickHandleMode != CLICK_HANDLE_MODE_CLICK) {
                        LMusicBrowser.playPause()
                        return
                    }
                    seekBarHandler.handle(clickPart)
                }

                override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.haptic(this@apply.root)
                    if (seekBarHandler.clickHandleMode != CLICK_HANDLE_MODE_LONG_CLICK || clickPart == CLICK_PART_MIDDLE) {
                        fmAppbarLayout.autoToggleExpand()
                        return
                    }
                    seekBarHandler.handle(clickPart)
                }

                override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                    HapticUtils.doubleHaptic(this@apply.root)
                    if (seekBarHandler.clickHandleMode != CLICK_HANDLE_MODE_DOUBLE_CLICK) return
                    seekBarHandler.handle(clickPart)
                }
            })
            maSeekBar.seekToListeners.add(OnSeekBarSeekToListener { value ->
                LMusicBrowser.seekTo(value)
            })
            maSeekBar.cancelListeners.add(OnSeekBarCancelListener {
                HapticUtils.haptic(this@apply.root)
            })
            LMusicRuntime.currentPlaylistLiveData.observe(activity) {
                adapter.setDiffNewData(it.toMutableList())
            }
            LMusicRuntime.currentPlayingLiveData.observe(activity) {
                maSeekBar.maxValue = it?.durationMs?.toFloat() ?: 0f
                song = it
            }
            LMusicRuntime.currentPositionLiveData.observe(activity) {
                maSeekBar.updateValue(it.toFloat())
                fmLyricViewX.updateTime(it)
            }
            LMusicLyricManager.currentLyricLiveData.observe(activity) {
                fmLyricViewX.setLyricEntryList(emptyList())
                fmLyricViewX.loadLyric(it?.first, it?.second)
            }
            settingsViewModel.settingsDataStore.apply {
                lyricGravity.liveData().observe(activity) {
                    when (it ?: Config.DEFAULT_SETTINGS_LYRIC_GRAVITY) {
                        0 -> fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                        1 -> fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                        2 -> fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
                    }
                }

                lyricTextSize.liveData().observe(activity) {
                    val sp = it ?: Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
                    val textSize = ConvertUtils.sp2px(sp.toFloat()).toFloat()
                    fmLyricViewX.setNormalTextSize(textSize)
                    fmLyricViewX.setCurrentTextSize(textSize * 1.2f)
                }

                lyricTypefaceUri.liveData().observe(activity) {
                    if (it != null) {
                        fmLyricViewX.setLyricTypeface(path = it)
                    } else {
                        fmLyricViewX.setLyricTypeface(typeface = null)
                    }
                }

                this.seekBarHandler.liveData().observe(activity) {
                    seekBarHandler.clickHandleMode = it ?: Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
                }
            }
        }
    }) {
        ViewCompat.dispatchApplyWindowInsets(root, windowInsetsCompat)
    }
}