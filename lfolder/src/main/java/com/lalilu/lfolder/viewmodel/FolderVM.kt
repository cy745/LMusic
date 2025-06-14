package com.lalilu.lfolder.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.lalilu.lmedia.repository.LMediaKV
import com.lalilu.lmedia.scanner.FileSource
import kotlinx.coroutines.flow.mapLatest
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FolderVM(private val application: Application) : ViewModel() {
    val targetDirectory = LMediaKV.includePath.flow()
        .mapLatest { str -> FileSource.from(str, application) }

    fun saveTargetUri(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        application.contentResolver.takePersistableUriPermission(uri, flags)

        LMediaKV.includePath.value += uri.toString()
    }

    fun savePaths(strList: List<String>) {
        LMediaKV.includePath.value += strList
    }

    fun remove(str: String) {
        LMediaKV.includePath.value -= str
    }
}