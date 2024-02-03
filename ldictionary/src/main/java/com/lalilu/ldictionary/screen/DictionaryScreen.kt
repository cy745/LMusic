package com.lalilu.ldictionary.screen

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.ldictionary.R
import com.lalilu.lmedia.repository.LMediaSp
import com.lalilu.lmedia.scanner.FileSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import me.rosuh.filepicker.FilePickerActivity
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager
import com.lalilu.component.R as ComponentR

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryScreenModel(
    private val application: Application,
    private val lMediaSp: LMediaSp,
) : ScreenModel {
    val targetDirectory = lMediaSp.includePath
        .flow(true)
        .mapLatest { str -> FileSource.from(str, application) }

    fun saveTargetUri(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        application.contentResolver.takePersistableUriPermission(uri, flags)

        lMediaSp.includePath.add(uri.toString())
    }

    fun savePaths(strList: List<String>) {
        lMediaSp.includePath.add(strList)
    }

    fun remove(str: String) {
        lMediaSp.includePath.remove(str)
    }
}

object DictionaryScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.dictionary_screen_title,
        icon = ComponentR.drawable.ic_disc_line
    )

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val dictionarySM = getScreenModel<DictionaryScreenModel>()
        val pickFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { treeUri ->
            treeUri?.let { dictionarySM.saveTargetUri(it) }
            LogUtils.i(treeUri)
        }
        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                val result = FilePickerManager.obtainData(true)

                dictionarySM.savePaths(result)
                LogUtils.i(it.data, it.resultCode, result)
            }
        )

        RegisterActions {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.dictionary_screen_title,
                    icon = ComponentR.drawable.ic_add_line,
                    color = Color(0xFF037200)
                ) {
                    runCatching { pickFileLauncher.launch(null) }
                        .getOrElse {
                            val activity =
                                ActivityUtils.getActivityByContext(context) ?: return@StaticAction
                            FilePickerManager.from(activity)
                                .skipDirWhenSelect(false)
                                .maxSelectable(Int.MAX_VALUE)
                                .filter(object : AbstractFileFilter() {
                                    override fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {
                                        return ArrayList(listData.filter { it.isDir })
                                    }
                                })
                            val intent = Intent(activity, FilePickerActivity::class.java)
                            filePickerLauncher.launch(intent)
                        }
                }
            )
        }

        DictionaryScreen(dictionarySM = dictionarySM)
    }
}


@Composable
private fun DictionaryScreen(
    dictionarySM: DictionaryScreenModel
) {
    val targetDirectory = dictionarySM.targetDirectory.collectAsLoadingState()

    LoadingScaffold(
        modifier = Modifier.fillMaxSize(),
        targetState = targetDirectory,
    ) { directory ->
        LLazyColumn(
            modifier = Modifier,
            contentPadding = WindowInsets.statusBars.asPaddingValues()
        ) {
            item {
                NavigatorHeader(
                    title = "文件夹",
                    subTitle = "长按以移除该文件夹"
                )
            }

            items(items = directory) {
                DirectoryCard(
                    title = it.name() ?: "unknown",
                    subTitle = it.path() ?: "unknown",
                    onLongClick = {
                        val id = when (it) {
                            is FileSource.Document -> it.id
                            is FileSource.IOFile -> it.id
                        }
                        dictionarySM.remove(id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectoryCard(
    title: String,
    subTitle: String,
    onLongClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = {}
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        Text(text = subTitle, style = MaterialTheme.typography.subtitle2)
    }
}

@Preview
@Composable
fun DirectoryCardPreview() {
    DirectoryCard(
        title = "LocalMusic",
        subTitle = "/Music/LocalMusic/"
    )
}