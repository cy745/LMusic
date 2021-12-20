package com.lalilu.lmusic.event

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.database.LMusicDataBase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: LMusicDataBase
) : ViewModel() {


}