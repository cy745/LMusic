package com.lalilu.lmusic.compose.screen.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.lmusic.compose.presenter.DetailScreenAction
import com.lalilu.lmusic.compose.presenter.DetailScreenLikeBtnPresenter
import com.lalilu.remixicon.HealthAndMedical
import com.lalilu.remixicon.healthandmedical.heart3Fill
import com.lalilu.remixicon.healthandmedical.heart3Line

fun provideSongLikeAction(mediaId: String): ScreenAction.Dynamic {
    return ScreenAction.Dynamic { actionContext ->
        val state = DetailScreenLikeBtnPresenter(mediaId)

        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        val haptic = LocalHapticFeedback.current
        val pressedState = interactionSource.collectIsPressedAsState()
        val iconColor by animateColorAsState(
            targetValue = if (state.isLiked) MaterialTheme.colors.primary
            else MaterialTheme.colors.onBackground.copy(0.3f),
            label = ""
        )
        val scaleValue by animateFloatAsState(
            animationSpec = SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy),
            targetValue = if (pressedState.value) 1.2f else 1f,
            label = ""
        )

        Surface(
            modifier = Modifier,
            color = iconColor.copy(0.15f)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .toggleable(
                        value = state.isLiked,
                        onValueChange = {
                            state.onAction(if (it) DetailScreenAction.Like else DetailScreenAction.UnLike)
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        role = Role.Checkbox,
                        interactionSource = interactionSource,
                        indication = null
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scaleValue),
                    imageVector = if (state.isLiked) RemixIcon.HealthAndMedical.heart3Fill else RemixIcon.HealthAndMedical.heart3Line,
                    tint = iconColor,
                    contentDescription = "A Checkable Button"
                )

                if (actionContext.isFullyExpanded) {
                    Text(
                        text = "收藏",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = iconColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}