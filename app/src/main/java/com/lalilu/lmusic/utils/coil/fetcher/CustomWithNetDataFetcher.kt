package com.lalilu.lmusic.utils.coil.fetcher

import android.content.Context
import android.os.Looper
import android.os.NetworkOnMainThreadException
import coil.network.HttpException
import coil.request.Options
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.internal.closeQuietly
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class CustomWithNetDataFetcher(
    private val callFactory: Call.Factory,
    private val options: Options,
) : BaseFetcher() {
    override suspend fun fetchForSong(context: Context, song: LSong): InputStream? {
        return fetchImageFromNet(song) ?: super.fetchForSong(context, song)
    }

    private suspend fun fetchImageFromNet(song: LSong): InputStream? {
        val url = song.artworkUri?.takeIf { it.scheme == "http" || it.scheme == "https" }
            ?.toString() ?: return null

        val response = executeNetworkRequest(newRequest(url))
        return response.body?.byteStream()
    }

    private suspend fun executeNetworkRequest(request: Request): Response {
        val response = if (Looper.myLooper() == Looper.getMainLooper()) {
            if (options.networkCachePolicy.readEnabled) {
                // Prevent executing requests on the main thread that could block due to a
                // networking operation.
                throw NetworkOnMainThreadException()
            } else {
                // Work around: https://github.com/Kotlin/kotlinx.coroutines/issues/2448
                callFactory.newCall(request).execute()
            }
        } else {
            // Suspend and enqueue the request on one of OkHttp's dispatcher threads.
            callFactory.newCall(request).await()
        }
        if (!response.isSuccessful && response.code != HttpURLConnection.HTTP_NOT_MODIFIED) {
            response.body?.closeQuietly()
            throw HttpException(response)
        }
        return response
    }

    private fun newRequest(url: String): Request {
        val request = Request.Builder()
            .url(url)
            .headers(options.headers)

        val diskRead = options.diskCachePolicy.readEnabled
        val networkRead = options.networkCachePolicy.readEnabled
        when {
            !networkRead && diskRead -> {
                request.cacheControl(CacheControl.FORCE_CACHE)
            }
            networkRead && !diskRead -> if (options.diskCachePolicy.writeEnabled) {
                request.cacheControl(CacheControl.FORCE_NETWORK)
            } else {
                request.cacheControl(CACHE_CONTROL_FORCE_NETWORK_NO_CACHE)
            }
            !networkRead && !diskRead -> {
                // This causes the request to fail with a 504 Unsatisfiable Request.
                request.cacheControl(CACHE_CONTROL_NO_NETWORK_NO_CACHE)
            }
        }

        return request.build()
    }

    companion object {
        private const val MIME_TYPE_TEXT_PLAIN = "text/plain"
        private val CACHE_CONTROL_FORCE_NETWORK_NO_CACHE =
            CacheControl.Builder().noCache().noStore().build()
        private val CACHE_CONTROL_NO_NETWORK_NO_CACHE =
            CacheControl.Builder().noCache().onlyIfCached().build()
    }

    private suspend fun Call.await(): Response {
        return suspendCancellableCoroutine { continuation ->
            val callback = ContinuationCallback(this, continuation)
            enqueue(callback)
            continuation.invokeOnCancellation(callback)
        }
    }

    private class ContinuationCallback(
        private val call: Call,
        private val continuation: CancellableContinuation<Response>
    ) : Callback, CompletionHandler {

        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            if (!call.isCanceled()) {
                continuation.resumeWithException(e)
            }
        }

        override fun invoke(cause: Throwable?) {
            try {
                call.cancel()
            } catch (_: Throwable) {
            }
        }
    }
}