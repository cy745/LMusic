package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayListBinding
import com.lalilu.lmusic.adapter2.LMusicPlayListAdapter
import com.lalilu.lmusic.ui.AntiMisOperationRecyclerView
import com.lalilu.player.LMusicPlayerModule

class LMusicPlayListFragment : Fragment() {
    private lateinit var mRecyclerView: AntiMisOperationRecyclerView
    private lateinit var mAdapter: LMusicPlayListAdapter
    private lateinit var mViewModel: LMusicViewModel
    private lateinit var playerModule: LMusicPlayerModule

    private fun initializeObserver() {
        playerModule.playlist.observeForever {
            it?.let {
                mAdapter.playLists = it.toMutableList()
                mAdapter.notifyDataSetChanged()
            }
        }
        mViewModel.mPlayListRecyclerView.postValue(mRecyclerView)
        mViewModel.mPlayListAdapter.postValue(mAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRecyclerView = FragmentPlayListBinding.bind(view).playListRecyclerView
        mRecyclerView.adapter = LMusicPlayListAdapter(requireContext())
        mAdapter = mRecyclerView.adapter as LMusicPlayListAdapter
        mViewModel = LMusicViewModel.getInstance(null)
        playerModule = LMusicPlayerModule.getInstance(null)

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        initializeObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }

}