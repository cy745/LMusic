package com.lalilu.lmusic.compose.component.settings

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.lalilu.R

@Composable
fun SettingExtensionSwitcher(
    state: MutableState<Boolean>,
    initialed: Boolean,
    title: String,
    subTitle: String? = null,
    startDownload: () -> Unit = {}
) {
    var value by state
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    SettingSwitcher(
        contentStart = {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp
            )
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    color = contentColorFor(
                        backgroundColor = MaterialTheme.colors.background
                    ).copy(0.5f)
                )
            }
        }
    ) { interaction ->
        if (initialed) {
            Switch(
                checked = value,
                onCheckedChange = { value = it },
                interactionSource = interaction,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                        .multiply(0.7f)
                )
            )
        } else {
            IconButton(onClick = startDownload) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download_cloud_2_line),
                    contentDescription = "下载插件"
                )
            }
        }
    }
}