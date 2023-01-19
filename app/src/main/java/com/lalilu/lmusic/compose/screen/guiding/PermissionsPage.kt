package com.lalilu.lmusic.compose.screen.guiding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import kotlin.system.exitProcess

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsPage(navController: NavController) {
    val permission = rememberPermissionState(permission = REQUIRE_PERMISSIONS)

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {
        when (permission.status) {
            PermissionStatus.Granted -> {
                ActionCard(
                    confirmTitle = "已授权，下一步",
                    onConfirm = { GuidingNavGraph.Permissions.getNext()?.navigate(navController) }
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