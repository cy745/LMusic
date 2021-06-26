package com.lalilu.media

import android.app.Application
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.AndroidViewModel
import com.lalilu.media.database.LMusicDatabase
import com.lalilu.media.scanner.LMusicMediaScanner
import org.jetbrains.annotations.Nullable

class LMusicMediaModule private constructor(application: Application) :
    AndroidViewModel(application) {

    val database: LMusicDatabase = LMusicDatabase.getInstance(application)
    val mediaScanner = LMusicMediaScanner(application, database)

    fun getMediaMetaData(): List<MediaMetadataCompat> {
        return database.mediaItemDao().getAll().map { it.toMediaMetaData() }
    }

    companion object {
        @Volatile
        private var instance: LMusicMediaModule? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): LMusicMediaModule {
            instance ?: synchronized(LMusicMediaModule::class.java) {
                if (application == null) throw NullPointerException("No Application Context Input")
                instance = LMusicMediaModule(application)
            }
            return instance!!
        }
    }
}