package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayListBinding
import com.lalilu.lmusic.adapter2.LMusicPlayListAdapter
import jp.wasabeef.recyclerview.animators.FadeInAnimator

class LMusicPlayListFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: LMusicPlayListAdapter
    private lateinit var mViewModel: LMusicViewModel

    private fun initializeObserver() {
        mViewModel.playlist.observeForever {
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

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = FadeInAnimator(OvershootInterpolator()).apply {
            this.addDuration = 300
            this.moveDuration = 200
            this.removeDuration = 50
        }

        initializeObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }

}