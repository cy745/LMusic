package com.lalilu.lmusic.utils.coil.fetcher

import coil.intercept.Interceptor
import coil.request.ImageRequest
import coil.request.ImageResult
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LMusicImageInterceptor @Inject constructor(
    database: MDataBase
) : Interceptor {
    val dao = database.networkDataDao()

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val song = chain.request.data
        if (song !is LSong) return chain.proceed(chain.request)

        val cover = dao.getById(song.id)?.cover
            ?: return chain.proceed(chain.request)

        chain.proceed(
            ImageRequest.Builder(chain.request)
                .data(cover)
                .build()
        ).takeIf { it.drawable != null }
            ?.let { return it }

        return chain.proceed(chain.request)
    }
}