package com.lalilu.lmusic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        name = "SETTINGS", produceMigrations = { context ->
            listOf(SharedPreferencesMigration(context, context.packageName))
        }
    )

    val playMode = intPreferencesKey(Config.KEY_SETTINGS_PLAY_MODE)
    val lyricTextSize = intPreferencesKey(Config.KEY_SETTINGS_LYRIC_TEXT_SIZE)
    val lyricGravity = intPreferencesKey(Config.KEY_SETTINGS_LYRIC_GRAVITY)
    val seekBarHandler = intPreferencesKey(Config.KEY_SETTINGS_SEEKBAR_HANDLER)
    val ignoreAudioFocus = booleanPreferencesKey(Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS)
    val volumeControl = intPreferencesKey(Config.KEY_SETTINGS_VOLUME_CONTROL)
    val lyricTypefaceUri = stringPreferencesKey(Config.KEY_SETTINGS_LYRIC_TYPEFACE_URI)
    val enableStatusLyric = booleanPreferencesKey(Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE)
    val enableSystemEq = booleanPreferencesKey(Config.KEY_SETTINGS_ENABLE_SYSTEM_EQ)

    val isGuidingOver = booleanPreferencesKey(Config.KEY_REMEMBER_IS_GUIDING_OVER)


    val songsSortRule = stringPreferencesKey("SONGS_SORT_RULE")
    val albumsSortRule = stringPreferencesKey("ALBUMS_SORT_RULE")
    val artistsSortRule = stringPreferencesKey("ARTISTS_SORT_RULE")

    val songsOrderRule = stringPreferencesKey("SONGS_ORDER_RULE")
    val albumsOrderRule = stringPreferencesKey("ALBUMS_ORDER_RULE")
    val artistsOrderRule = stringPreferencesKey("ARTISTS_ORDER_RULE")

    val songsGroupRule = stringPreferencesKey("SONGS_GROUP_RULE")
    val albumsGroupRule = stringPreferencesKey("ALBUMS_GROUP_RULE")
    val artistsGroupRule = stringPreferencesKey("ARTISTS_GROUP_RULE")
}