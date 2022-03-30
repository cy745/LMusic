package com.lalilu.lmusic.fragment

import android.content.Context
import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.media3.common.MediaItem
import com.blankj.utilcode.util.AdaptScreenUtils
import com.blankj.utilcode.util.SnackbarUtils
import com.dirror.lyricviewx.GRAVITY_CENTER
import com.dirror.lyricviewx.GRAVITY_LEFT
import com.dirror.lyricviewx.GRAVITY_RIGHT
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.adapter.PlayingAdapter.OnItemDragOrSwipedListener
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.base.showDialog
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.event.GlobalViewModel
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.get
import com.lalilu.lmusic.utils.listen
import com.lalilu.lmusic.viewmodel.AblyService
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.STATE_LISTENING
import com.lalilu.lmusic.viewmodel.bindViewModel
import com.lalilu.ui.*
import com.lalilu.ui.appbar.ExpendHeaderBehavior
import com.lalilu.ui.appbar.MyAppbarBehavior
import com.lalilu.ui.appbar.STATE_COLLAPSED
import com.lalilu.ui.appbar.STATE_NORMAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlayingFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var lyricManager: LyricManager

    @Inject
    lateinit var mGlobal: GlobalViewModel

    @Inject
    lateinit var mState: PlayingViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mAdapter: PlayingAdapter

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var ablyService: AblyService

    private var needRefresh = true

    private val dialog: NavigatorFragment by lazy {
        NavigatorFragment()
    }

    private val settingsSp: SharedPreferences by lazy {
        requireContext().getSharedPreferences(Config.SETTINGS_SP, Context.MODE_PRIVATE)
    }

    private val seekbarHandlerKey: String by lazy {
        resources.getString(R.string.sp_key_player_settings_seekbar_handler)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemDragOrSwipedListener = object : OnItemDragOrSwipedListener {
            override fun onDelete(mediaItem: MediaItem): Boolean {
                return mSongBrowser.removeById(mediaItem.mediaId).also {
                    val message = SpannableStringBuilder()
                        .append(
                            "已移除\n", RelativeSizeSpan(0.8f),
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        ).append(mediaItem.mediaMetadata.title)

                    SnackbarUtils.with(mBinding!!.root)
                        .setDuration(SnackbarUtils.LENGTH_LONG)
                        .setMessage(message)
                        .setAction("撤回") {
                            mSongBrowser.revokeRemove()
                        }.show()
                }
            }

            override fun onAddToNext(mediaItem: MediaItem): Boolean {
                return mSongBrowser.addToNext(mediaItem.mediaId).also {
                    val message = SpannableStringBuilder()
                        .append(
                            "下一首播放\n", RelativeSizeSpan(0.8f),
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        ).append(mediaItem.mediaMetadata.title)

                    SnackbarUtils.with(mBinding!!.root)
                        .setDuration(SnackbarUtils.LENGTH_SHORT)
                        .setMessage(message)
                        .show()
                }
            }
        }
        mAdapter.onItemClick = { item, position ->
            if (mSongBrowser.playById(item.mediaId)) {
                mSongBrowser.browser?.apply {
                    prepare()
                    play()
                }
            }
        }
        mAdapter.onItemLongClick = { item, position ->
            showDialog(dialog) {
                (this as NavigatorFragment)
                    .navigateFrom(R.id.songDetailFragment)
                    .navigate(
                        LibraryFragmentDirections.libraryToSongDetail(item.mediaId)
                    )
            }
        }
        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.vm, mState)
            .addParam(BR.ev, mEvent)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentPlayingBinding
        val fmAppbarLayout = binding.fmAppbarLayout
        val fmLyricViewX = binding.fmLyricViewX
        val fmToolbar = binding.fmToolbar
        val seekBar = binding.maSeekBar
        val fmTips = binding.fmTips
        val behavior = fmAppbarLayout.behavior as MyAppbarBehavior

        settingsSp.listen(
            R.string.sp_key_lyric_settings_text_size,
            resources.getInteger(R.integer.sp_key_lyric_settings_text_default_size)
        ) {
            fmLyricViewX.setCurrentTextSize(AdaptScreenUtils.pt2Px(it.toFloat()).toFloat())
            fmLyricViewX.invalidate()
        }
        settingsSp.listen(
            R.string.sp_key_lyric_settings_secondary_text_size,
            resources.getInteger(R.integer.sp_key_lyric_settings_secondary_text_default_size)
        ) {
            fmLyricViewX.setNormalTextSize(AdaptScreenUtils.pt2Px(it.toFloat()).toFloat())
            fmLyricViewX.invalidate()
        }
        settingsSp.listen(
            R.string.sp_key_lyric_settings_text_gravity, 1
        ) {
            when (it) {
                0 -> fmLyricViewX.setTextGravity(GRAVITY_LEFT)
                1 -> fmLyricViewX.setTextGravity(GRAVITY_CENTER)
                2 -> fmLyricViewX.setTextGravity(GRAVITY_RIGHT)
            }
        }

        mActivity?.setSupportActionBar(fmToolbar)
        behavior.addOnStateChangeListener(object :
            ExpendHeaderBehavior.OnScrollToStateListener(STATE_COLLAPSED, STATE_NORMAL) {
            override fun onScrollToStateListener() {
                if (fmToolbar.hasExpandedActionView())
                    fmToolbar.collapseActionView()
            }
        })
        mGlobal.currentPlaylistLiveData.observe(viewLifecycleOwner) {
            mState.postData(it)
        }
        mGlobal.currentMediaItemLiveData.observe(viewLifecycleOwner) {
            mState.song.postValue(it)
            ablyService.listenChannel?.publish(STATE_LISTENING, it?.mediaMetadata?.title)
        }
        mGlobal.currentPositionLiveData.observe(viewLifecycleOwner) {
            fmLyricViewX.updateTime(it, needRefresh)
            needRefresh = false
        }
        // 从 metadata 中获取歌曲的总时长传递给 SeekBar
        mGlobal.currentMediaItemLiveData.observe(viewLifecycleOwner) {
            seekBar.maxValue = (it?.mediaMetadata?.getDuration()?.coerceAtLeast(0) ?: 0f).toFloat()
        }
        mGlobal.currentPositionLiveData.observe(viewLifecycleOwner) {
            seekBar.updateValue(it.toFloat())
        }
        lyricManager.songLyric.observe(viewLifecycleOwner) {
            fmLyricViewX.setLyricEntryList(emptyList())
            fmLyricViewX.loadLyric(it?.first, it?.second)
        }
        mEvent.isAppbarLayoutExpand.observe(viewLifecycleOwner) {
            it?.get { fmAppbarLayout.setExpanded(false, true) }
        }

        ablyService.otherHistoryLiveData.observe(viewLifecycleOwner) {
            fmTips.visibility = if (it == null) View.GONE else View.VISIBLE
            fmTips.text = "Users: ${it?.size}"
        }
        seekBar.minIncrement = 500f
        seekBar.clickListeners.add(object : OnSeekBarClickListener {
            override fun onClick(@ClickPart clickPart: Int, action: Int) {
                if (settingsSp.get(seekbarHandlerKey, 0) == 0) {
                    playHandle(clickPart)
                } else {
                    mSongBrowser.togglePlay()
                }
                haptic()
            }

            override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                if (settingsSp.get(seekbarHandlerKey, 0) == 1) {
                    playHandle(clickPart)
                }
                haptic()
            }

            override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                if (settingsSp.get(seekbarHandlerKey, 0) == 2) {
                    playHandle(clickPart)
                }
                doubleHaptic()
            }
        })
        seekBar.seekToListeners.add(
            object : OnSeekBarSeekToListener {
                override fun onSeekTo(value: Float) {
                    mSongBrowser.browser?.seekTo(value.toLong())
                }
            }
        )
        seekBar.scrollListeners.add(
            object : OnSeekBarScrollToThresholdListener({ 300f }) {
                override fun onScrollToThreshold() {
                    showDialog(dialog)
                    haptic()
                }

                override fun onScrollRecover() {
                    dialog.dismiss()
                    haptic()
                }
            }
        )
        seekBar.cancelListeners.add(
            object : OnSeekBarCancelListener {
                override fun onCancel() {
                    haptic()
                }
            }
        )
        seekBar.progressToListener.add(
            object : OnProgressToListener {
                override fun onProgressToMax(value: Float, fromUser: Boolean) {
                    if (fromUser) haptic()
                }

                override fun onProgressToMin(value: Float, fromUser: Boolean) {
                    if (fromUser) haptic()
                }
            }
        )
    }

    fun playHandle(@ClickPart clickPart: Int) {
        when (clickPart) {
            CLICK_PART_LEFT -> mSongBrowser.browser?.seekToPrevious()
            CLICK_PART_MIDDLE -> mSongBrowser.togglePlay()
            CLICK_PART_RIGHT -> mSongBrowser.browser?.seekToNext()
        }
    }

    override fun onStart() {
        needRefresh = true
        super.onStart()
    }

    fun haptic() {
        HapticUtils.haptic(this.requireView(), HapticUtils.Strength.HAPTIC_STRONG)
    }

    fun doubleHaptic() {
        HapticUtils.doubleHaptic(this.requireView())
    }
}