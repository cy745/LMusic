package com.lalilu.lmusic

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hjq.permissions.XXPermissions
import com.lalilu.lmusic.scanner.MSongScanner
import com.lalilu.lmusic.utils.ToastUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LMusicApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var songScanner: MSongScanner

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        XXPermissions.setScopedStorage(true)

        songScanner.setScanStart {
            toastUtil.show("[开始扫描]: 共计 $it 首歌曲")
            println("[开始扫描]: 共计 $it 首歌曲")
        }.setScanFailed {
            toastUtil.show(it)
        }.scanStart(this)
    }
}