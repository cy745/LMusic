/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.exoplayer.source

import android.content.Context
import androidx.media3.common.*
import androidx.media3.common.MediaItem.*
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.ads.AdsLoader
import androidx.media3.exoplayer.source.ads.AdsMediaSource
import androidx.media3.exoplayer.text.SubtitleDecoderFactory
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.extractor.*
import androidx.media3.extractor.SeekMap.Unseekable
import androidx.media3.extractor.text.SubtitleExtractor
import com.google.common.base.Supplier
import com.google.common.collect.ImmutableList
import com.google.common.primitives.Ints
import java.io.IOException


@UnstableApi
class ConcatenatingMediaSourceFactory @UnstableApi constructor(
    private val dataSourceFactory: DataSource.Factory?, extractorsFactory: ExtractorsFactory?
) : MediaSource.Factory {

    @Deprecated("Use {@link AdsLoader.Provider} instead. ")
    interface AdsLoaderProvider : AdsLoader.Provider

    private val delegateFactoryLoader: DelegateFactoryLoader =
        DelegateFactoryLoader(dataSourceFactory, extractorsFactory)
    private var serverSideAdInsertionMediaSourceFactory: MediaSource.Factory? = null
    private var adsLoaderProvider: AdsLoader.Provider? = null
    private var adViewProvider: AdViewProvider? = null
    private var loadErrorHandlingPolicy: LoadErrorHandlingPolicy? = null
    private var liveTargetOffsetMs = C.TIME_UNSET
    private var liveMinOffsetMs = C.TIME_UNSET
    private var liveMaxOffsetMs = C.TIME_UNSET
    private var liveMinSpeed = C.RATE_UNSET
    private var liveMaxSpeed = C.RATE_UNSET
    private var useProgressiveMediaSourceForSubtitles = false

    /**
     * Creates a new instance.
     *
     * @param context Any context.
     */
    constructor(context: Context?) : this(DefaultDataSource.Factory(context!!))

    /**
     * Creates a new instance.
     *
     * @param context Any context.
     * @param extractorsFactory An [ExtractorsFactory] used to extract progressive media from
     * its container.
     */
    constructor(context: Context?, extractorsFactory: ExtractorsFactory?) : this(
        DefaultDataSource.Factory(
            context!!
        ), extractorsFactory
    )

    /**
     * Creates a new instance.
     *
     * @param dataSourceFactory A [DataSource.Factory] to create [DataSource] instances
     * for requesting media data.
     */
    @UnstableApi
    constructor(dataSourceFactory: DataSource.Factory?) : this(
        dataSourceFactory,
        DefaultExtractorsFactory()
    ) {
    }

    /**
     * Sets whether a [ProgressiveMediaSource] or [SingleSampleMediaSource] is constructed
     * to handle [MediaItem.LocalConfiguration.subtitleConfigurations]. Defaults to false (i.e.
     * [SingleSampleMediaSource].
     *
     *
     * This method is experimental, and will be renamed or removed in a future release.
     *
     * @param useProgressiveMediaSourceForSubtitles Indicates that [ProgressiveMediaSource]
     * should be used for subtitles instead of [SingleSampleMediaSource].
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun experimentalUseProgressiveMediaSourceForSubtitles(
        useProgressiveMediaSourceForSubtitles: Boolean
    ): ConcatenatingMediaSourceFactory {
        this.useProgressiveMediaSourceForSubtitles = useProgressiveMediaSourceForSubtitles
        return this
    }

    /**
     * Sets the [AdsLoader.Provider] that provides [AdsLoader] instances for media items
     * that have [ads configurations][MediaItem.LocalConfiguration.adsConfiguration].
     *
     * @param adsLoaderProvider A provider for [AdsLoader] instances.
     * @return This factory, for convenience.
     */
    fun setAdsLoaderProvider(
        adsLoaderProvider: AdsLoader.Provider?
    ): ConcatenatingMediaSourceFactory {
        this.adsLoaderProvider = adsLoaderProvider
        return this
    }

    /**
     * Sets the [AdViewProvider] that provides information about views for the ad playback UI.
     *
     * @param adViewProvider A provider for [AdsLoader] instances.
     * @return This factory, for convenience.
     */
    fun setAdViewProvider(adViewProvider: AdViewProvider?): ConcatenatingMediaSourceFactory {
        this.adViewProvider = adViewProvider
        return this
    }

    /**
     * Sets the [MediaSource.Factory] used to handle [MediaItem] instances containing a
     * [Uri] identified as resolving to content with server side ad insertion (SSAI).
     *
     *
     * SSAI URIs are those with a [scheme][Uri.getScheme] of [C.SSAI_SCHEME].
     *
     * @param serverSideAdInsertionMediaSourceFactory The [MediaSource.Factory] for SSAI
     * content, or `null` to remove a previously set [MediaSource.Factory].
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun setServerSideAdInsertionMediaSourceFactory(
        serverSideAdInsertionMediaSourceFactory: MediaSource.Factory?
    ): ConcatenatingMediaSourceFactory {
        this.serverSideAdInsertionMediaSourceFactory = serverSideAdInsertionMediaSourceFactory
        return this
    }

    /**
     * Sets the target live offset for live streams, in milliseconds.
     *
     * @param liveTargetOffsetMs The target live offset, in milliseconds, or [C.TIME_UNSET] to
     * use the media-defined default.
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun setLiveTargetOffsetMs(liveTargetOffsetMs: Long): ConcatenatingMediaSourceFactory {
        this.liveTargetOffsetMs = liveTargetOffsetMs
        return this
    }

    /**
     * Sets the minimum offset from the live edge for live streams, in milliseconds.
     *
     * @param liveMinOffsetMs The minimum allowed live offset, in milliseconds, or [     ][C.TIME_UNSET] to use the media-defined default.
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun setLiveMinOffsetMs(liveMinOffsetMs: Long): ConcatenatingMediaSourceFactory {
        this.liveMinOffsetMs = liveMinOffsetMs
        return this
    }

    /**
     * Sets the maximum offset from the live edge for live streams, in milliseconds.
     *
     * @param liveMaxOffsetMs The maximum allowed live offset, in milliseconds, or [     ][C.TIME_UNSET] to use the media-defined default.
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun setLiveMaxOffsetMs(liveMaxOffsetMs: Long): ConcatenatingMediaSourceFactory {
        this.liveMaxOffsetMs = liveMaxOffsetMs
        return this
    }

    /**
     * Sets the minimum playback speed for live streams.
     *
     * @param minSpeed The minimum factor by which playback can be sped up for live streams, or [     ][C.RATE_UNSET] to use the media-defined default.
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun setLiveMinSpeed(minSpeed: Float): ConcatenatingMediaSourceFactory {
        liveMinSpeed = minSpeed
        return this
    }

    /**
     * Sets the maximum playback speed for live streams.
     *
     * @param maxSpeed The maximum factor by which playback can be sped up for live streams, or [     ][C.RATE_UNSET] to use the media-defined default.
     * @return This factory, for convenience.
     */
    @UnstableApi
    fun setLiveMaxSpeed(maxSpeed: Float): ConcatenatingMediaSourceFactory {
        liveMaxSpeed = maxSpeed
        return this
    }

    @UnstableApi
    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider?
    ): ConcatenatingMediaSourceFactory {
        delegateFactoryLoader.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this
    }

    @UnstableApi
    override fun setLoadErrorHandlingPolicy(
        loadErrorHandlingPolicy: LoadErrorHandlingPolicy?
    ): ConcatenatingMediaSourceFactory {
        this.loadErrorHandlingPolicy = loadErrorHandlingPolicy
        delegateFactoryLoader.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    @UnstableApi
    override fun getSupportedTypes(): IntArray {
        return delegateFactoryLoader.getSupportedTypes()
    }

    @UnstableApi
    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        var mediaItem = mediaItem
        Assertions.checkNotNull(mediaItem.localConfiguration)
        val scheme = mediaItem.localConfiguration!!.uri.scheme
        if (scheme != null && scheme == C.SSAI_SCHEME) {
            return Assertions.checkNotNull(serverSideAdInsertionMediaSourceFactory)
                .createMediaSource(mediaItem)
        }
        val type = Util.inferContentTypeForUriAndMimeType(
            mediaItem.localConfiguration!!.uri, mediaItem.localConfiguration!!.mimeType
        )
        val mediaSourceFactory = delegateFactoryLoader.getMediaSourceFactory(type)
        Assertions.checkStateNotNull(
            mediaSourceFactory, "No suitable media source factory found for content type: $type"
        )
        val liveConfigurationBuilder = mediaItem.liveConfiguration.buildUpon()
        if (mediaItem.liveConfiguration.targetOffsetMs == C.TIME_UNSET) {
            liveConfigurationBuilder.setTargetOffsetMs(liveTargetOffsetMs)
        }
        if (mediaItem.liveConfiguration.minPlaybackSpeed == C.RATE_UNSET) {
            liveConfigurationBuilder.setMinPlaybackSpeed(liveMinSpeed)
        }
        if (mediaItem.liveConfiguration.maxPlaybackSpeed == C.RATE_UNSET) {
            liveConfigurationBuilder.setMaxPlaybackSpeed(liveMaxSpeed)
        }
        if (mediaItem.liveConfiguration.minOffsetMs == C.TIME_UNSET) {
            liveConfigurationBuilder.setMinOffsetMs(liveMinOffsetMs)
        }
        if (mediaItem.liveConfiguration.maxOffsetMs == C.TIME_UNSET) {
            liveConfigurationBuilder.setMaxOffsetMs(liveMaxOffsetMs)
        }
        val liveConfiguration = liveConfigurationBuilder.build()
        // Make sure to retain the very same media item instance, if no value needs to be overridden.
        if (liveConfiguration != mediaItem.liveConfiguration) {
            mediaItem = mediaItem.buildUpon().setLiveConfiguration(liveConfiguration).build()
        }
        var mediaSource = mediaSourceFactory!!.createMediaSource(mediaItem)
        val subtitleConfigurations: List<SubtitleConfiguration> =
            Util.castNonNull(mediaItem.localConfiguration).subtitleConfigurations
        if (subtitleConfigurations.isNotEmpty()) {
            val mediaSources: Array<MediaSource?> = arrayOfNulls(subtitleConfigurations.size + 1)
            mediaSources[0] = mediaSource
            for (i in subtitleConfigurations.indices) {
                if (useProgressiveMediaSourceForSubtitles) {
                    val format = Format.Builder()
                        .setSampleMimeType(subtitleConfigurations[i].mimeType)
                        .setLanguage(subtitleConfigurations[i].language)
                        .setSelectionFlags(subtitleConfigurations[i].selectionFlags)
                        .setRoleFlags(subtitleConfigurations[i].roleFlags)
                        .setLabel(subtitleConfigurations[i].label)
                        .setId(subtitleConfigurations[i].id)
                        .build()
                    val extractorsFactory = ExtractorsFactory {
                        arrayOf(
                            if (SubtitleDecoderFactory.DEFAULT.supportsFormat(format)) {
                                SubtitleExtractor(
                                    SubtitleDecoderFactory.DEFAULT.createDecoder(format), format
                                )
                            } else UnknownSubtitlesExtractor(format)
                        )
                    }
                    mediaSources[i + 1] =
                        ProgressiveMediaSource.Factory(dataSourceFactory!!, extractorsFactory)
                            .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
                            .createMediaSource(
                                fromUri(subtitleConfigurations[i].uri.toString())
                            )
                } else {
                    mediaSources[i + 1] = SingleSampleMediaSource.Factory(dataSourceFactory!!)
                        .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
                        .createMediaSource(
                            subtitleConfigurations[i],  /* durationUs= */
                            C.TIME_UNSET
                        )
                }
            }

            mediaSource = MergingMediaSource(*mediaSources.mapNotNull { it }.toTypedArray())
        }
        return maybeWrapWithAdsMediaSource(mediaItem, maybeClipMediaSource(mediaItem, mediaSource))
    }

    private fun maybeWrapWithAdsMediaSource(
        mediaItem: MediaItem,
        mediaSource: MediaSource
    ): MediaSource {
        Assertions.checkNotNull(mediaItem.localConfiguration)
        val adsConfiguration = mediaItem.localConfiguration!!.adsConfiguration ?: return mediaSource
        val adsLoaderProvider = adsLoaderProvider
        val adViewProvider = adViewProvider
        if (adsLoaderProvider == null || adViewProvider == null) {
            Log.w(
                TAG,
                "Playing media without ads. Configure ad support by calling setAdsLoaderProvider and"
                        + " setAdViewProvider."
            )
            return mediaSource
        }
        val adsLoader = adsLoaderProvider.getAdsLoader(adsConfiguration)
        if (adsLoader == null) {
            Log.w(TAG, "Playing media without ads, as no AdsLoader was provided.")
            return mediaSource
        }
        val adsId = if (adsConfiguration.adsId != null) {
            adsConfiguration.adsId
        } else {
            ImmutableList.of(
                mediaItem.mediaId, mediaItem.localConfiguration!!.uri, adsConfiguration.adTagUri
            )
        }
        return AdsMediaSource(
            mediaSource,
            DataSpec(adsConfiguration.adTagUri),
            /* adsId= */ adsId!!,
            /* adMediaSourceFactory= */ this,
            adsLoader,
            adViewProvider
        )
    }

    /** Loads media source factories lazily.  */
    private class DelegateFactoryLoader(
        private val dataSourceFactory: DataSource.Factory?,
        private val extractorsFactory: ExtractorsFactory?
    ) {
        private val mediaSourceFactorySuppliers: MutableMap<Int, Supplier<MediaSource.Factory>?>
        private val supportedTypes: MutableSet<Int>
        private val mediaSourceFactories: MutableMap<Int, MediaSource.Factory?>
        private var drmSessionManagerProvider: DrmSessionManagerProvider? = null
        private var loadErrorHandlingPolicy: LoadErrorHandlingPolicy? = null
        fun getSupportedTypes(): IntArray {
            ensureAllSuppliersAreLoaded()
            return Ints.toArray(supportedTypes)
        }

        fun getMediaSourceFactory(contentType: @C.ContentType Int): MediaSource.Factory? {
            var mediaSourceFactory = mediaSourceFactories[contentType]
            if (mediaSourceFactory != null) {
                return mediaSourceFactory
            }
            val mediaSourceFactorySupplier = maybeLoadSupplier(contentType) ?: return null
            mediaSourceFactory = mediaSourceFactorySupplier.get()
            if (drmSessionManagerProvider != null) {
                mediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
            }
            if (loadErrorHandlingPolicy != null) {
                mediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            }
            mediaSourceFactories[contentType] = mediaSourceFactory
            return mediaSourceFactory
        }

        fun setDrmSessionManagerProvider(
            drmSessionManagerProvider: DrmSessionManagerProvider?
        ) {
            this.drmSessionManagerProvider = drmSessionManagerProvider
            for (mediaSourceFactory in mediaSourceFactories.values) {
                mediaSourceFactory!!.setDrmSessionManagerProvider(drmSessionManagerProvider)
            }
        }

        fun setLoadErrorHandlingPolicy(
            loadErrorHandlingPolicy: LoadErrorHandlingPolicy?
        ) {
            this.loadErrorHandlingPolicy = loadErrorHandlingPolicy
            for (mediaSourceFactory in mediaSourceFactories.values) {
                mediaSourceFactory!!.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            }
        }

        private fun ensureAllSuppliersAreLoaded() {
            maybeLoadSupplier(C.TYPE_DASH)
            maybeLoadSupplier(C.TYPE_SS)
            maybeLoadSupplier(C.TYPE_HLS)
            maybeLoadSupplier(C.TYPE_RTSP)
            maybeLoadSupplier(C.TYPE_OTHER)
        }

        private fun maybeLoadSupplier(contentType: @C.ContentType Int): Supplier<MediaSource.Factory>? {
            if (mediaSourceFactorySuppliers.containsKey(contentType)) {
                return mediaSourceFactorySuppliers[contentType]
            }
            var mediaSourceFactorySupplier: Supplier<MediaSource.Factory>? = null
            try {
                val clazz: Class<out MediaSource.Factory>
                when (contentType) {
                    C.TYPE_DASH -> {
                        clazz =
                            Class.forName("androidx.media3.exoplayer.dash.DashMediaSource\$Factory")
                                .asSubclass(MediaSource.Factory::class.java)
                        mediaSourceFactorySupplier = Supplier {
                            newInstance(
                                clazz,
                                dataSourceFactory
                            )
                        }
                    }
                    C.TYPE_SS -> {
                        clazz =
                            Class.forName("androidx.media3.exoplayer.smoothstreaming.SsMediaSource\$Factory")
                                .asSubclass(MediaSource.Factory::class.java)
                        mediaSourceFactorySupplier = Supplier {
                            newInstance(
                                clazz,
                                dataSourceFactory
                            )
                        }
                    }
                    C.TYPE_HLS -> {
                        clazz =
                            Class.forName("androidx.media3.exoplayer.hls.HlsMediaSource\$Factory")
                                .asSubclass(MediaSource.Factory::class.java)
                        mediaSourceFactorySupplier = Supplier {
                            newInstance(
                                clazz,
                                dataSourceFactory
                            )
                        }
                    }
                    C.TYPE_RTSP -> {
                        clazz =
                            Class.forName("androidx.media3.exoplayer.rtsp.RtspMediaSource\$Factory")
                                .asSubclass(MediaSource.Factory::class.java)
                        mediaSourceFactorySupplier = Supplier {
                            newInstance(
                                clazz
                            )
                        }
                    }
                    C.TYPE_OTHER -> mediaSourceFactorySupplier =
                        Supplier {
                            ProgressiveMediaSource.Factory(
                                dataSourceFactory!!,
                                extractorsFactory!!
                            )
                        }
                    else -> {}
                }
            } catch (e: ClassNotFoundException) {
                // Expected if the app was built without the specific module.
            }
            mediaSourceFactorySuppliers[contentType] = mediaSourceFactorySupplier
            if (mediaSourceFactorySupplier != null) {
                supportedTypes.add(contentType)
            }
            return mediaSourceFactorySupplier
        }

        init {
            mediaSourceFactorySuppliers = HashMap()
            supportedTypes = HashSet()
            mediaSourceFactories = HashMap()
        }
    }

    private class UnknownSubtitlesExtractor(private val format: Format) :
        Extractor {
        override fun sniff(input: ExtractorInput): Boolean {
            return true
        }

        override fun init(output: ExtractorOutput) {
            val trackOutput = output.track( /* id= */0, C.TRACK_TYPE_TEXT)
            output.seekMap(Unseekable(C.TIME_UNSET))
            output.endTracks()
            trackOutput.format(
                format
                    .buildUpon()
                    .setSampleMimeType(MimeTypes.TEXT_UNKNOWN)
                    .setCodecs(format.sampleMimeType)
                    .build()
            )
        }

        @Throws(IOException::class)
        override fun read(input: ExtractorInput, seekPosition: PositionHolder): Int {
            val skipResult = input.skip(Int.MAX_VALUE)
            return if (skipResult == C.RESULT_END_OF_INPUT) {
                Extractor.RESULT_END_OF_INPUT
            } else Extractor.RESULT_CONTINUE
        }

        override fun seek(position: Long, timeUs: Long) {}
        override fun release() {}
    }

    companion object {
        private const val TAG = "DMediaSourceFactory"

        // internal methods
        private fun maybeClipMediaSource(
            mediaItem: MediaItem,
            mediaSource: MediaSource
        ): MediaSource {
            return if (mediaItem.clippingConfiguration.startPositionMs == 0L && mediaItem.clippingConfiguration.endPositionMs == C.TIME_END_OF_SOURCE && !mediaItem.clippingConfiguration.relativeToDefaultPosition
            ) {
                mediaSource
            } else ClippingMediaSource(
                mediaSource,
                Util.msToUs(mediaItem.clippingConfiguration.startPositionMs),
                Util.msToUs(mediaItem.clippingConfiguration.endPositionMs),  /* enableInitialDiscontinuity= */
                !mediaItem.clippingConfiguration.startsAtKeyFrame,  /* allowDynamicClippingUpdates= */
                mediaItem.clippingConfiguration.relativeToLiveWindow,
                mediaItem.clippingConfiguration.relativeToDefaultPosition
            )
        }

        private fun newInstance(
            clazz: Class<out MediaSource.Factory>, dataSourceFactory: DataSource.Factory?
        ): MediaSource.Factory {
            return try {
                clazz.getConstructor(DataSource.Factory::class.java).newInstance(dataSourceFactory)
            } catch (e: Exception) {
                throw IllegalStateException(e)
            }
        }

        private fun newInstance(clazz: Class<out MediaSource.Factory>): MediaSource.Factory {
            return try {
                clazz.getConstructor().newInstance()
            } catch (e: Exception) {
                throw IllegalStateException(e)
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param dataSourceFactory A [DataSource.Factory] to create [DataSource] instances
     * for requesting media data.
     * @param extractorsFactory An [ExtractorsFactory] used to extract progressive media from
     * its container.
     */
    init {

    }
}