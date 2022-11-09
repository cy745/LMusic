package com.lalilu.lmusic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lalilu.lmusic.Config
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryDataStore @Inject constructor(
    @ApplicationContext context: Context,
) : BaseDataStore() {
    override val dataStore by lazy { context.dataStore }
    override val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "HISTORY", produceMigrations = {
            listOf(SharedPreferencesMigration(it, Config.LAST_PLAYED_SP))
        }
    )

    val lastPlayedIdKey = stringPreferencesKey(Config.LAST_PLAYED_ID)
    val lastPlayedPositionKey = longPreferencesKey(Config.LAST_PLAYED_POSITION)
    val lastPlayedListIdsKey = stringListPreferencesKey(Config.LAST_PLAYED_LIST, temp = true)
}