package com.lalilu.lmusic.compose.screen.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.navigation.NavGraphBuilder
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RomUtils
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmusic.GuidingActivity
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.component.settings.*
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.StatusBarLyricExt
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
object SettingsScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(ScreenData.Settings.name) {
            SettingsScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Settings.name
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsScreen(
    playingVM: PlayingViewModel = getViewModel()
) {
    val context = LocalContext.current
    val ignoreAudioFocus = playingVM.lMusicSp.ignoreAudioFocus
    val enableUnknownFilter = playingVM.lMusicSp.enableUnknownFilter
    val statusBarLyric = playingVM.lMusicSp.enableStatusLyric
    val seekbarHandler = playingVM.lMusicSp.seekBarHandler
    val lyricGravity = playingVM.lMusicSp.lyricGravity
    val lyricTextSize = playingVM.lMusicSp.lyricTextSize
    val playMode = playingVM.lMusicSp.playMode
    val volumeControl = playingVM.lMusicSp.volumeControl
    val lyricTypefaceUri = playingVM.lMusicSp.lyricTypefaceUri
    val enableSystemEq = playingVM.lMusicSp.enableSystemEq
    val enableDynamicTips = playingVM.lMusicSp.enableDynamicTips
    val autoHideSeekBar = playingVM.lMusicSp.autoHideSeekbar

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
                if (RomUtils.isMeizu() || StatusBarLyricExt.hasEnable()) {
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
                }
            }
        }
    }
}