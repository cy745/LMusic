package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RomUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.GuidingActivity
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.component.settings.*
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.utils.StatusBarLyricExt
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.getActivity

@Composable
fun SettingsScreen() {
    val windowSize = LocalWindowSize.current
    val context = LocalContext.current
    val ignoreAudioFocus = rememberDataSaverState(
        Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS,
        Config.DEFAULT_SETTINGS_IGNORE_AUDIO_FOCUS
    )
    val unknownFilter = rememberDataSaverState(
        Config.KEY_SETTINGS_MEDIA_UNKNOWN_FILTER,
        Config.DEFAULT_SETTINGS_MEDIA_UNKNOWN_FILTER
    )
    val statusBarLyric = rememberDataSaverState(
        Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE,
        Config.DEFAULT_SETTINGS_STATUS_LYRIC_ENABLE
    )
    val seekbarHandler = rememberDataSaverState(
        Config.KEY_SETTINGS_SEEKBAR_HANDLER,
        Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
    )
    val lyricGravity = rememberDataSaverState(
        Config.KEY_SETTINGS_LYRIC_GRAVITY,
        Config.DEFAULT_SETTINGS_LYRIC_GRAVITY
    )
    val lyricTextSize = rememberDataSaverState(
        Config.KEY_SETTINGS_LYRIC_TEXT_SIZE,
        Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
    )
    val kanhiraEnable = rememberDataSaverState(
        Config.KEY_SETTINGS_KANHIRA_ENABLE,
        Config.DEFAULT_SETTINGS_KANHIRA_ENABLE
    )
    val playMode = rememberDataSaverState(
        Config.KEY_SETTINGS_PLAY_MODE,
        Config.DEFAULT_SETTINGS_PLAY_MODE
    )
    val volumeControl = rememberDataSaverState(
        Config.KEY_SETTINGS_VOLUME_CONTROL,
        Config.DEFAULT_SETTINGS_VOLUME_CONTROL
    )
    val lyricTypefaceUri = rememberDataSaverState(
        Config.KEY_SETTINGS_LYRIC_TYPEFACE_URI,
        Config.DEFAULT_SETTINGS_LYRIC_TYPEFACE_URI
    )

    SmartContainer.StaggeredVerticalGrid(
        columns = if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1,
    ) {
        NavigatorHeader(route = ScreenData.Settings)

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
        }

        SettingCategory(
            iconRes = R.drawable.ic_lrc_fill,
            titleRes = R.string.preference_lyric_settings
        ) {
            if (RomUtils.isMeizu() || StatusBarLyricExt.hasEnable()) {
                SettingSwitcher(
                    titleRes = R.string.preference_lyric_settings_status_bar_lyric,
                    state = statusBarLyric
                )
            }
            SettingFilePicker(
                state = lyricTypefaceUri,
                title = "自定义字体",
                subTitle = "请选择TTF格式的字体文件",
                mimeType = "application/octet-stream"
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

        SettingCategory(
            iconRes = R.drawable.ic_scan_line,
            titleRes = R.string.preference_media_source_settings
        ) {
            SettingSwitcher(
                state = unknownFilter,
                titleRes = R.string.preference_media_source_settings_unknown_filter,
                subTitleRes = R.string.preference_media_source_tips
            )
        }

//                val isKanhiraInitialed by SearchTextUtil.isKanhiraInitialed
//                    .collectAsState(initial = false)

        SettingCategory(
            iconRes = R.drawable.ic_gradienter_line,
            titleRes = R.string.preference_extensions
        ) {
            SettingExtensionSwitcher(
                state = kanhiraEnable,
                initialed = true,
                title = "罗马字匹配功能"
            )
        }

        SettingCategory(
            icon = painterResource(id = R.drawable.ic_loader_line),
            title = "其他"
        ) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                TextButton(onClick = {
                    context.getActivity()?.apply {
                        ActivityUtils.startActivity(GuidingActivity::class.java)
                    }
                }) {
                    Text(text = "新手引导")
                }
            }
        }
    }
}