package com.lalilu.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lalilu.media.database.LMusicDatabase
import org.jetbrains.annotations.Nullable

class LMusicMediaModule private constructor(application: Application) :
    AndroidViewModel(application) {

    val database: LMusicDatabase = LMusicDatabase.getInstance(application)
    val mediaScanner = AudioMediaScanner(application, database)

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