package com.lalilu.lmusic.service.pusher

import android.app.Service
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import coil.imageLoader
import coil.request.ImageRequest
import com.blankj.utilcode.util.ImageUtils
import com.google.protobuf.kotlin.toByteString
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.utils.extension.getMediaId
import com.lalilu.lmusic.utils.extension.toBitmap
import com.lalilu.remote.MusicServiceGrpc
import com.lalilu.remote.MusicServiceGrpc.MusicServiceBlockingStub
import com.lalilu.remote.MusicServiceGrpc.MusicServiceStub
import com.lalilu.remote.MusicServiceOuterClass
import com.lalilu.remote.MusicServiceOuterClass.HandleResult
import com.lalilu.remote.MusicServiceOuterClass.MusicInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Singleton
class RemotePusher @Inject constructor(
    @ApplicationContext mContext: Context,
    private val coverRepo: CoverRepository
) : Pusher, StreamObserver<HandleResult>, CoroutineScope {
    override var getService: () -> Service? = { null }
    override var getMediaSession: () -> MediaSessionCompat? = { null }
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val blockingStub: MusicServiceBlockingStub? by lazy {
        val channel = ManagedChannelBuilder.forAddress("192.168.2.240", 10054)
            .usePlaintext()
            .build()
        MusicServiceGrpc.newBlockingStub(channel)
    }

    private val stub: MusicServiceStub? by lazy {
        val channel = ManagedChannelBuilder.forAddress("192.168.2.240", 10054)
            .usePlaintext()
            .build()
        MusicServiceGrpc.newStub(channel)
    }

    private val pushFlow = MutableStateFlow(System.currentTimeMillis())

    init {
        pushFlow.debounce(50)
            .flatMapLatest {
                val mediaSession = getMediaSession.invoke()
                mediaSession?.controller?.metadata?.description?.let {
                    blockingStub?.updateMusicInfo(
                        MusicInfo.newBuilder()
                            .setTitle(it.title.toString())
                            .setArtist(it.subtitle.toString())
                            .setDuration(0)
                            .setMediaId("0")
                            .build()
                    )
                }

                val mediaId = mediaSession?.getMediaId()
                coverRepo.fetch(mediaId).mapLatest { data ->
                    val bitmap = mContext.imageLoader.execute(
                        ImageRequest.Builder(mContext)
                            .allowHardware(false)
                            .data(data)
                            .build()
                    ).drawable?.toBitmap()

                    stub?.updateMusicCover(this)
                        ?.apply {
                            ImageUtils.bitmap2Bytes(bitmap)?.toByteString()?.let {
                                onNext(
                                    MusicServiceOuterClass.CoverInfo.newBuilder()
                                        .setBytes(it)
                                        .build()
                                )
                            }
                            onCompleted()
                        }
                }
            }.launchIn(this)
    }

    override fun update() {
        pushFlow.tryEmit(System.currentTimeMillis())
    }

    override fun cancel() {

    }

    override fun destroy() {
        getService = { null }
        getMediaSession = { null }
    }

    override fun onNext(value: HandleResult?) {
        println("[onNext]: $value")
    }

    override fun onError(t: Throwable?) {
        t?.printStackTrace()
    }

    override fun onCompleted() {
        println("[onCompleted]: RemotePusher")
    }
}