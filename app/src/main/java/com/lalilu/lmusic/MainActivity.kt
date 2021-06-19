package com.lalilu.lmusic

import android.content.Intent
import android.graphics.Canvas
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.databinding.ActivityMainBinding
import com.lalilu.lmusic.adapter2.ItemTouch
import com.lalilu.lmusic.adapter2.MusicListAdapter
import com.lalilu.lmusic.service2.MusicBrowser
import com.lalilu.lmusic.service2.MusicService.Companion.ACTION_MOVE_SONG
import com.lalilu.lmusic.service2.MusicService.Companion.ACTION_SWIPED_SONG
import com.lalilu.lmusic.utils.*
import com.lalilu.media.LMusicMediaModule
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaBrowser: MusicBrowser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.requestPermission(this)
        mediaBrowser = MusicBrowser(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolBar()
        initRecyclerView()
        initSeekBar()
        bindUiToBrowser()
    }

    class ItemTouchCallback(private val itemTouch: ItemTouch) : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val swipeFlags = ItemTouchHelper.RIGHT
            return makeMovementFlags(0, swipeFlags);
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return itemTouch.onItemMove(viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            itemTouch.onItemSwiped(viewHolder)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                //滑动时改变Item的透明度
                val alpha: Float = 1 - abs(dX) / viewHolder.itemView.width
                viewHolder.itemView.alpha = alpha
            }
            val x = (dX * 0.7).toFloat()
            super.onChildDraw(c, recyclerView, viewHolder, x, dY, actionState, isCurrentlyActive)
        }
    }

    private fun initRecyclerView() {
        binding.musicRecyclerView.adapter = MusicListAdapter(this)
        (binding.musicRecyclerView.adapter as MusicListAdapter).itemOnMove = { mediaId ->
            mediaBrowser.mediaController.transportControls.sendCustomAction(
                ACTION_MOVE_SONG,
                Bundle().also { it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId) }
            )
        }
        (binding.musicRecyclerView.adapter as MusicListAdapter).itemOnSwiped = { mediaId ->
            mediaBrowser.mediaController.transportControls.sendCustomAction(
                ACTION_SWIPED_SONG,
                Bundle().also { it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId) }
            )
        }
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.musicRecyclerView.itemAnimator = FadeInAnimator(OvershootInterpolator()).apply {
            this.addDuration = 300
            this.moveDuration = 200
            this.removeDuration = 50
        }
        ItemTouchHelper(ItemTouchCallback(binding.musicRecyclerView.adapter as MusicListAdapter)).attachToRecyclerView(
            binding.musicRecyclerView
        )
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolbar)
        ColorAnimator.setContentScrimColorFromPaletteDraweeView(
            binding.playingSongAlbumPic,
            binding.collapsingToolbarLayout
        )
        binding.toolbar.setOnClickListener {
            binding.musicRecyclerView.smoothScrollToPosition(0)
            binding.appbar.setExpanded(true, true)
        }
    }

    private fun initSeekBar() {
        ColorAnimator.getPaletteFromFromPaletteDraweeView(binding.playingSongAlbumPic) {
            binding.seekBar.setThumbColor(it.getAutomaticColor())
        }
        binding.seekBar.onActionUp = {
            mediaBrowser.mediaController.transportControls?.seekTo(it)
        }
        binding.seekBar.setOnClickListener {
            mediaBrowser.mediaController.transportControls.sendCustomAction(
                PlaybackStateCompat.ACTION_PLAY_PAUSE.toString(), null
            )
        }
    }


    private fun bindUiToBrowser() {
        mediaBrowser.mediaMetadataCompat.observeForever {
            if (it == null) return@observeForever
            binding.collapsingToolbarLayout.title = it.description.title
            binding.playingSongAlbumPic.setImageURI(
                it.description.iconUri, this
            )
            binding.seekBar.setSumDuration(
                it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
        }
        mediaBrowser.playbackStateCompat.observeForever {
            if (it == null) return@observeForever
            binding.seekBar.updateNowPosition(it)
        }
        mediaBrowser.setAdapterToUpdate(
            binding.musicRecyclerView.adapter as MusicListAdapter
        )
    }

    override fun onStart() {
        super.onStart()
        println("[onStart]")
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        mediaBrowser.disconnect()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                mediaBrowser.mediaController.transportControls.play()
            }
            R.id.appbar_pause -> {
                mediaBrowser.mediaController.transportControls.pause()
            }
            R.id.appbar_scan_song -> {
                Thread {
                    mediaBrowser.disconnect()
                    LMusicMediaModule.getInstance(null).mediaScanner.updateSongDataBase {
                        mediaBrowser.connect()
                    }
                }.start()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PermissionUtils.returnPermissionCheck(this, requestCode)
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }


}