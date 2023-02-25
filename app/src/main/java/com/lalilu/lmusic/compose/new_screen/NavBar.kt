package com.lalilu.lmusic.compose.new_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lalilu.R
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.Destination
import com.lalilu.lmusic.compose.new_screen.destinations.DictionariesScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.DictionaryDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.FavouriteScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.HomeScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.NetDataScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SearchScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SettingsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongsScreenDestination
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.popUpElse
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

object NavBar {

    private val destinationTitleMap = mapOf(
        HomeScreenDestination.route to R.string.destination_label_library,
        FavouriteScreenDestination.route to R.string.destination_label_favourite,
        PlaylistsScreenDestination.route to R.string.destination_label_playlists,
        SearchScreenDestination.route to R.string.destination_label_search,
        SongsScreenDestination.route to R.string.destination_label_all_song,
        SongDetailScreenDestination.route to R.string.destination_label_song_detail,
        AlbumsScreenDestination.route to R.string.destination_label_albums,
        AlbumDetailScreenDestination.route to R.string.destination_label_album_detail,
        ArtistsScreenDestination.route to R.string.destination_label_artist,
        ArtistDetailScreenDestination.route to R.string.destination_label_artist_detail,
        DictionariesScreenDestination.route to R.string.destination_label_dictionaries,
        DictionaryDetailScreenDestination.route to R.string.destination_label_dictionary_detail,
        SettingsScreenDestination.route to R.string.destination_label_settings,
        NetDataScreenDestination.route to R.string.destination_label_match_network_data
    )

    enum class NavItem(
        val titleRes: Int,
        val iconRes: Int,
        val destination: DirectionDestinationSpec
    ) {
        Home(
            R.string.destination_label_library,
            R.drawable.ic_loader_line,
            HomeScreenDestination
        ),
        Favourite(
            R.string.destination_label_favourite,
            R.drawable.ic_heart_3_line,
            FavouriteScreenDestination
        ),
        Playlists(
            R.string.destination_label_playlists,
            R.drawable.ic_play_list_line,
            PlaylistsScreenDestination
        ),
        Search(
            R.string.destination_label_search,
            R.drawable.ic_search_2_line,
            SearchScreenDestination
        );

        companion object {
            fun contain(destination: Destination): Boolean {
                return values().any { it.destination == destination }
            }
        }
    }

    val content: @Composable () -> Unit by lazy { { Content() } }

    private fun NavController.navTo(
        currentDestinationSpec: Destination?,
        targetDestinationSpec: DirectionDestinationSpec
    ) {
        if (currentDestinationSpec == targetDestinationSpec) return

        navigate(targetDestinationSpec) {
            popUpTo(graph.findStartDestination().id) {
                inclusive = false
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun Content(navController: NavController = LocalNavigatorHost.current) {
        val destination by navController.appCurrentDestinationAsState()
        val previous = destinationTitleMap[navController.previousBackStackEntry?.destination?.route]
        val showMainBar by remember {
            derivedStateOf { destination != null && NavItem.contain(destination!!) }
        }

        AnimatedContent(targetState = showMainBar) { showMain ->
            if (showMain) {
                Row(
                    modifier = Modifier
                        .height(52.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    NavItem.values().forEach {
                        NavigateItem(
                            titleRes = { it.titleRes },
                            iconRes = { it.iconRes },
                            isSelected = { it.destination == destination },
                            onClick = { navController.navTo(destination, it.destination) }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .clickable(enabled = false) {}
                        .height(52.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val contentColor =
                        contentColorFor(backgroundColor = MaterialTheme.colors.background)
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor
                        ),
                        onClick = {
                            navController.popUpElse {
                                SmartModalBottomSheet.hide()
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                            contentDescription = "backButtonIcon",
                            colorFilter = ColorFilter.tint(color = contentColor)
                        )
                        Text(
                            text = previous?.let { stringResource(id = it) } ?: "返回",
                            fontSize = 14.sp
                        )
                    }
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color(0x25FE4141),
                            contentColor = Color(0xFFFE4141)
                        ),
                        onClick = { SmartModalBottomSheet.hide() }
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_bottom_sheet_navigator_close),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigateItem(
    titleRes: () -> Int,
    iconRes: () -> Int,
    isSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    baseColor: Color = MaterialTheme.colors.primary,
    unSelectedColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
) {
    val titleValue = stringResource(id = titleRes())
    val iconTintColor = animateColorAsState(if (isSelected()) baseColor else unSelectedColor)
    val backgroundColor by animateColorAsState(if (isSelected()) baseColor.copy(alpha = 0.12f) else Color.Transparent)

    Surface(
        color = backgroundColor,
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier
            .size(48.dp)
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = iconRes()),
                    contentDescription = titleValue,
                    colorFilter = ColorFilter.tint(iconTintColor.value),
                    contentScale = FixedScale(if (isSelected()) 1.1f else 1f)
                )
                AnimatedVisibility(visible = isSelected()) {
                    Text(
                        text = titleValue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.1.sp,
                        color = dayNightTextColor()
                    )
                }
            }
        }
    }
}
