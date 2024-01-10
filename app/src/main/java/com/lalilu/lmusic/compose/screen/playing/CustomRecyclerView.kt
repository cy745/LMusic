package com.lalilu.lmusic.compose.screen.playing

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.common.base.Playable
import com.lalilu.component.extension.collectWithLifeCycleOwner
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lmusic.adapter.NewPlayingAdapter
import com.lalilu.lmusic.adapter.ViewEvent
import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.ui.ComposeNestedScrollRecyclerView
import com.lalilu.lmusic.utils.extension.calculateExtraLayoutSpace
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.QueueAction
import org.koin.compose.koinInject


@Composable
fun CustomRecyclerView(
    modifier: Modifier = Modifier,
    playingVM: PlayingViewModel = koinInject(),
    onScrollStart: () -> Unit = {},
    onScrollTouchUp: () -> Unit = {},
    onScrollIdle: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val activity = context.getActivity()!!

            ComposeNestedScrollRecyclerView(context = context).apply {
                val mAdapter = createAdapter(playingVM) { scrollToPosition(0) }
                mAdapter.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                id = Int.MAX_VALUE
                overScrollMode = View.OVER_SCROLL_NEVER
                layoutManager = calculateExtraLayoutSpace(context, 500)
                adapter = mAdapter
                setItemViewCacheSize(5)

                LPlayer.runtime.info.listFlow
                    .collectWithLifeCycleOwner(activity) { mAdapter.setDiffData(it) }

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        when (newState) {
                            1 -> onScrollStart()
                            2 -> onScrollTouchUp()
                            0 -> onScrollIdle()
                        }
                    }
                })
            }
        }
    )
}

private fun createAdapter(
    playingVM: PlayingViewModel,
    onScrollToTop: () -> Unit = {},
): NewPlayingAdapter {
    return NewPlayingAdapter.Builder()
        .setViewEvent { event, item ->
            when (event) {
                ViewEvent.OnClick -> playingVM.play(mediaId = item.mediaId, playOrPause = true)
                ViewEvent.OnLongClick -> {
                    GlobalNavigatorImpl.goToDetailOf(
                        mediaId = item.mediaId,
                        navigator = NavigationWrapper.navigator,
                    )
                    NavigationWrapper.navigator?.show()
                }

                ViewEvent.OnSwipeLeft -> {
                    DynamicTips.push(
                        title = item.title,
                        subTitle = "下一首播放",
                        imageData = item.imageSource
                    )
                    QueueAction.AddToNext(item.mediaId).action()
                }

                ViewEvent.OnSwipeRight -> QueueAction.Remove(item.mediaId).action()
                ViewEvent.OnBind -> {

                }
            }
        }
        .setOnDataUpdatedCB { needScrollToTop -> if (needScrollToTop) onScrollToTop() }
        .setOnItemBoundCB { binding, item ->
            playingVM.requireLyric(item) {
                binding.songLrc.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }
        }
        .setItemCallback(object : DiffUtil.ItemCallback<Playable>() {
            override fun areItemsTheSame(oldItem: Playable, newItem: Playable): Boolean =
                oldItem.mediaId == newItem.mediaId

            override fun areContentsTheSame(oldItem: Playable, newItem: Playable): Boolean =
                oldItem.mediaId == newItem.mediaId &&
                        oldItem.title == newItem.title &&
                        oldItem.durationMs == newItem.durationMs
        })
        .build()
}