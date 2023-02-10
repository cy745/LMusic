package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedSp @Inject constructor(
    @ApplicationContext context: Context
) : BaseSp() {
    override val sp: SharedPreferences by lazy {
        context.getSharedPreferences("BLOCKED", Application.MODE_PRIVATE)
    }

    val blockedPaths = stringSetSp("BLOCKED_PATHS")
}