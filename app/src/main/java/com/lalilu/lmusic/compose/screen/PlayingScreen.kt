package com.lalilu.lmusic.compose.screen

import android.view.View
import androidx.activity.OnBackPressedCallback
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
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.ConvertUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.common.HapticUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.NewPlayingAdapter
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.screen.library.detail.SongDetailScreen
import com.lalilu.lmusic.utils.SeekBarHandler
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_CLICK
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_DOUBLE_CLICK
import com.lalilu.lmusic.utils.SeekBarHandler.Companion.CLICK_HANDLE_MODE_LONG_CLICK
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.ui.CLICK_PART_MIDDLE
import com.lalilu.ui.ClickPart
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarClickListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import com.lalilu.ui.OnSeekBarSeekToListener
import com.lalilu.ui.appbar.MyAppbarBehavior
import com.lalilu.ui.internal.StateHelper.Companion.STATE_FULLY_EXPENDED
import com.lalilu.ui.internal.StateHelper.Companion.STATE_MIDDLE
import org.koin.androidx.compose.getViewModel

@Composable
@ExperimentalMaterialApi
fun PlayingScreen(
    playingVM: PlayingViewModel = getViewModel()
) {
    val density = LocalDensity.current
    val navToSongAction = SongDetailScreen.navToByArgv()
    val statusBarsInsets = WindowInsets.statusBars
    val systemUiController = rememberSystemUiController()
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
                onPlayNext = { playingVM.browser.skipToNext() },
                onPlayPause = { playingVM.browser.playPause() },
                onPlayPrevious = { playingVM.browser.skipToPrevious() }
            )

            val statusBarHeight = SystemUiUtil.getFixedStatusHeight(activity)

            val behavior = fmAppbarLayout.behavior as MyAppbarBehavior
            activity.setSupportActionBar(fmToolbar)
            fmToolbar.setPadding(0, statusBarHeight, 0, 0)
            fmToolbar.layoutParams.apply { height += statusBarHeight }

            val adapter = NewPlayingAdapter.Builder()
                .setOnLongClickCB {
                    navToSongAction.invoke(it)
                    SmartModalBottomSheet.show()
                }
                .setOnClickCB {
                    playingVM.browser.playById(it)
                }
                .setOnSwipedLeftCB {
                    playingVM.browser.addToNext(it.id)
                }
                .setOnSwipedRightCB {
                    playingVM.browser.removeById(it.id)
                }
                .setOnDataUpdatedCB {
                    fmRecyclerView.scrollToPosition(0)
                }
                .setOnItemBoundCB { binding, item ->
                    playingVM.requireLyric(item) {
                        binding.songLrc.visibility = if (it) View.VISIBLE else View.INVISIBLE
                    }
                }
                .setItemCallback(object : DiffUtil.ItemCallback<LSong>() {
                    override fun areItemsTheSame(oldItem: LSong, newItem: LSong): Boolean =
                        oldItem.id == newItem.id

                    override fun areContentsTheSame(oldItem: LSong, newItem: LSong): Boolean =
                        oldItem.id == newItem.id &&
                                oldItem.name == newItem.name &&
                                oldItem.durationMs == newItem.durationMs
                })
                .build()

            val backPressedHelper = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    fmAppbarLayout.setExpanded(true)
                }
            }
            activity.onBackPressedDispatcher.addCallback(backPressedHelper)
            behavior.addOnStateChangeListener { lastState, nowState ->
                backPressedHelper.isEnabled = nowState == STATE_FULLY_EXPENDED

                if (lastState == STATE_FULLY_EXPENDED && nowState == STATE_MIDDLE) {
                    maSeekBar.animateAlphaTo(100f)
                    systemUiController.isStatusBarVisible = true
                } else if (lastState == STATE_MIDDLE && nowState == STATE_FULLY_EXPENDED) {
                    maSeekBar.animateAlphaTo(0f)
                    systemUiController.isStatusBarVisible = false
                }
            }

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
                        playingVM.browser.playPause()
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
                playingVM.browser.seekTo(value)
            })
            maSeekBar.cancelListeners.add(OnSeekBarCancelListener {
                HapticUtils.haptic(this@apply.root)
            })
            playingVM.runtime.songsLiveData.observe(activity) {
                adapter.setDiffData(it)
            }
            playingVM.runtime.playingLiveData.observe(activity) {
                maSeekBar.maxValue = it?.durationMs?.toFloat() ?: 0f
                song = it

                if (it != null) {
                    DynamicTips.push(
                        title = it.name,
                        subTitle = "播放中",
                        imageData = it
                    )
                }
            }
            playingVM.runtime.positionLiveData.observe(activity) {
                maSeekBar.updateValue(it.toFloat())
                fmLyricViewX.updateTime(it)
            }
            playingVM.lyricRepository.currentLyricLiveData.observe(activity) {
                fmLyricViewX.setLyricEntryList(emptyList())
                fmLyricViewX.loadLyric(it?.first, it?.second)
            }
            playingVM.lMusicSp.apply {
                lyricGravity.flow(true).asLiveData().observe(activity) {
                    when (it ?: Config.DEFAULT_SETTINGS_LYRIC_GRAVITY) {
                        0 -> fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                        1 -> fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                        2 -> fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
                    }
                }

                lyricTextSize.flow(true).asLiveData().observe(activity) {
                    val sp = it ?: Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
                    val textSize = ConvertUtils.sp2px(sp.toFloat()).toFloat()
                    fmLyricViewX.setNormalTextSize(textSize)
                    fmLyricViewX.setCurrentTextSize(textSize * 1.2f)
                }

                lyricTypefaceUri.flow(true).asLiveData().observe(activity) {
                    if (it != null) {
                        fmLyricViewX.setLyricTypeface(path = it)
                    } else {
                        fmLyricViewX.setLyricTypeface(typeface = null)
                    }
                }

                autoHideSeekbar.flow(true).asLiveData().observe(activity) {
                    val autoHide = it ?: Config.DEFAULT_SETTINGS_AUTO_HIDE_SEEKBAR

                    maSeekBar.autoHide = autoHide
                }

                this.seekBarHandler.flow(true).asLiveData().observe(activity) {
                    seekBarHandler.clickHandleMode = it ?: Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
                }
            }
        }
    }) {
        ViewCompat.dispatchApplyWindowInsets(root, windowInsetsCompat)
    }
}