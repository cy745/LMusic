package com.lalilu.lmusic.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lalilu.lmusic.Config
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext context: Context,
) : BaseDataStore() {
    override val dataStore by lazy { context.dataStore }
    override val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "SETTINGS",
//        produceMigrations = { context ->
//            listOf(SharedPreferencesMigration(context, context.packageName))
//        }
    )

    val repeatModeKey = intPreferencesKey(Config.KEY_SETTINGS_REPEAT_MODE)
}