package com.lalilu.lmusic.compose.new_screen

import StatusBarLyric.API.StatusBarLyric
import android.media.MediaScannerConnection
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.RomUtils
import com.lalilu.R
import com.lalilu.lmusic.GuidingActivity
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.component.settings.SettingCategory
import com.lalilu.lmusic.compose.component.settings.SettingFilePicker
import com.lalilu.lmusic.compose.component.settings.SettingProgressSeekBar
import com.lalilu.lmusic.compose.component.settings.SettingStateSeekBar
import com.lalilu.lmusic.compose.component.settings.SettingSwitcher
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.extension.getActivity
import com.ramcosta.composedestinations.annotation.Destination
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun SettingsScreen(
    lMusicSp: LMusicSp = get(),
    statusBarLyricExt: StatusBarLyric = get()
) {
    val context = LocalContext.current
    val darkModeOption = lMusicSp.darkModeOption
    val ignoreAudioFocus = lMusicSp.ignoreAudioFocus
    val enableUnknownFilter = lMusicSp.enableUnknownFilter
    val statusBarLyric = lMusicSp.enableStatusLyric
    val seekbarHandler = lMusicSp.seekBarHandler
    val lyricGravity = lMusicSp.lyricGravity
    val lyricTextSize = lMusicSp.lyricTextSize
    val playMode = lMusicSp.playMode
    val volumeControl = lMusicSp.volumeControl
    val lyricTypefacePath = lMusicSp.lyricTypefacePath
    val enableSystemEq = lMusicSp.enableSystemEq
    val enableDynamicTips = lMusicSp.enableDynamicTips
    val autoHideSeekBar = lMusicSp.autoHideSeekbar

    val launcherForAudioFx = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
    }

    SmartContainer.LazyStaggeredVerticalGrid(
        columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 },
    ) {
        item {
            NavigatorHeader(route = ScreenData.Settings)
        }

        item {
            SettingCategory(
                iconRes = R.drawable.ic_settings_4_line,
                titleRes = R.string.preference_player_settings
            ) {
                SettingSwitcher(
                    titleRes = R.string.preference_player_settings_ignore_audio_focus,
                    state = ignoreAudioFocus
                )
                SettingProgressSeekBar(
                    state = volumeControl,
                    title = "独立音量控制",
                    valueRange = 0..100
                )
                SettingStateSeekBar(
                    state = playMode,
                    selection = listOf("列表循环", "单曲循环", "随机播放"),
                    title = "播放模式"
                )
                SettingStateSeekBar(
                    state = seekbarHandler,
                    selection = stringArrayResource(id = R.array.seekbar_handler).toList(),
                    titleRes = R.string.preference_player_settings_seekbar_handler
                )
                SettingSwitcher(
                    state = enableSystemEq,
                    title = "启用系统均衡器",
                    subTitle = "实验性功能，存在较大机型差异"
                )
                val enableSystemEqValue by enableSystemEq
                AnimatedVisibility(visible = enableSystemEqValue) {
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
                                EQHelper.startSystemEqActivity {
                                    launcherForAudioFx.launch(it)
                                }
                            })
                    }
                }
            }
        }

        item {
            SettingCategory(
                iconRes = R.drawable.ic_lrc_fill,
                titleRes = R.string.preference_lyric_settings
            ) {
                if (RomUtils.isMeizu() || statusBarLyricExt.hasEnable()) {
                    SettingSwitcher(
                        titleRes = R.string.preference_lyric_settings_status_bar_lyric,
                        state = statusBarLyric
                    )
                }
                SettingSwitcher(
                    title = "歌词页展开时隐藏其他组件",
                    subTitle = "简化界面显示效果",
                    state = autoHideSeekBar,
                )
                SettingFilePicker(
                    state = lyricTypefacePath,
                    title = "自定义字体",
                    subTitle = "请选择TTF格式的字体文件",
                    mimeType = "font/ttf"
                )
                SettingStateSeekBar(
                    state = lyricGravity,
                    selection = stringArrayResource(id = R.array.lyric_gravity_text).toList(),
                    titleRes = R.string.preference_lyric_settings_text_gravity
                )
                SettingProgressSeekBar(
                    state = lyricTextSize,
                    title = "歌词文字大小",
                    valueRange = 14..36
                )
            }
        }

        item {
            SettingCategory(
                iconRes = R.drawable.ic_scan_line,
                titleRes = R.string.preference_media_source_settings
            ) {
                SettingSwitcher(
                    state = enableUnknownFilter,
                    titleRes = R.string.preference_media_source_settings_unknown_filter,
                    subTitleRes = R.string.preference_media_source_tips
                )
            }
        }

        item {
            SettingCategory(
                icon = painterResource(id = R.drawable.ic_loader_line),
                title = "其他"
            ) {
                SettingStateSeekBar(
                    state = darkModeOption,
                    selection = stringArrayResource(id = R.array.dark_mode_options).toList(),
                    titleRes = R.string.preference_dark_mode
                )
                SettingSwitcher(
                    state = enableDynamicTips,
                    titleRes = R.string.preference_media_source_settings_enable_dynamic_tips,
                    subTitleRes = R.string.preference_dynamic_tips
                )
                Row(
                    Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = "重新扫描",
                        color = Color(0xFFFF8B3F),
                        onClick = {
                            Toast.makeText(context, "扫描开始", Toast.LENGTH_SHORT).show()
                            MediaScannerConnection.scanFile(
                                context, arrayOf("/storage/emulated/0/"), null
                            ) { path, uri ->
                                Toast.makeText(context, "扫描结束", Toast.LENGTH_SHORT).show()
                                LogUtils.i("MediaScannerConnection", "path: $path, uri: $uri")
                            }
                        })
                }
            }
        }
    }
}