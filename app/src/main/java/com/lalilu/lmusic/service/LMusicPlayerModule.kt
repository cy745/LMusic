package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cm55.kanhira.KakasiDictReader
import com.cm55.kanhira.Kanhira
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.LAST_METADATA
import com.lalilu.lmusic.Config.LAST_PLAYBACK_STATE
import com.lalilu.lmusic.utils.KanaToRomaji
import com.lalilu.lmusic.utils.Mathf
import com.tencent.mmkv.MMKV
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import java.util.*
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext


@Singleton
@ExperimentalCoroutinesApi
class LMusicPlayerModule @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val logger = Logger.getLogger(this.javaClass.name)
    private val mmkv = MMKV.defaultMMKV()

    var mediaController: MediaControllerCompat? = null
    private var controllerCallback: MusicControllerCallback = MusicControllerCallback()
    private var connectionCallback: MusicConnectionCallback = MusicConnectionCallback(context)
    private var subscriptionCallback: MusicSubscriptionCallback = MusicSubscriptionCallback()
    private var mediaBrowser: MediaBrowserCompat = MediaBrowserCompat(
        context, ComponentName(context, MSongService::class.java),
        connectionCallback, null
    )
    private val mKanhira = MutableStateFlow<Kanhira?>(null).also {
        launch {
            it.emit(
                Kanhira(
                    KakasiDictReader.load(
                        context.resources.openRawResource(R.raw.kakasidict_utf_8),
                        Charsets.UTF_8.name()
                    )
                )
            )
        }
    }
    private val kanaToRomaji = KanaToRomaji()
    private val format = HanyuPinyinOutputFormat().also {
        it.caseType = HanyuPinyinCaseType.UPPERCASE
        it.toneType = HanyuPinyinToneType.WITHOUT_TONE
        it.vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    private val _metadata = MutableStateFlow(
        mmkv.decodeParcelable(LAST_METADATA, MediaMetadataCompat::class.java)
    )
    private val _playBackState = MutableStateFlow(
        mmkv.decodeParcelable(LAST_PLAYBACK_STATE, PlaybackStateCompat::class.java).let {
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_STOPPED,
                it?.position ?: 0L,
                it?.playbackSpeed ?: 1.0f
            ).build()
        }
    )
    private val _mediaItems: MutableStateFlow<MutableList<MediaBrowserCompat.MediaItem>> =
        MutableStateFlow(ArrayList())
    private val _keyword: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mediaItems: Flow<List<MediaBrowserCompat.MediaItem>> =
        _mediaItems.combine(_metadata) { items, metadata ->
            listOrderChange(items, metadata?.description?.mediaId) ?: items
        }.combine(mKanhira) { items, kanhira ->
            if (kanhira == null) return@combine items

            items.onEach {
                val originStr = "${it.description.title} ${it.description.subtitle}"
                var resultStr = originStr
                val isContainChinese = isContainChinese(originStr)
                val isContainKatakanaOrHinagana = isContainKatakanaOrHinagana(originStr)
                if (isContainChinese || isContainKatakanaOrHinagana) {
                    if (isContainChinese) {
                        val chinese = PinyinHelper.toHanYuPinyinString(originStr, format, "", true)
                        resultStr = "$resultStr $chinese"
                    }

                    val japanese = kanhira.convert(originStr)
                    val romaji = kanaToRomaji.convert(japanese)
                    resultStr = "$resultStr $romaji"
                }
                it.description.extras?.putString("searchStr", resultStr)
            }
        }.combine(_keyword) { items, keyword ->
            if (keyword == null || TextUtils.isEmpty(keyword)) return@combine items

            val keywords = keyword.split(" ")

            return@combine items.filter { item ->
                val originStr = item.description.extras?.getString("searchStr")
                checkKeywords(originStr, keywords)
            }
        }

    val playBackState: LiveData<PlaybackStateCompat> = _playBackState.asLiveData()
    val metadata: LiveData<MediaMetadataCompat?> = _metadata.asLiveData()
    val mediaItemsLiveData: LiveData<List<MediaBrowserCompat.MediaItem>> = mediaItems.asLiveData()

    fun searchFor(keyword: String?) = launch {
        _keyword.emit(keyword)
    }

    fun connect() = mediaBrowser.connect()
    fun disconnect() = mediaBrowser.disconnect().also {
        mediaController?.unregisterCallback(controllerCallback)
    }

    fun subscribe(parentId: String) = mediaBrowser.subscribe(parentId, subscriptionCallback)

    inner class MusicConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(controllerCallback)
            }

            subscribe(Config.MEDIA_ID_EMPTY_ROOT)
            logger.info("[MusicConnectionCallback]#onConnected")
        }
    }

    inner class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            launch {
                _mediaItems.emit(children)
                logger.info("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
            }
        }
    }

    inner class MusicControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            launch {
                _playBackState.emit(state ?: return@launch)
                mmkv.encode(LAST_PLAYBACK_STATE, state)
                logger.info("[MusicControllerCallback]#onPlaybackStateChanged: ${state.state} ${state.position}")
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            launch {
                _metadata.emit(metadata ?: return@launch)
                mmkv.encode(LAST_METADATA, metadata)
                logger.info("[MusicControllerCallback]#onMetadataChanged: ${metadata.description?.title}")
            }
        }
    }

    private fun listOrderChange(
        oldList: List<MediaBrowserCompat.MediaItem>,
        mediaId: String?
    ): MutableList<MediaBrowserCompat.MediaItem>? {
        mediaId ?: return null

        val nowPosition = oldList.indexOfFirst { item ->
            item.description.mediaId == mediaId
        }
        if (nowPosition == -1) return null

        return ArrayList(oldList.map { song ->
            val position = Mathf.clampInLoop(
                0, oldList.size - 1, oldList.indexOf(song), nowPosition
            )
            oldList[position]
        })
    }

    private fun checkKeywords(str: CharSequence?, keywords: List<String>): Boolean {
        keywords.forEach { keyword ->
            if (!checkKeyword(str, keyword)) return false
        }
        return true
    }

    private fun checkKeyword(str: CharSequence?, keyword: String): Boolean {
        str ?: return false
        return str.toString().uppercase(Locale.getDefault()).contains(
            keyword.uppercase(Locale.getDefault())
        )
    }

    private fun isContainChinese(str: String): Boolean {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find()
    }

    private fun isContainKatakanaOrHinagana(str: String): Boolean {
        return Pattern.compile("[\u3040-\u309f]").matcher(str).find() ||
                Pattern.compile("[\u30a0-\u30ff]").matcher(str).find()
    }
}
