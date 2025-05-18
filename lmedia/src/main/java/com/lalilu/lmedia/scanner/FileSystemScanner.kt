package com.lalilu.lmedia.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.lalilu.common.base.SourceType
import com.lalilu.common.toUpdatableFlow
import com.lalilu.lmedia.entity.FileInfo
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.LMediaKV
import com.lalilu.lmedia.repository.LMediaSp
import com.lalilu.lmedia.wrapper.Taglib
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder

sealed class FileSource {
    open fun canRead(): Boolean = false
    open fun isDirectory(): Boolean = false
    open fun listFiles(): List<FileSource> = emptyList()
    open fun mimeType(): String? = null
    open fun name(): String? = null
    open fun path(): String? = null
    open fun length(): Long = 0

    data class IOFile(
        val id: String,
        val file: File
    ) : FileSource() {
        override fun name(): String? = file.name
        override fun length(): Long = file.length()
        override fun mimeType(): String? = file.extension
        override fun path(): String? = file.absolutePath
        override fun canRead(): Boolean = file.exists() && file.canRead()
        override fun isDirectory(): Boolean = file.isDirectory
        override fun listFiles(): List<FileSource> =
            file.listFiles()?.map { IOFile(it.path, it) } ?: emptyList()
    }

    data class Document(
        val id: String,
        val file: DocumentFile
    ) : FileSource() {
        override fun name(): String? = file.name
        override fun length(): Long = file.length()
        override fun mimeType(): String? = file.type
        override fun path(): String? = file.uri.toString()
        override fun canRead(): Boolean = file.exists() && file.canRead()
        override fun isDirectory(): Boolean = file.isDirectory
        override fun listFiles(): List<FileSource> =
            file.listFiles().map { Document(it.uri.toString(), it) }
    }

    companion object {
        fun from(strList: List<String>?, context: Context): List<FileSource> {
            if (strList == null) return emptyList()

            val persistedUri = context.contentResolver.persistedUriPermissions
            return strList.mapNotNull { str ->
                var fileSource: FileSource? = runCatching { Uri.parse(str) }.getOrNull()
                    ?.takeIf { uri -> persistedUri.any { it.uri.path == uri.path && it.isReadPermission } }
                    ?.let { DocumentFile.fromTreeUri(context, it) }
                    ?.let { Document(str, it) }

                if (fileSource == null) {
                    fileSource = runCatching { File(str) }.getOrNull()
                        ?.takeIf { it.exists() && it.isDirectory && it.canRead() }
                        ?.let { IOFile(str, it) }
                }

                fileSource
            }
        }
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
class FileSystemScanner(
    private val context: Context,
    lMediaSp: LMediaSp
) : MediaSource<LSong> {

    /**
     * 使用 isDirty 标记是否需要重新进行加载
     */
    private var isDirty = false
    override fun updateAsync() {
        isDirty = true
        resultFlow.requireUpdate()
    }

    override fun requireFlow(): Flow<List<LSong>> = resultFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    private val resultFlow = lMediaSp.includePath.flow(true)
        .mapLatest { uriStr -> FileSource.from(uriStr, context) }
        .mapLatest { fileSources ->
            // 先尝试从缓存加载，缓存无结果则尝试扫描读取
            loadCache(fileSources) ?: loadItems(fileSources)
                .awaitAll()
                .filterNotNull()
                .also { saveCache(it) }
        }
        .toUpdatableFlow()

    private fun loadCache(fileSources: List<FileSource>): List<LSong>? {
        if (isDirty) {
            isDirty = false
            return null
        }
        if (fileSources.isEmpty()) return null

        val result = fileSources.mapNotNull { fileSource ->
            val id = when (fileSource) {
                is FileSource.Document -> fileSource.id
                is FileSource.IOFile -> fileSource.id
            }
            val md5ForDirectory = EncryptUtils.encryptMD5ToString(id).take(8)

            LMediaKV.obtainList<LSong>(md5ForDirectory)
                .get()
                ?.takeIf { it.isNotEmpty() }
        }.flatten()
            .takeIf { it.isNotEmpty() }

        LogUtils.i("Cache Loaded: songs: ${result?.size}")
        return result
    }

    private fun saveCache(songs: List<LSong>) {
        if (songs.isEmpty()) return

        val directoryMap = songs.groupBy { it.fileInfo.directoryPath }
        for (entry in directoryMap) {
            val md5ForDirectory = EncryptUtils.encryptMD5ToString(entry.key).take(8)

            LMediaKV.obtainList<LSong>(md5ForDirectory)
                .set(entry.value)
        }

        LogUtils.i("Cache Saved: songs: ${songs.size} directory: ${directoryMap.keys.size}")
    }

    private suspend fun loadItems(fileSources: List<FileSource>): List<Deferred<LSong?>> =
        withContext(Dispatchers.IO) {
            val childResult = fileSources
                .filter { it.isDirectory() }
                .map { async { loadItems(it.listFiles()) } }

            val currentResult = fileSources
                .map { loadItem(it) }

            currentResult + childResult
                .awaitAll()
                .flatten()
        }

    private suspend fun loadItem(fileSource: FileSource): Deferred<LSong?> =
        withContext(Dispatchers.IO) {
            async {
                if (!fileSource.canRead() || fileSource.isDirectory()) return@async null

                val pathStr = when (fileSource) {
                    is FileSource.Document -> fileSource.file.uri.toString()
                    is FileSource.IOFile -> fileSource.file.absolutePath
                }.let { URLDecoder.decode(it, "UTF-8") }

                val directoryPath = FileUtils.getDirName(pathStr)
                    ?.takeIf(String::isNotEmpty)
                    ?: "Unknown dir"

                val uri = when (fileSource) {
                    is FileSource.Document -> fileSource.file.uri
                    is FileSource.IOFile -> fileSource.file.toUri()
                }

                val metadata = runCatching {
                    context.contentResolver.openFileDescriptor(uri, "r")
                }.getOrElse {
                    LogUtils.e(it)
                    null
                }?.use { Taglib.retrieveMetadataWithFD(it.detachFd()) }
                    ?: return@async null

                LSong(
                    id = uri.toString(),
                    metadata = metadata,
                    uri = uri,
                    fileInfo = FileInfo(
                        mimeType = fileSource.mimeType() ?: "",
                        pathStr = pathStr,
                        directoryPath = directoryPath,
                        fileName = fileSource.name(),
                        size = fileSource.length()
                    ),
                    sourceType = SourceType.Local
                )
            }
        }
}