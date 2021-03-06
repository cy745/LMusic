package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.card.DestinationCard
import com.lalilu.lmusic.utils.WindowSize

@Composable
fun LibraryScreen(
    currentWindowSize: WindowSize,
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigatorHeader(route = MainScreenData.Library)
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (currentWindowSize == WindowSize.Expanded) 3 else 2),
            contentPadding = PaddingValues(20.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MainScreenData.values().forEach {
                if (it.showNavigateButton) {
                    item {
                        DestinationCard(
                            route = it,
                            navigateTo = navigateTo
                        )
                    }
                }
            }
        }
    }
}
