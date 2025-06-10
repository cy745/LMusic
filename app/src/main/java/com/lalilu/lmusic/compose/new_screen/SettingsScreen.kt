package com.lalilu.lmusic.compose.new_screen

import android.annotation.SuppressLint
import android.media.MediaScannerConnection
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.component.IconTextButton
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.extension.DynamicTipsItem
import com.lalilu.component.extension.rememberFixedStatusBarHeightDp
import com.lalilu.component.lumo.components.RadioButton
import com.lalilu.component.lumo.components.card.CardDefaults
import com.lalilu.component.lumo.components.card.OutlinedCard
import com.lalilu.component.settings.SettingCategory
import com.lalilu.component.settings.SettingStateAccordion
import com.lalilu.component.settings.SettingSwitcher
import com.lalilu.crash.CrashHelper
import com.lalilu.lmedia.scanner.FileSystemScanner
import com.lalilu.lmusic.GuidingActivity
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lplayer.MPlayerKV
import com.lalilu.lplayer.extensions.PlayMode
import com.lalilu.lplayer.utils.EQHelper
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.System
import com.lalilu.remixicon.Weather
import com.lalilu.remixicon.design.contrastLine
import com.lalilu.remixicon.media.orderPlayFill
import com.lalilu.remixicon.media.repeatOneFill
import com.lalilu.remixicon.media.shuffleFill
import com.lalilu.remixicon.system.settings4Line
import com.lalilu.remixicon.weather.moonLine
import com.lalilu.remixicon.weather.sunLine
import com.zhangke.krouter.annotation.Destination
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Destination("/pages/settings")
object SettingsScreen : Screen, ScreenInfoFactory {
    private fun readResolve(): Any = SettingsScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.screen_title_settings) },
            icon = RemixIcon.System.settings4Line,
        )
    }

    @Composable
    override fun Content() {
        SettingsScreen()
    }
}


@SuppressLint("PrivateApi")
@Composable
private fun SettingsScreen(
    eqHelper: EQHelper = koinInject(),
    settingsSp: SettingsSp = koinInject(),
    fileSystemScanner: FileSystemScanner = koinInject()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkModeOption = settingsSp.darkModeOption
    val enableUnknownFilter = settingsSp.enableUnknownFilter
    val enableSystemEq = settingsSp.enableSystemEq
    val enableDynamicTips = settingsSp.enableDynamicTips
    val forceHideStatusBar = settingsSp.forceHideStatusBar

    val launcherForAudioFx = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
    }

    LazyColumn(
        contentPadding = PaddingValues(top = rememberFixedStatusBarHeightDp())
    ) {
        item {
            NavigatorHeader(
                title = stringResource(id = R.string.screen_title_settings),
                subTitle = stringResource(id = R.string.destination_subtitle_settings)
            )
        }

        item {
            SettingCategory(
                modifier = Modifier.padding(bottom = 16.dp),
                iconRes = R.drawable.ic_settings_4_line,
                titleRes = R.string.preference_player_settings
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colors.surface)
                ) {
                    Spacer(Modifier.height(12.dp))
                    val playModeOptions = remember { listOf("列表循环", "单曲循环", "随机播放") }
                    val selectedOption = remember {
                        mutableStateOf(
                            playModeOptions.getOrNull(PlayMode.from(MPlayerKV.playMode.value).index)
                                ?: playModeOptions.first()
                        )
                    }

                    SettingStateAccordion(
                        title = "播放模式",
                        subTitle = "当前为：${selectedOption.value}",
                        content = {
                            playModeOptions.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            MPlayerKV.playMode.value = PlayMode.indexOf(index).name
                                            selectedOption.value = option

                                            DynamicTipsItem.Static(
                                                title = option,
                                                subTitle = "切换播放模式",
                                            ).show()
                                        }
                                        .padding(start = 16.dp, end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = remember {
                                            when (index) {
                                                0 -> RemixIcon.Media.orderPlayFill
                                                1 -> RemixIcon.Media.repeatOneFill
                                                else -> RemixIcon.Media.shuffleFill
                                            }
                                        },
                                        tint = MaterialTheme.colors.onBackground,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = option,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colors.onBackground
                                    )
                                    RadioButton(
                                        selected = index == PlayMode.from(MPlayerKV.playMode.value).index,
                                        onClick = {
                                            MPlayerKV.playMode.value = PlayMode.indexOf(index).name
                                            selectedOption.value = option

                                            DynamicTipsItem.Static(
                                                title = option,
                                                subTitle = "切换播放模式",
                                            ).show()
                                        }
                                    )
                                }
                            }
                        }
                    )

                    SettingSwitcher(
                        title = stringResource(R.string.preference_player_settings_ignore_audio_focus),
                        onStateUpdate = { MPlayerKV.handleAudioFocus.value = !it },
                        state = { !(MPlayerKV.handleAudioFocus.value ?: false) }
                    )
                    SettingSwitcher(
                        title = "当耳机断开连接时暂停播放",
                        subTitle = "推荐开启，有效避免社死",
                        onStateUpdate = { MPlayerKV.handleBecomeNoisy.value = it },
                        state = { MPlayerKV.handleBecomeNoisy.value ?: true }
                    )

                    SettingSwitcher(
                        title = "是否重启后自动续播",
                        subTitle = "谨慎开启，避免社死",
                        onStateUpdate = { MPlayerKV.autoPlayWhenRestart.value = it },
                        state = { MPlayerKV.autoPlayWhenRestart.value == true }
                    )
//                    SettingProgressSeekBar(
//                        value = { 0f },
//                        onValueUpdate = { },
//                        title = "独立音量控制",
//                        valueRange = 0..100
//                    )
                    SettingSwitcher(
                        state = enableSystemEq,
                        title = "启用系统均衡器",
                        subTitle = "实验性功能，存在较大机型差异"
                    )
                    AnimatedVisibility(visible = enableSystemEq.value) {
                        Row(
                            Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconTextButton(
                                text = "系统均衡器",
                                iconPainter = painterResource(id = R.drawable.equalizer_line),
                                showIcon = { true },
                                color = Color(0xFF006E7C),
                                onClick = {
                                    eqHelper.startSystemEqActivity {
                                        launcherForAudioFx.launch(it)
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        item {
            SettingCategory(
                modifier = Modifier.padding(bottom = 16.dp),
                iconRes = R.drawable.ic_scan_line,
                titleRes = R.string.preference_media_source_settings
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colors.surface)
                ) {
                    Spacer(Modifier.height(12.dp))
//                    SettingProgressSeekBar(
//                        state = settingsSp.durationFilter,
//                        title = "筛除小于时长的文件",
//                        valueRange = 0..60
//                    )
                    SettingSwitcher(
                        state = enableUnknownFilter,
                        titleRes = R.string.preference_media_source_settings_unknown_filter,
                        subTitleRes = R.string.preference_media_source_tips
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        item {
            SettingCategory(
                modifier = Modifier.padding(bottom = 16.dp),
                icon = painterResource(id = R.drawable.ic_loader_line),
                title = "其他"
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colors.surface)
                ) {
                    Spacer(Modifier.height(12.dp))
                    SettingSwitcher(
                        title = "全局隐藏状态栏",
                        subTitle = "简化界面显示效果",
                        state = forceHideStatusBar,
                    )

                    SettingStateAccordion(
                        title = stringResource(R.string.preference_dark_mode),
                        subTitle = "切换深色模式控制",
                        content = {
                            stringArrayResource(id = R.array.dark_mode_options)
                                .forEachIndexed { index, option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                darkModeOption.value = index
                                            }
                                            .padding(start = 16.dp, end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(20.dp),
                                            imageVector = remember {
                                                when (index) {
                                                    0 -> RemixIcon.Design.contrastLine
                                                    1 -> RemixIcon.Weather.sunLine
                                                    else -> RemixIcon.Weather.moonLine
                                                }
                                            },
                                            tint = MaterialTheme.colors.onBackground,
                                            contentDescription = null
                                        )
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = option,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colors.onBackground
                                        )
                                        RadioButton(
                                            selected = index == darkModeOption.value,
                                            onClick = { darkModeOption.value = index }
                                        )
                                    }
                                }
                        }
                    )
                    SettingSwitcher(
                        state = enableDynamicTips,
                        titleRes = R.string.preference_media_source_settings_enable_dynamic_tips,
                        subTitleRes = R.string.preference_dynamic_tips
                    )
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconTextButton(
                            text = "新手引导",
                            color = Color(0xFF3EA22C),
                            onClick = {
                                context.getActivity()?.apply {
                                    ActivityUtils.startActivity(GuidingActivity::class.java)
                                }
                            })

                        IconTextButton(
                            text = "日志分享",
                            color = Color(0xFF0040FF),
                            onClick = {
                                scope.launch {
                                    context.getActivity()?.apply {
                                        CrashHelper.shareLog(this)
                                    } ?: run {
                                        ToastUtils.showShort("日志分享失败")
                                    }
                                }
                            }
                        )

                        IconTextButton(
                            text = "MediaStore重新扫描",
                            color = Color(0xFFFF8B3F),
                            onClick = {
                                Toast.makeText(context, "扫描开始", Toast.LENGTH_SHORT).show()
                                // TODO 存在扫描不到的情况，改进方向为先遍历出fileList然后交由其进行scanFile
                                MediaScannerConnection.scanFile(
                                    context, arrayOf("/storage/emulated/0/"), null
                                ) { path, uri ->
                                    Toast.makeText(context, "扫描结束", Toast.LENGTH_SHORT).show()
                                    LogUtils.i("MediaScannerConnection", "path: $path, uri: $uri")
                                }
                            }
                        )

                        IconTextButton(
                            text = "FileSystem重新扫描",
                            color = Color(0xFFFF8B3F),
                            onClick = {
                                Toast.makeText(context, "扫描开始", Toast.LENGTH_SHORT).show()
                                fileSystemScanner.updateAsync()
                            }
                        )
                        IconTextButton(
                            text = "备份数据",
                            color = Color(0xFFFF8B3F),
                            onClick = {
                                ToastUtils.showShort("重做中...")
//                            val json = settingsSp.backup()
//                            clipboardManager.setText(AnnotatedString(json))
                            }
                        )
                        IconTextButton(
                            text = "恢复数据",
                            color = Color(0xFFFF8B3F),
                            onClick = {
                                ToastUtils.showShort("重做中...")
//                            val json = clipboardManager.getText()?.text
//                            json?.let { settingsSp.restore(it) }
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        smartBarPadding()
    }
}