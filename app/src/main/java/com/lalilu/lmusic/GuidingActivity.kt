package com.lalilu.lmusic

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.LocalDataSaver
import com.lalilu.lmusic.screen.guiding.GuidingScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GuidingActivity : AppCompatActivity() {

    @Inject
    lateinit var dataSaverPreferences: DataSaverPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LMusicTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences) {
                    GuidingScreen()
                }
            }
        }
    }
}