package com.lalilu.lmusic.compose.component.playing

import StatusBarLyric.API.StatusBarLyric
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.RomUtils
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.common.CustomRomUtils
import com.lalilu.common.kv.KVItem
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.split
import com.lalilu.component.extension.transform
import com.lalilu.component.lumo.components.RadioButton
import com.lalilu.component.lumo.components.card.CardDefaults
import com.lalilu.component.lumo.components.card.OutlinedCard
import com.lalilu.component.lumo.components.rememberAccordionGroupState
import com.lalilu.component.settings.SettingBaseAccordion
import com.lalilu.component.settings.SettingCategory
import com.lalilu.component.settings.SettingFilePicker
import com.lalilu.component.settings.SettingFloatProgressSeekBar
import com.lalilu.component.settings.SettingIntProgressSeekBar
import com.lalilu.component.settings.SettingSwitcher
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings
import com.lalilu.lmusic.compose.screen.playing.lyric.SerializableFont
import com.lalilu.lmusic.datastore.SettingsKV
import com.lalilu.lmusic.extension.SleepTimerSmallEntry
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.System
import com.lalilu.remixicon.editor.alignCenter
import com.lalilu.remixicon.editor.alignLeft
import com.lalilu.remixicon.editor.alignRight
import com.lalilu.remixicon.system.closeLine
import com.lalilu.remixicon.system.settingsFill
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private fun TextAlign.toInt(): Int {
    return when (this) {
        TextAlign.Start -> 0
        TextAlign.Center -> 1
        TextAlign.End -> 2
        else -> -1
    }
}

private fun Int.toTextAlign(): TextAlign {
    return when (this) {
        0 -> TextAlign.Start
        1 -> TextAlign.Center
        2 -> TextAlign.End
        else -> TextAlign.Start
    }
}

val LyricViewActionDialog = DialogItem.Dynamic(backgroundColor = Color.Transparent) {
    val settings: KVItem<LyricSettings> = koinInject(named("LyricSettings"))
    val lyricTypefacePath = settings.split(
        getValue = { it.mainFont },
        setValue = { value.copy(mainFont = it) },
        transform = transform(
            from = { SerializableFont.LoadedFont(it) },
            to = { item ->
                when (item) {
                    is SerializableFont.DeviceFont -> item.fontName
                    is SerializableFont.LoadedFont -> item.fontPath
                    null -> ""
                }
            }
        )
    )

    val listState = rememberLazyListState()
    val accordionGroupState = rememberAccordionGroupState(count = 3)
    val gravityOptions = stringArrayResource(id = R.array.lyric_gravity_text)
    val paddingBottom = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()

    val isOnTop = remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val dividerAnimation = animateFloatAsState(
        targetValue = if (isOnTop.value) 0f else 1f,
        visibilityThreshold = 0.001f
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(0.6f)
            .background(color = MaterialTheme.colors.background),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = paddingBottom + 12.dp),
        overscrollEffect = null
    ) {
        stickyHeader(key = "STICKY_HEADER") {
            SettingCategory(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .pointerInput(Unit) { detectTapGestures() }
                    .padding(top = 8.dp),
                gapHeight = 8.dp,
                icon = rememberVectorPainter(RemixIcon.System.settingsFill),
                title = "歌词设置",
                contentEnd = {
                    IconButton(onClick = { dismiss() }) {
                        Icon(
                            imageVector = RemixIcon.System.closeLine,
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = "Close lyric settings dialog button"
                        )
                    }
                },
                content = {
                    HorizontalDivider(
                        modifier = Modifier.graphicsLayer { alpha = dividerAnimation.value },
                        color = MaterialTheme.colors.onBackground.copy(0.2f)
                    )
                }
            )
        }
        item {
            SettingBaseAccordion(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                index = 0,
                groupState = accordionGroupState,
                title = stringResource(R.string.preference_lyric_settings_text_gravity),
                subTitle = "当前为 \"${gravityOptions.getOrNull(settings.value.textAlign.toInt())}\"",
                content = {
                    val selected =
                        remember { mutableIntStateOf(settings.value.textAlign.toInt()) }

                    gravityOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    selected.intValue = index
                                    settings.value =
                                        settings.value.copy(textAlign = index.toTextAlign())
                                    settings.save()
                                }
                                .padding(start = 16.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = remember {
                                    when (index) {
                                        0 -> RemixIcon.Editor.alignLeft
                                        1 -> RemixIcon.Editor.alignCenter
                                        else -> RemixIcon.Editor.alignRight
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
                                selected = index == selected.intValue,
                                onClick = {
                                    selected.intValue = index
                                    settings.value =
                                        settings.value.copy(textAlign = index.toTextAlign())
                                    settings.save()
                                }
                            )
                        }
                    }
                }
            )
        }
        item {
            SettingBaseAccordion(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                index = 1,
                groupState = accordionGroupState,
                title = "歌词样式调整",
                subTitle = "歌词样式",
                content = {
                    SettingIntProgressSeekBar(
                        value = { settings.value.mainFontSize.value },
                        onValueUpdate = {
                            settings.value = settings.value.copy(mainFontSize = it.sp)
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "歌词文字大小",
                        valueRange = 14..64
                    )
                    SettingIntProgressSeekBar(
                        value = { settings.value.mainLineHeight.value },
                        onValueUpdate = {
                            settings.value = settings.value.copy(mainLineHeight = it.sp)
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "歌词行高大小",
                        valueRange = 14..72
                    )
                    SettingIntProgressSeekBar(
                        value = { settings.value.mainFontWeight.toFloat() },
                        onValueUpdate = {
                            settings.value =
                                settings.value.copy(mainFontWeight = it.roundToInt())
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "歌词字重",
                        valueRange = 50..900
                    )
                    SettingIntProgressSeekBar(
                        value = {
                            settings.value.containerPadding.run {
                                (calculateLeftPadding(LayoutDirection.Ltr) +
                                        calculateRightPadding(LayoutDirection.Ltr)) / 2
                            }.value
                        },
                        onValueUpdate = {
                            settings.value = settings.value.copy(
                                containerPadding = PaddingValues(
                                    horizontal = it.dp,
                                    vertical = (settings.value.containerPadding.calculateTopPadding() +
                                            settings.value.containerPadding.calculateBottomPadding()) / 2
                                )
                            )
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "横向边距",
                        valueRange = 0..50
                    )
                }
            )
        }
        item {
            SettingBaseAccordion(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                index = 2,
                groupState = accordionGroupState,
                title = "翻译样式调整",
                subTitle = "翻译样式",
                content = {
                    SettingIntProgressSeekBar(
                        value = { settings.value.translationFontSize.value },
                        onValueUpdate = {
                            settings.value = settings.value.copy(translationFontSize = it.sp)
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "翻译文字大小",
                        valueRange = 14..64
                    )
                    SettingIntProgressSeekBar(
                        value = { settings.value.translationLineHeight.value },
                        onValueUpdate = {
                            settings.value = settings.value.copy(translationLineHeight = it.sp)
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "翻译行高大小",
                        valueRange = 14..72
                    )
                    SettingIntProgressSeekBar(
                        value = { settings.value.translationFontWeight.toFloat() },
                        onValueUpdate = {
                            settings.value =
                                settings.value.copy(translationFontWeight = it.roundToInt())
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "翻译字重",
                        valueRange = 50..900
                    )
                    SettingIntProgressSeekBar(
                        value = { settings.value.gapSize.value },
                        onValueUpdate = {
                            settings.value = settings.value.copy(gapSize = it.dp)
                        },
                        onFinishedUpdate = { settings.save() },
                        title = "歌词翻译间距",
                        valueRange = 0..50
                    )
                }
            )
        }
        item {
            OutlinedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colors.surface)
            ) {
                SettingIntProgressSeekBar(
                    value = { settings.value.timeOffset.toFloat() },
                    onValueUpdate = {
                        settings.value = settings.value.copy(timeOffset = it.roundToLong())
                    },
                    onFinishedUpdate = { settings.save() },
                    title = "歌词偏移时间(ms)",
                    valueRange = 0..500
                )
                SettingFloatProgressSeekBar(
                    value = { settings.value.scrollSpringDampingRatio.toFloat() },
                    onValueUpdate = {
                        settings.value = settings.value.copy(scrollSpringDampingRatio = it)
                    },
                    onFinishedUpdate = { settings.save() },
                    title = "歌词滚动阻尼（默认0.75）",
                    subTitle = "阻尼比值越大，滚动衰减越快",
                    valueRange = 0.3f..1f
                )
                SettingFloatProgressSeekBar(
                    value = { settings.value.scrollSpringStiffness.toFloat() },
                    onValueUpdate = {
                        settings.value = settings.value.copy(scrollSpringStiffness = it)
                    },
                    onFinishedUpdate = { settings.save() },
                    title = "歌词滚动刚度（默认100）",
                    subTitle = "刚度数值越大，滚动速度越快",
                    valueRange = 1f..400f
                )
                val statusBarLyricExt: StatusBarLyric = koinInject()
                if (RomUtils.isMeizu() || statusBarLyricExt.hasEnable() || CustomRomUtils.isFlyme) {
                    SettingSwitcher(
                        titleRes = R.string.preference_lyric_settings_status_bar_lyric,
                        state = SettingsKV.enableStatusLyric
                    )
                }
                SettingSwitcher(
                    title = "歌词翻译只显示当前行",
                    subTitle = "随君喜好开启",
                    state = { settings.value.onlyCurrentTranslationVisible },
                    onStateUpdate = {
                        settings.value = settings.value.copy(onlyCurrentTranslationVisible = it)
                        settings.save()
                    }
                )
                SettingSwitcher(
                    title = "歌词模糊效果",
                    subTitle = "为歌词添加一点模糊效果",
                    state = { settings.value.blurEffectEnable },
                    onStateUpdate = {
                        settings.value = settings.value.copy(blurEffectEnable = it)
                        settings.save()
                    }
                )
                SettingSwitcher(
                    title = "歌词页展开时隐藏其他组件",
                    subTitle = "简化界面显示效果",
                    state = SettingsKV.autoHideSeekbar,
                )
                SettingSwitcher(
                    title = "歌词页展开时屏幕常亮",
                    subTitle = "小心烧屏",
                    state = SettingsKV.keepScreenOnWhenLyricExpanded,
                )
                SettingFilePicker(
                    state = lyricTypefacePath,
                    title = "自定义字体",
                    subTitle = "请选择TTF格式的字体文件（存在bug，待修复）",
                    mimeType = "font/ttf"
                )
            }
        }
    }
}

@Composable
fun LyricViewToolbar(
    contentColor: () -> Color
) {
    val settings: KVItem<LyricSettings> = koinInject(named("LyricSettings"))

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val iconAlpha = animateFloatAsState(
            targetValue = if (settings.value.translationVisible) 1f else 0.5f,
            label = ""
        )

        SleepTimerSmallEntry(
            contentColor = contentColor
        )

        IconButton(onClick = { DialogWrapper.push(LyricViewActionDialog) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_text),
                contentDescription = "",
                tint = contentColor()
            )
        }

        IconButton(onClick = {
            settings.value = settings.value.copy(
                translationVisible = !settings.value.translationVisible
            )
        }) {
            Icon(
                modifier = Modifier.graphicsLayer { alpha = iconAlpha.value },
                painter = painterResource(id = R.drawable.translate_2),
                contentDescription = "",
                tint = contentColor()
            )
        }
    }
}