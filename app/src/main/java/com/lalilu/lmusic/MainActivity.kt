package com.lalilu.lmusic

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lalilu.lmusic.adapter.MusicListAdapter
import com.lalilu.lmusic.databinding.ActivityMainBinding
import com.lalilu.lmusic.service.MusicService
import com.lalilu.lmusic.service.MusicServiceConn
import com.lalilu.lmusic.utils.ColorAnimator
import com.lalilu.lmusic.utils.MediaUtils
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.viewmodel.MusicDataBaseViewModel
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MusicDataBaseViewModel
    private lateinit var musicConn: MusicServiceConn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionUtils.requestPermission(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = MusicDataBaseViewModel.getInstance(application)
        musicConn = MusicServiceConn()

        bindService(Intent(this, MusicService::class.java).also {
            startService(it)
        }, musicConn, BIND_AUTO_CREATE)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setContentScrimColorFromPaletteDraweeView(
            binding.playingSongAlbumPic,
            binding.collapsingToolbarLayout
        )

        binding.musicRecyclerView.adapter = MusicListAdapter(this)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.toolbar.setOnClickListener {
            binding.musicRecyclerView.smoothScrollToPosition(0)
            binding.appbar.setExpanded(true, true)
        }

        viewModel.getSongsLiveDate().observeForever {
            (binding.musicRecyclerView.adapter as MusicListAdapter).setSongList(it)
            MusicServiceViewModel.getInstance().getSongList().postValue(it)
        }

        MusicServiceViewModel.getInstance().getPlayingSong().observeForever {
            if (it != null) {
                binding.collapsingToolbarLayout.title = it.songTitle
                binding.playingSongAlbumPic.setImageURI(it.albumUri, this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PermissionUtils.returnPermissionCheck(this, requestCode)
    }

    private fun setContentScrimColorFromPaletteDraweeView(
        imageView: PaletteDraweeView,
        ctLayout: CollapsingToolbarLayout
    ) {
        imageView.palette.observeForever {
            if (it != null) {
                var oldColor =
                    imageView.oldPalette?.getDarkVibrantColor(Color.LTGRAY) ?: Color.LTGRAY
                if (isLightColor(oldColor)) oldColor =
                    imageView.oldPalette?.getDarkMutedColor(Color.LTGRAY) ?: Color.LTGRAY
                var plColor = it.getDarkVibrantColor(Color.LTGRAY)
                if (isLightColor(plColor)) plColor = it.getDarkMutedColor(Color.LTGRAY)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ColorAnimator(oldColor, plColor).setColorChangedListener { color ->
                        ctLayout.setContentScrimColor(color)
                    }.start(600)
                } else {
                    ctLayout.setContentScrimColor(plColor)
                }
            }
        }
    }

    private fun isLightColor(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                Toast.makeText(this, "开始", Toast.LENGTH_SHORT).show()
                musicConn.binder?.play()
            }
            R.id.appbar_pause -> {
                Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show()
                musicConn.binder?.pause()
            }
            R.id.appbar_add_folder -> {
                MediaUtils.updateSongDataBase(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicConn)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}