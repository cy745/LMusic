package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionActivityPendingIntent

@Module
@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
class MSongObjModule {

    @Provides
    @SessionActivityPendingIntent
    fun provideSessionActivityPendingIntent(
        @ApplicationContext context: Context,
    ): PendingIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        .let { sessionIntent ->
            PendingIntent.getActivity(
                context, 0,
                sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context
    ): MediaSessionCompat = MediaSessionCompat(
        context, MSongService::class.java.name
    )
}