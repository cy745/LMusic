package com.lalilu.lmusic.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryDataStore @Inject constructor(
    @ApplicationContext context: Context,
) : BaseDataStore() {
    override val dataStore by lazy { context.dataStore }
    override val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "LIBRARY")

    val today = intPreferencesKey("DAY_OF_YEAR")
    val dailyRecommends = stringListPreferencesKey("DAILY_RECOMMENDS", temp = true)
}