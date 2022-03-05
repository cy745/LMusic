package com.lalilu.lmusic.fragment

import android.annotation.SuppressLint
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lalilu.R
import com.lalilu.databinding.FragmentSongDetailBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setCoverSourceUri
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.fragment.viewmodel.SongDetailViewModel
import com.lalilu.lmusic.service.MSongBrowser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@FlowPreview
@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SongDetailFragment : DataBindingFragment(), CoroutineScope {
    private val args: SongDetailFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: SongDetailViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_song_detail)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onViewCreated() {
        val binding = mBinding as FragmentSongDetailBinding
        mState._song.observe(viewLifecycleOwner) {
            it ?: return@observe
            binding.songDetailTitle.text = it.mediaMetadata.title.toString()
            binding.songDetailArtist.text = it.mediaMetadata.artist.toString()
            binding.detailCover.setCoverSourceUri(it.mediaMetadata.mediaUri)
        }
        mState._song.postValue(
            mediaSource.getItemById(ITEM_PREFIX + args.mediaId)
        )
        binding.songDetailSetSongToNextButton.setOnClickListener {
            mSongBrowser.addToNext(args.mediaId)
        }
        binding.songDetailAddSongToPlaylistButton.setOnClickListener {
            findNavController().navigate(
                SongDetailFragmentDirections.songDetailToAddToPlaylist(args.mediaId)
            )
        }
        binding.songDetailSearchForLyricButton.setOnClickListener {
            val mediaItem = mState.song.value ?: return@setOnClickListener
            val metadata = mediaItem.mediaMetadata
            findNavController().navigate(
                SongDetailFragmentDirections.songDetailToSearchForLyric(
                    mediaTitle = metadata.title.toString(),
                    artistName = metadata.artist.toString(),
                    albumTitle = metadata.albumTitle.toString(),
                    mediaId = mediaItem.mediaId
                )
            )
        }
    }
}