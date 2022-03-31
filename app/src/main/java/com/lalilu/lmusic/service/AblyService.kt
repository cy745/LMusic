package com.lalilu.lmusic.service

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.apis.bean.ShareDto
import com.lalilu.lmusic.utils.getByResId
import com.lalilu.lmusic.utils.listen
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.Channel
import io.ably.lib.realtime.ChannelBase
import io.ably.lib.types.ClientOptions
import io.ably.lib.types.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

const val STATE_OFFLINE = "st_offline"
const val STATE_ONLINE = "st_online"
const val STATE_LISTENING = "st_listening"

const val CHANNEL_NORMAL = "cn_normal"

@Singleton
@ExperimentalCoroutinesApi
class AblyService @Inject constructor(
    @ApplicationContext val context: Context
) : AblyRealtime(ClientOptions().also {
    it.autoConnect = false
    it.authUrl = Config.ABLY_AUTH_BASE_URL
}), DefaultLifecycleObserver, CoroutineScope, ChannelBase.MessageListener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val settingsSp: SharedPreferences =
        context.getSharedPreferences(Config.SETTINGS_SP, Context.MODE_PRIVATE)

    val maxAvailableDuration = 5 * 60 * 1000
    var availableMessageFilter: (Map.Entry<String, Message>, now: Long) -> Boolean = { pair, now ->
        (now - pair.value.timestamp) <= maxAvailableDuration && pair.value.name != STATE_OFFLINE
    }

    val listenChannel: Channel = channels.get(CHANNEL_NORMAL)

    val latestSharedDto: MutableStateFlow<ShareDto?> =
        MutableStateFlow(null)
    private val latestReceivedMessage: MutableStateFlow<Message?> =
        MutableStateFlow(null)
    private val history: MutableStateFlow<LinkedHashMap<String, Message>> =
        MutableStateFlow(LinkedHashMap())
    private val isEnable: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val historyLiveData = history.combine(latestReceivedMessage) { map, msg ->
        val now = System.currentTimeMillis()
        if (msg != null) map[msg.connectionId] = msg
        map.filter { availableMessageFilter(it, now) }
    }.combine(isEnable) { map, isEnable ->
        if (isEnable) map else null
    }.asLiveData()

    fun launchAbly() {
        if (settingsSp.getByResId(R.string.sp_key_ably_service_enable, false) != true)
            return
        connect()
    }

    fun shutdownAbly() {
        listenChannel.publish(STATE_OFFLINE, STATE_OFFLINE)
        close()
    }

    override fun onMessage(message: Message) {
        if (connection?.id == message.connectionId) return
        latestReceivedMessage.tryEmit(message)
        println(
            """
            [channel]
            msg.id: ${message.id}
            msg.name: ${message.name}
            msg.data: ${message.data}
            msg.encoding: ${message.encoding}
            msg.clientId: ${message.clientId}
            msg.connectionId: ${message.connectionId}
            msg.timestamp: ${message.timestamp}
            """.trimIndent()
        )
    }

    override fun onCreate(owner: LifecycleOwner) {
        launchAbly()
    }

    override fun onResume(owner: LifecycleOwner) {
        latestReceivedMessage.tryEmit(null)
    }

    init {
        settingsSp.listen(R.string.sp_key_ably_service_enable, false) { enable ->
            if (enable) launchAbly() else shutdownAbly()
            isEnable.tryEmit(enable)
        }

        latestSharedDto.mapLatest {
            it ?: return@mapLatest null
            Message(STATE_LISTENING, it.toJson())
        }.distinctUntilChanged()
            .onEach {
                it ?: return@onEach
                listenChannel.publish(it)
            }.launchIn(this)

        connection.on { connectState ->
            if (connectState.current.name == "connected") {
                listenChannel.publish(STATE_ONLINE, STATE_ONLINE)
            }
            println(
                """
                    [connection: ${connection?.state?.name}]
                    it.event.name: ${connectState?.event?.name}
                    it.current.name: ${connectState?.current?.name}
                    it.reason.message: ${connectState?.reason?.message}
                    """.trimIndent()
            )
        }

        listenChannel.subscribe(this)
    }
}