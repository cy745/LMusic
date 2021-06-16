package com.lalilu.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lalilu.media.database.LMusicDatabase
import org.jetbrains.annotations.Nullable

class LMusicMediaContainer private constructor(application: Application) :
    AndroidViewModel(application) {

    val database: LMusicDatabase = LMusicDatabase.getInstance(application)
    val mediaItemLiveData = database.mediaItemDao().getAllLiveData()
    val mediaScanner = AudioMediaScanner(application, database)

    companion object {
        @Volatile
        private var instance: LMusicMediaContainer? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): LMusicMediaContainer {
            instance ?: synchronized(LMusicMediaContainer::class.java) {
                if (application == null) throw NullPointerException("No Application Context Input")
                instance = LMusicMediaContainer(application)
            }
            return instance!!
        }
    }
}