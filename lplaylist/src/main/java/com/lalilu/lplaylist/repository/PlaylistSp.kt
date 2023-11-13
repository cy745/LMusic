package com.lalilu.lplaylist.repository

import android.app.Application
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp
import com.lalilu.lplaylist.entity.LPlaylist

class PlaylistSp(private val context: Application) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences(
            context.packageName + "_PLAYLIST",
            Application.MODE_PRIVATE
        )
    }

    val playlistList = obtainList<LPlaylist>(key = "PLAYLIST", autoSave = false)
}