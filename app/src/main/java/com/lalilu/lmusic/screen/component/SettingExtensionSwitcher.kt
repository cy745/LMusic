package com.lalilu.lmusic.screen.component

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState
import com.lalilu.R

@Composable
fun SettingExtensionSwitcher(
    state: DataSaverMutableState<Boolean>,
    initialed: Boolean,
    title: String,
    subTitle: String? = null,
    startDownload: () -> Unit = {}
) {
    var value by state

    SettingSwitcher(
        contentStart = {
            Text(
                text = title,
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