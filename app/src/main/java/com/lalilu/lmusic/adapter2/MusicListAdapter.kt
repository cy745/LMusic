package com.lalilu.lmusic.adapter2

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.MusicApplication
import com.lalilu.lmusic.R
import com.lalilu.lmusic.databinding.ItemSongBinding
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.utils.SongDiffCallback
import java.util.*

class MusicListAdapter(
    private val context: Activity,
    private val itemClickListener: (song: Song) -> Unit
) : RecyclerView.Adapter<MusicListAdapter.SongHolder>() {
    private var songList: List<Song> = ArrayList()
    private var audioMediaScanner = (context.application as MusicApplication).audioMediaScanner

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return SongHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int = songList.size

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.binding.song = songList[position]
    }

    inner class SongHolder(
        itemView: View,
        itemClickListener: (song: Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSongBinding.bind(itemView)
        var removeBtn: ImageButton = binding.songRemove

        init {
            binding.root.setOnClickListener {
                binding.song?.let { itemClickListener(it) }
                swapSong()
            }
            binding.root.setOnLongClickListener {
                Toast.makeText(
                    context, (binding.song as Song).toString(), Toast.LENGTH_SHORT
                ).show()
                true
            }
        }

        private fun swapSong() {
            val temp = ArrayList(songList)
            Collections.swap(temp, temp.indexOf(binding.song as Song), 0)
            setSongList(temp)
        }
    }

    fun setSongList(list: List<Song>) {
        val result = DiffUtil.calculateDiff(SongDiffCallback(songList, list), true)
        result.dispatchUpdatesTo(this)
        songList = list
    }
}
