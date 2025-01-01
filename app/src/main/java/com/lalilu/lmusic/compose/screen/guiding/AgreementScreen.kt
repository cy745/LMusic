package com.lalilu.lmusic.compose.screen.guiding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lalilu.R
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import kotlin.system.exitProcess

class AgreementScreen(
    private val nextScreen: Screen
) : Screen, ScreenInfoFactory {

    @Composable
    override fun provideScreenInfo(): ScreenInfo {
        return remember {
            ScreenInfo(title = { stringResource(R.string.screen_title_agreement) })
        }
    }

    @Composable
    override fun Content() {
        AgreementPage(
            nextScreen = nextScreen
        )
    }
}

@Composable
private fun AgreementPage(
    nextScreen: Screen,
    navigator: Navigator = LocalNavigator.currentOrThrow
) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {
        item {
            ActionCard(
                onReject = { exitProcess(0) },
                onConfirm = { navigator.push(nextScreen) }
            ) {
                """
            使用本应用的用户应知晓以下内容：

            1. 本应用所提供的网络歌词获取功能需要使用网络权限，如无此需求可拒绝网络权限授予；
            2. 本应用所涉及的网络接口调用均不以获取用户个人唯一标识为前提，以此确保用户个人信息和隐私安全；
            3. 本应用本体及代码基于AGPL-3.0开源协议进行开源，任何个人与组织不得将此应用本体及代码应用于商业行为；

            未来此协议可能有扩充的可能性，认可本协议内容即视为同意未来的变更。
            不会加广告，不会收费，大可放心。
                
            酷安@邱邱邱Qiu  v1.4.12  2022/05/17
            """
            }
        }
    }
}
