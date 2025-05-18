package com.lalilu.component.extension

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lalilu.RemixIcon
import com.lalilu.lmedia.extension.sortable.ItemExtraData
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.System
import com.lalilu.remixicon.media.voiceprintLine
import com.lalilu.remixicon.system.timeLine

object SortExtraPresetUI {

    @Composable
    fun Show(
        extraData: ItemExtraData?,
        modifier: Modifier = Modifier,
    ) {
        val text = remember { mutableStateOf(extraData?.text()) }
        val icon = remember { mutableStateOf(extraData?.icon()) }

        LaunchedEffect(extraData) {
            if (extraData != null) {
                text.value = extraData.text()
                icon.value = extraData.icon()
            }
        }

        AnimatedVisibility(
            visible = extraData != null,
            modifier = modifier.wrapContentWidth(),
            enter = fadeIn() + expandHorizontally(clip = false),
            exit = fadeOut() + shrinkHorizontally(clip = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colors.onSurface.copy(0.05f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    icon.value?.let {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onBackground.copy(0.8f)
                        )
                    }

                    Text(
                        modifier = Modifier,
                        text = "${text.value}",
                        color = MaterialTheme.colors.onBackground.copy(0.8f),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }

    private fun ItemExtraData?.text(): String? {
        return when (this) {
            is ItemExtraData.TrackNumber -> "$number"
            is ItemExtraData.PlayedCount -> "$count"
            else -> null
        }
    }

    private fun ItemExtraData?.icon(): ImageVector? {
        return when (this) {
            is ItemExtraData.TrackNumber -> RemixIcon.Media.voiceprintLine
            is ItemExtraData.PlayedCount -> RemixIcon.System.timeLine
            else -> null
        }
    }
}