package com.lalilu.lmusic.service

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.lalilu.R
import com.lalilu.lmusic.Config
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

const val STATE_OFFLINE = "st_offline"
const val STATE_ONLINE = "st_online"
const val STATE_LISTENING = "st_listening"

const val CHANNEL_NORMAL = "cn_normal"

@Singleton
class AblyService @Inject constructor(
    @ApplicationContext val context: Context
) : DefaultLifecycleObserver, CoroutineScope,
    ChannelBase.MessageListener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val settingsSp: SharedPreferences =
        context.getSharedPreferences(Config.SETTINGS_SP, Context.MODE_PRIVATE)

    var ably: AblyRealtime? = null
        get() = field ?: createAbly()
    val listenChannel: Channel?
        get() = ably?.let { getChannel(it) }

    private val newestMessage: MutableStateFlow<Message?> =
        MutableStateFlow(null)
    private val history: MutableStateFlow<LinkedHashMap<String, Message>> =
        MutableStateFlow(LinkedHashMap())
    private val isEnable: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val historyLiveData = history.combine(newestMessage) { map, msg ->
        msg ?: return@combine map.filter { pair ->
            (now - pair.value.timestamp) <= 300000 && pair.value.name != STATE_OFFLINE
        }
        map[msg.connectionId] = msg
        val now = System.currentTimeMillis()
        return@combine map.filter { pair ->
            (now - pair.value.timestamp) <= 300000 && pair.value.name != STATE_OFFLINE
        }
    }.combine(isEnable) { map, isEnable ->
        if (isEnable) map else null
    }.asLiveData()

    fun getChannel(ablyRealtime: AblyRealtime): Channel {
        return ablyRealtime.channels.get(CHANNEL_NORMAL)
    }

    private fun createAbly(): AblyRealtime? {
        if (settingsSp.getByResId(R.string.sp_key_ably_service_enable, false) != true)
            return null

        return AblyRealtime(ClientOptions().also {
            it.autoConnect = false
            it.authUrl = Config.ABLY_AUTH_BASE_URL
        }).also {
            it.connection.on { connectState ->
                if (connectState.current.name == "connected") {
                    getChannel(it).let { channel ->
                        channel.subscribe(this)
                        channel.publish(STATE_ONLINE, STATE_ONLINE)
                    }
                }
                println(
                    """
                    [connection]
                    it.event.name: ${connectState?.event?.name}
                    it.current.name: ${connectState?.current?.name}
                    it.reason.message: ${connectState?.reason?.message}
                    """.trimIndent()
                )
            }
        }
    }

    fun shutdownAbly() {
        ably ?: return
        listenChannel?.publish(STATE_OFFLINE, STATE_OFFLINE)
        listenChannel?.unsubscribe(this)
        ably?.close()
        ably = null
    }

    override fun onMessage(message: Message) {
        if (ably?.connection?.id == message.connectionId) return
        newestMessage.tryEmit(message)
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
        ably = ably ?: createAbly()
        ably?.connect()
    }

    override fun onResume(owner: LifecycleOwner) {
        newestMessage.tryEmit(null)
    }

//    override fun onResume(owner: LifecycleOwner) {
//        ably = ably ?: createAbly()
//        history.getAndUpdate { map -> map.also { it.clear() } }
//        ably?.connect()
//    }

//    override fun onStop(owner: LifecycleOwner) {
//        shutdownAbly()
//    }

    init {
        settingsSp.listen(R.string.sp_key_ably_service_enable, false) { enable ->
            if (enable) {
                ably = ably ?: createAbly()
                history.getAndUpdate { map -> map.also { it.clear() } }
                ably?.connect()
            } else shutdownAbly()
            isEnable.tryEmit(enable)
        }
    }
}