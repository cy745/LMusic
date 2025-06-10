package com.lalilu.component.settings

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.lalilu.component.R
import okio.buffer
import okio.sink
import okio.source
import java.io.File

@Composable
fun SettingFilePicker(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    title: String,
    subTitle: String? = null,
    mimeType: String
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    FileSelectWrapper(state = state) { launcher, fileName ->
        SettingBaseItem(
            modifier = modifier,
            title = title,
            subTitle = fileName.value.takeIf { it.isNotEmpty() } ?: subTitle ?: "",
            onContentStartClick = { launcher.launch(mimeType) },
            contentEnd = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
                    tint = textColor,
                    contentDescription = ""
                )
            }
        )
    }
}

@Composable
fun FileSelectWrapper(
    state: MutableState<String> = remember { mutableStateOf("") },
    content: @Composable (
        pickFileLauncher: ManagedActivityResultLauncher<String, Uri?>,
        fileNameState: State<String>
    ) -> Unit
) {
    var value by state
    val fileName = remember {
        derivedStateOf {
            value.split('/').last()
        }
    }

    val context = LocalContext.current
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        // 若是可直接访问的File则直接
        if (uri.scheme == ContentResolver.SCHEME_FILE && !uri.path.isNullOrEmpty()) {
            value = uri.path!!
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
            val name = displayName?.takeIf { it.isNotEmpty() }
                ?: "${System.currentTimeMillis()}_${Math.random() * 1000}.tff"
            val path = "${context.cacheDir.absolutePath}/$name"

            try {
                contentResolver.openInputStream(uri).use { stream ->
                    stream?.source()?.buffer()?.use { source ->
                        File(path).outputStream().sink().buffer().use {
                            it.write(source.readByteArray())
                            it.flush()
                        }
                    }
                }
                value = path
            } catch (_: Exception) {
            }
        }
    }

    content(pickFileLauncher, fileName)
}