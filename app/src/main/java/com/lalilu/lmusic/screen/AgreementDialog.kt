package com.lalilu.lmusic.screen

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import kotlin.system.exitProcess

@Composable
fun AgreementDialog() {
    var isAgree by rememberDataSaverState("IS_AGREE_WITH_CONTRACT_AgreementDialog", false)
    if (isAgree) return

    AlertDialog(
        onDismissRequest = { isAgree = false },
        confirmButton = {
            TextButton(onClick = { isAgree = true }) {
                Text(text = "确认")
            }
        },
        dismissButton = {
            TextButton(onClick = { exitProcess(0) }) {
                Text(text = "拒绝")
            }
        },
        title = {
            Text(text = "用户协议 | 隐私协议")
        },
        text = {
            Text(
                """
                使用本应用的用户应知晓以下内容：

                1. 本应用所提供的网络歌词获取功能需要使用网络权限，如无此需求可拒绝网络权限授予；
                2. 本应用所涉及的网络接口调用均不以获取用户个人唯一标识为前提，以此确保用户个人信息和隐私安全；
                3. 本应用本体及代码基于AGPL-3.0开源协议进行开源，任何个人与组织不得将此应用本体及代码应用于商业行为；

                未来此协议可能有扩充的可能性，认可本协议内容即视为同意未来的变更。
                不会加广告，不会收费，大可放心。
                
                酷安@邱邱邱Qiu  v1.4.12  2022/05/17
                """.trimIndent()
            )
        },
        shape = RoundedCornerShape(20.dp)
    )
}