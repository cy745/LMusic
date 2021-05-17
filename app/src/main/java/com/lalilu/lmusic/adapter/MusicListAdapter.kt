package com.lalilu.lmusic.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.lalilu.lmusic.R
import com.lalilu.lmusic.dao.SongDao
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.databinding.ItemSongBinding
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.utils.SongDiffCallback
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel
import java.util.*

class MusicListAdapter(private val context: Activity) :
    RecyclerView.Adapter<MusicListAdapter.SongHolder>() {
    private var songList: List<Song> = ArrayList()
    private var songDao: SongDao = MusicDatabase.getInstance(context).songDao()
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder =
        SongHolder(LayoutInflater.from(context).inflate(R.layout.item_song, parent, false))

    override fun getItemCount(): Int = songList.size

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.binding.song = songList[position]
        holder.albumPic.setImageURI((holder.binding.song as Song).albumUri, context)
    }

    inner class SongHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSongBinding.bind(itemView)
        var removeBtn: ImageButton = binding.songRemove
        var albumPic: SimpleDraweeView = binding.songAlbumPic

        init {
            binding.root.setOnClickListener {
                swapSong()
                recyclerView.scrollToPosition(0)
                MusicServiceViewModel.getInstance().getPlayingSong().postValue(binding.song as Song)
            }
            binding.root.setOnLongClickListener {
                Toast.makeText(
                    context, (binding.song as Song).toString(), Toast.LENGTH_SHORT
                ).show()
                true
            }
            removeBtn.setOnClickListener {
                songDao.delete(binding.song as Song)
            }
        }

        private fun swapSong() {
            val temp = ArrayList<Song>(songList)
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
