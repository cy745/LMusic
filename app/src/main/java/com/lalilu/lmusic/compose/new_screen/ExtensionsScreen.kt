package com.lalilu.lmusic.compose.new_screen

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.ExtensionHostScreenDestination
import com.lalilu.lmusic.utils.extension.rememberFixedStatusBarHeightDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun ExtensionsScreen(
    context: Context = LocalContext.current,
    navigator: DestinationsNavigator
) {
    val results by ExtensionManager.extensionsFlow.collectAsState()
    val statusBarHeight = rememberFixedStatusBarHeightDp()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = statusBarHeight)
    ) {
        item {
            NavigatorHeader(
                title = "Extensions",
                subTitle = "Extensions available in the app"
            ) {
                IconButton(onClick = { ExtensionManager.loadExtensions(context) }) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        }

        items(items = results) { extensionResult ->
            when (extensionResult) {
                is ExtensionLoadResult.Ready -> {
                    Surface(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = 1.dp,
                        onClick = {
                            navigator.navigate(
                                ExtensionHostScreenDestination(packageName = extensionResult.packageInfo.packageName)
                            )
                        }
                    ) {
                        extensionResult.Place(
                            contentKey = "banner",
                            errorPlaceHolder = {
                                Text(text = "LoadError ${extensionResult.packageInfo.packageName}")
                            },
                        )
                    }
                }

                is ExtensionLoadResult.Error -> {
                    Text(text = "Error: ${extensionResult.message}")
                }

                is ExtensionLoadResult.OutOfDated -> {
                    Text(text = "OutOfDate")
                }
            }
        }
    }
}

class ExtensionsViewModel(
    private val context: Application
) : ViewModel() {

}