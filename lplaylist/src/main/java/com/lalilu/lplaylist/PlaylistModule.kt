package com.lalilu.lplaylist

import androidx.compose.runtime.collectAsState
import com.lalilu.component.SlotState
import com.lalilu.lplaylist.repository.PlaylistRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.lalilu.lplaylist")
object PlaylistModule


@Single
@Named("favourite_ids")
fun provideFavouriteIds(
    playlistRepo: PlaylistRepository,
) = SlotState {
    playlistRepo.getFavouriteMediaIds()
        .collectAsState(emptyList())
}