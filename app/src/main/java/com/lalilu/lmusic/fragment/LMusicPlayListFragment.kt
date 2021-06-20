package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayListBinding

class LMusicPlayListFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentPlayListBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }

}