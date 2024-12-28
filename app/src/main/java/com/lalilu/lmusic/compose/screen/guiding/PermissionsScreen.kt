package com.lalilu.lmusic.compose.screen.guiding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ActivityUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.lalilu.R
import com.lalilu.component.base.CustomScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.lmedia.LMedia
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.MainActivity
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.getActivity
import org.koin.compose.koinInject
import kotlin.system.exitProcess

class PermissionsScreen(
) : CustomScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_permissions
    )

    @Composable
    override fun Content() {
        PermissionsPage()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionsPage(
    settingsSp: SettingsSp = koinInject()
) {
    val permission = rememberPermissionState(permission = REQUIRE_PERMISSIONS)
    var isGuidingOver by settingsSp.isGuidingOver
    val context = LocalContext.current

    LaunchedEffect(permission.status) {
        if (permission.status is PermissionStatus.Granted) {
            LMedia.init(context)
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {
        when (permission.status) {
            PermissionStatus.Granted -> {
                ActionCard(
                    confirmTitle = "已授权，进入",
                    onConfirm = {
                        context.getActivity()?.apply {
                            isGuidingOver = true

                            if (!ActivityUtils.isActivityExistsInStack(MainActivity::class.java)) {
                                ActivityUtils.startActivity(MainActivity::class.java)
                            }
                            finishAfterTransition()
                        }
                    }
                ) {
                    """
                    本应用需要获取本地存储权限，以访问本机存储的所有歌曲文件
                    """
                }
            }

            is PermissionStatus.Denied -> {
                ActionCard(
                    rejectTitle = "拒绝并退出",
                    confirmTitle = "授权",
                    onReject = { exitProcess(0) },
                    onConfirm = { permission.launchPermissionRequest() }
                ) {
                    """
                    本应用需要获取本地存储权限，以访问本机存储的所有歌曲文件
                    """
                }
            }
        }
    }
}