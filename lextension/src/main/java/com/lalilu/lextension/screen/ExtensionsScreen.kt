package com.lalilu.lextension.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenInfo
import com.lalilu.lextension.R
import com.lalilu.lextension.component.ExtensionList
import com.lalilu.lextension.component.ExtensionsScreenModel
import com.lalilu.component.R as ComponentR

object ExtensionsScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.extension_screen_title,
        icon = ComponentR.drawable.ic_shapes_line
    )

    @Composable
    override fun Content() {
        val extensionsSM = getScreenModel<ExtensionsScreenModel>()

        ExtensionsScreen(extensionsSM = extensionsSM)
    }
}

@Composable
private fun DynamicScreen.ExtensionsScreen(
    extensionsSM: ExtensionsScreenModel
) {
    ExtensionList(
        extensionsSM = extensionsSM,
        headerContent = { extension ->
            item {
                NavigatorHeader(
                    modifier = Modifier.statusBarsPadding(),
                    title = stringResource(id = R.string.extension_screen_title),
                    subTitle = "共 ${extension.size} 个扩展"
                )
            }
        },
        footerContent = {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.clickable {
                            extensionsSM.isEditing.value = !extensionsSM.isEditing.value
                        },
                        text = if (extensionsSM.isEditing.value) "Save" else "Edit",
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    )
}