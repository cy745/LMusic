package com.lalilu.lmusic.compose.component.settings

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState
import com.lalilu.R
import okio.buffer
import okio.sink
import okio.source
import java.io.File

@Composable
fun SettingFilePicker(
    state: DataSaverMutableState<String>,
    title: String,
    subTitle: String? = null,
    mimeType: String
) {
    var value by state
    val context = LocalContext.current
    var fileName by remember(value) {
        mutableStateOf(value.split('/').last())
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        // 若是可直接访问的File则直接
        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            uri.path?.let { value = it }
            uri.path?.split('/')?.last()?.let {
                fileName = it
            }
            return@rememberLauncherForActivityResult
        }

        // 若为 Scheme 为 content，则需要将文件复制到私有目录下
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            var displayName: String? = null
            val contentResolver = context.contentResolver

            // 通过ContentResolver查询uri对应的文件的 DISPLAY_NAME
            contentResolver.query(uri, null, null, null, null)
                ?.apply {
                    if (moveToFirst()) {
                        val index = getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        if (index >= 0) displayName = getString(index)
                    }
                    close()
                }

            try {
                val bSource = contentResolver.openInputStream(uri)
                    ?.source()
                    ?.buffer()
                    ?: return@rememberLauncherForActivityResult

                fileName = displayName?.takeIf { it.isNotEmpty() }
                    ?: "${System.currentTimeMillis()}_${Math.random() * 1000}.ext"

                val path = "${context.cacheDir.absolutePath}/$fileName"

                val bSink = File(path).outputStream()
                    .sink()
                    .buffer()
                bSink.write(bSource.readByteArray())
                bSink.flush()
                bSink.close()
                bSource.close()
                value = path
            } catch (_: Exception) {
            }
        }
    }

    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { pickFileLauncher.launch(mimeType) })
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp
            )
            Text(
                text = fileName.takeIf { it.isNotEmpty() } ?: subTitle ?: "",
                fontSize = 12.sp,
                color = textColor.copy(0.5f)
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
            tint = textColor,
            contentDescription = ""
        )
    }
}
