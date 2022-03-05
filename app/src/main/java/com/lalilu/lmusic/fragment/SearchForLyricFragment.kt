package com.lalilu.lmusic.fragment

import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentSearchForLyricBinding
import com.lalilu.lmusic.adapter.SearchForLyricAdapter
import com.lalilu.lmusic.apis.NetworkLyric
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.PersistLyric
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SearchForLyricFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val args: SearchForLyricFragmentArgs by navArgs()

    @Inject
    lateinit var mAdapter: SearchForLyricAdapter

    @Inject
    lateinit var networkLyric: NetworkLyric

    @Inject
    lateinit var dataBase: LMusicDataBase

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter = SearchForLyricAdapter()
        return DataBindingConfig(R.layout.fragment_search_for_lyric)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentSearchForLyricBinding

        binding.searchForLyricCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchForLyricConfirm.setOnClickListener {
            val id = mAdapter.singleSelected?.id
            if (id == null) {
                toastThen("未选择匹配歌曲")
                return@setOnClickListener
            }
            launch(Dispatchers.IO) {
                toastThen("开始获取歌词")
                val response = networkLyric.searchForLyric(id)
                val lyric = response?.lrc?.lyric ?: run {
                    toastThen("选中歌曲无歌词")
                    return@launch
                }
                val tlyric = response.tlyric?.lyric

                dataBase.persistLyricDao().save(
                    PersistLyric(
                        mediaId = args.mediaId,
                        lyric = lyric,
                        tlyric = tlyric
                    )
                )
                toastThen("保存匹配歌词成功") {
                    findNavController().navigateUp()
                }
            }
        }

        val keyword = "${args.mediaTitle} ${args.artistName}"

        mAdapter.onItemClick = {
            val oldIndex = mAdapter.data.indexOf(mAdapter.singleSelected)
            val newIndex = mAdapter.data.indexOf(it)

            mAdapter.singleSelected = it
            mAdapter.notifyItemChanged(oldIndex)
            mAdapter.notifyItemChanged(newIndex)
        }

        launch {
            val response = networkLyric.searchForSong(keyword)
            val results = response?.result?.songs ?: emptyList()

            withContext(Dispatchers.Main) {
                if (results.isEmpty()) {
                    binding.searchForLyricRefreshAndTipsButton.text =
                        requireContext().getString(R.string.button_search_for_lyric_no_result)
                } else {
                    binding.searchForLyricRefreshAndTipsButton.text = ""
                    binding.searchForLyricRefreshAndTipsButton.visibility = View.GONE
                }
                mAdapter.setDiffNewData(results.toMutableList())
            }
        }
    }

    private fun toastThen(text: String, then: () -> Unit = {}) = launch(Dispatchers.Main) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        then()
    }
}