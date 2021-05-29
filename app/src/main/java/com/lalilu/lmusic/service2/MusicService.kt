package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmusic.MusicApplication
import com.lalilu.lmusic.R
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.utils.AudioMediaScanner
import com.lalilu.lmusic.utils.NotificationUtils

class MusicService : MediaBrowserServiceCompat() {
    companion object {
        const val Access_ID = "access_id"
        const val Empty_ID = "empty_id"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioMediaScanner: AudioMediaScanner
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var musicSessionCallback: MusicSessionCallback

    override fun onCreate() {
        super.onCreate()
        audioMediaScanner = (application as MusicApplication).audioMediaScanner

        musicPlayer = MusicPlayer(this)
        musicSessionCallback = MusicSessionCallback(musicPlayer)

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            setCallback(musicSessionCallback)
            setSessionToken(sessionToken)
//            setPlaybackState(PlaybackState)
            isActive = true
        }
    }

    private fun sendNotification(context: Context) {
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata ?: return
        val description = mediaMetadata.description

        val builder = NotificationCompat.Builder(this, NotificationUtils.playerChannelName).apply {
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)

            setContentIntent(controller.sessionActivity)

            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            setSmallIcon(R.drawable.ic_launcher_foreground)
            color = Color.DKGRAY

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_pause_line,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }
        startForeground(1, builder.build())
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(Access_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == Empty_ID) return

        val songList = MusicDatabase.getInstance(this).songDao().getAll()
        val resultList: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()

        for (song: Song in songList) {
            val metaDate = MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.songTitle)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, song.albumId.toString())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.albumArtist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.songDuration)
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, song.songId.toString())
                .putString(MediaMetadata.METADATA_KEY_MEDIA_URI, song.songUri.toString())
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, song.albumUri.toString())
                .putString(
                    MediaMetadata.METADATA_KEY_ART_URI,
                    audioMediaScanner.loadThumbnail(song).toString()
                ).build()
            resultList.add(
                MediaBrowserCompat.MediaItem(
                    metaDate.description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            )
        }

        result.sendResult(resultList)
    }
}